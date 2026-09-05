/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.ldap.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.scim.core.repository.BaseRepository;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.repository.ScimRequestContext;
import org.apache.directory.scim.ldap.ldap.LdapDao;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.ldap.mapping.AttributeMapper;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * SCIMple {@link BaseRepository} implementation for SCIM {@link ScimUser} resources backed by LDAP.
 *
 * <p>This repository stores and retrieves user data from an LDAP directory. It uses the
 * LDAP {@code entryUUID} operational attribute as the SCIM resource identifier, ensuring
 * a stable identity that is independent of the entry's distinguished name.</p>
 *
 * <p>Attribute mapping between SCIM and LDAP is delegated to {@link AttributeMapper}.</p>
 *
 * @see BaseRepository
 * @see AttributeMapper
 */
@Named
@ApplicationScoped
public class LdapUserRepository extends BaseRepository<ScimUser> {

  private static final Logger LOG = LoggerFactory.getLogger(LdapUserRepository.class);

  private final LdapDao ldapDao;
  private final AttributeMapper attributeMapper;
  private final ScimLdapConfig properties;

  /**
   * Constructs a new {@code LdapUserRepository} with the required CDI-managed dependencies.
   *
   * @param ldapDao          the data access object for LDAP operations (search, create, modify, delete)
   * @param attributeMapper  maps between SCIM {@link ScimUser} attributes and LDAP entry attributes
   * @param properties       LDAP configuration properties including base DNs
   * @param patchHandler     applies SCIM PATCH operations to produce an updated resource
   */
  @Inject
  public LdapUserRepository(LdapDao ldapDao, AttributeMapper attributeMapper,
                             ScimLdapConfig properties, PatchHandler patchHandler) {
    super(ScimUser.class, patchHandler);
    this.ldapDao = ldapDao;
    this.attributeMapper = attributeMapper;
    this.properties = properties;
  }

  /**
   * Constructs a new {@code LdapUserRepository} without a {@link PatchHandler}.
   * Useful for testing when patch behavior is not under test.
   */
  LdapUserRepository(LdapDao ldapDao, AttributeMapper attributeMapper, ScimLdapConfig properties) {
    super(ScimUser.class, null);
    this.ldapDao = ldapDao;
    this.attributeMapper = attributeMapper;
    this.properties = properties;
  }

  protected LdapUserRepository() {
    super();
    this.ldapDao = null;
    this.attributeMapper = null;
    this.properties = null;
  }

  /**
   * Creates a new user in LDAP and returns the resulting SCIM representation.
   *
   * <p>The SCIM resource is mapped to an LDAP entry and added under the configured user base DN.
   * After creation, the entry is read back from the directory to obtain the server-assigned
   * {@code entryUUID}, which becomes the SCIM resource {@code id}.</p>
   *
   * @param resource       the SCIM user to create
   * @param requestContext the current SCIM request context
   * @return the created {@link ScimUser} with the server-assigned {@code id}
   * @throws ResourceException if the LDAP operation fails
   */
  @Override
  public ScimUser create(ScimUser resource, ScimRequestContext requestContext) throws ResourceException {
    try {
      Entry entry = attributeMapper.toEntry(resource, properties.getUserBaseDn());
      ldapDao.create(entry);

      // Read back to get the entryUUID assigned by the LDAP server
      Entry created = ldapDao.lookup(entry.getDn().toString());
      return attributeMapper.toScimUser(created);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to create user", e);
      throw new ResourceException(500, "Failed to create user");
    }
  }

  /**
   * Replaces an existing LDAP user with the given SCIM resource.
   *
   * <p>Locates the existing entry by searching for the {@code entryUUID} matching the given
   * {@code id}. The updated SCIM attributes are mapped to LDAP modifications (using
   * {@code REPLACE_ATTRIBUTE} operations) and applied to the entry. The modified entry is
   * then read back and returned as a SCIM user.</p>
   *
   * @param id             the SCIM resource id (LDAP {@code entryUUID})
   * @param resource       the replacement SCIM user data
   * @param requestContext the current SCIM request context
   * @return the updated {@link ScimUser} as read back from LDAP
   * @throws ResourceNotFoundException if no entry with the given {@code entryUUID} exists
   * @throws ResourceException         if the LDAP operation fails
   */
  @Override
  public ScimUser update(String id, ScimUser resource, ScimRequestContext requestContext) throws ResourceException {
    try {
      Entry existing = findByEntryUuid(id);
      if (existing == null) {
        throw new ResourceNotFoundException(id);
      }
      String dn = existing.getDn().toString();

      // Detect userName change → LDAP modifyDn before attribute modifications.
      // LDAP does not support multi-operation transactions (RFC 4511). If the
      // rename succeeds but the subsequent modify fails, the entry will remain at
      // the new DN with attributes only partially updated. Re-sending the request
      // to the new DN will recover the entry to a consistent state.
      String rdnAttr = attributeMapper.getUserRdnAttribute();
      Attribute currentRdnAttr = existing.get(rdnAttr);
      String existingUserName = currentRdnAttr != null ? currentRdnAttr.getString() : null;
      String newUserName = resource.getUserName();
      if (newUserName != null && !newUserName.equals(existingUserName)) {
        String newRdn = new Rdn(rdnAttr, newUserName).toString();
        ldapDao.rename(dn, newRdn);
        // Parent DN is unchanged; only the RDN changes
        Dn newDn = new Dn(new Rdn(rdnAttr, newUserName), existing.getDn().getParent());
        dn = newDn.toString();
      }

      Entry updated = attributeMapper.toEntry(resource, properties.getUserBaseDn());

      List<Modification> modifications = buildReplaceModifications(updated);
      if (!modifications.isEmpty()) {
        ldapDao.modify(dn, modifications.toArray(new Modification[0]));
      }

      Entry result = ldapDao.lookup(dn);
      return attributeMapper.toScimUser(result);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to update user", e);
      throw new ResourceException(500, "Failed to update user");
    }
  }

  /**
   * Retrieves a single SCIM user by its id.
   *
   * <p>Performs an LDAP search under the user base DN for an entry whose {@code entryUUID}
   * matches the given {@code id}. Returns {@code null} if no matching entry is found.</p>
   *
   * @param id             the SCIM resource id (LDAP {@code entryUUID})
   * @param requestContext the current SCIM request context
   * @return the matching {@link ScimUser}, or {@code null} if not found
   * @throws ResourceException if the LDAP operation fails
   */
  @Override
  public ScimUser get(String id, ScimRequestContext requestContext) throws ResourceException {
    try {
      Entry entry = findByEntryUuid(id);
      if (entry == null) {
        return null;
      }
      return attributeMapper.toScimUser(entry);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to get user", e);
      throw new ResourceException(500, "Failed to get user");
    }
  }

  /**
   * Deletes a SCIM user from LDAP.
   *
   * <p>Resolves the SCIM {@code id} to an LDAP entry via an {@code entryUUID} search,
   * then deletes the entry by its distinguished name.</p>
   *
   * @param id the SCIM resource id (LDAP {@code entryUUID})
   * @throws ResourceNotFoundException if no entry with the given {@code entryUUID} exists
   * @throws ResourceException         if the LDAP operation fails
   */
  @Override
  public void delete(String id) throws ResourceException {
    Entry entry = findByEntryUuid(id);
    if (entry == null) {
      throw new ResourceNotFoundException(id);
    }
    ldapDao.delete(entry.getDn().toString());
  }

  /**
   * Searches for SCIM users matching the given filter.
   *
   * <p>Delegates to {@link LdapDao#findUsers(Filter, PageRequest)} which translates the SCIM
   * filter, performs a paged LDAP search under the user base DN, and applies SCIM pagination.
   * The returned entries are mapped to {@link ScimUser} instances.</p>
   *
   * @param filter         the SCIM filter to apply, or {@code null} for all users
   * @param requestContext the current SCIM request context (includes pagination parameters)
   * @return a {@link FilterResponse} containing the matching users and total count
   * @throws ResourceException if the LDAP search or mapping fails
   */
  @Override
  public FilterResponse<ScimUser> find(Filter filter, ScimRequestContext requestContext) throws ResourceException {
    try {
      // LdapDao handles LDAP paged results control and SCIM pagination
      PageRequest pageRequest = requestContext.getPageRequest().orElse(null);
      FilterResponse<Entry> ldapResults = ldapDao.findUsers(filter, pageRequest);

      // Map LDAP entries to SCIM users
      List<ScimUser> users = new ArrayList<>();
      for (Entry entry : ldapResults.getResources()) {
        try {
          users.add(attributeMapper.toScimUser(entry));
        } catch (Exception e) {
          LOG.error("Failed to map LDAP entry to ScimUser: {}", entry.getDn(), e);
          throw new ResourceException(500, "Failed to map LDAP entry: " + entry.getDn());
        }
      }

      return new FilterResponse<>(users, ldapResults.getTotalResults());
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to search users", e);
      throw new ResourceException(500, "Failed to search users");
    }
  }

  private Entry findByEntryUuid(String entryUuid) throws ResourceException {
    return ldapDao.searchByAttribute(properties.getUserBaseDn(), "entryUUID", entryUuid);
  }

  private List<Modification> buildReplaceModifications(Entry updated) {
    List<Modification> modifications = new ArrayList<>();

    for (Attribute attr : updated) {
      String attrId = attr.getId();
      // Skip objectClass and DN-related attributes
      if ("objectClass".equalsIgnoreCase(attrId) || attributeMapper.getUserRdnAttribute().equalsIgnoreCase(attrId)) {
        continue;
      }
      modifications.add(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attr));
    }

    return modifications;
  }
}
