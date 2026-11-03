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
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
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
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * SCIMple {@link BaseRepository} implementation for SCIM {@link ScimGroup} resources backed by LDAP.
 *
 * <p>This repository stores and retrieves group data from an LDAP directory. It uses the
 * LDAP {@code entryUUID} operational attribute as the SCIM resource identifier. In addition
 * to standard CRUD operations, this class handles bidirectional resolution between SCIM
 * member identifiers ({@code entryUUID} values) and LDAP member distinguished names.</p>
 *
 * <p>Attribute mapping between SCIM and LDAP is delegated to {@link AttributeMapper}.</p>
 *
 *
 * @see BaseRepository
 * @see AttributeMapper
 */
@Named
@ApplicationScoped
public class LdapGroupRepository extends BaseRepository<ScimGroup> {

  private static final Logger LOG = LoggerFactory.getLogger(LdapGroupRepository.class);

  private final LdapDao ldapDao;
  private final AttributeMapper attributeMapper;
  private final ScimLdapConfig properties;

  /**
   * Constructs a new {@code LdapGroupRepository} with the required CDI-managed dependencies.
   *
   * @param ldapDao          the data access object for LDAP operations (search, create, modify, delete)
   * @param attributeMapper  maps between SCIM {@link ScimGroup} attributes and LDAP entry attributes
   * @param properties       LDAP configuration properties including base DNs
   * @param patchHandler     applies SCIM PATCH operations to produce an updated resource
   */
  @Inject
  public LdapGroupRepository(LdapDao ldapDao, AttributeMapper attributeMapper,
                              ScimLdapConfig properties, PatchHandler patchHandler) {
    super(ScimGroup.class, patchHandler);
    this.ldapDao = ldapDao;
    this.attributeMapper = attributeMapper;
    this.properties = properties;
  }

  /**
   * Constructs a new {@code LdapGroupRepository} without a {@link PatchHandler}.
   * Useful for testing when patch behavior is not under test.
   */
  LdapGroupRepository(LdapDao ldapDao, AttributeMapper attributeMapper, ScimLdapConfig properties) {
    super(ScimGroup.class, null);
    this.ldapDao = ldapDao;
    this.attributeMapper = attributeMapper;
    this.properties = properties;
  }

  protected LdapGroupRepository() {
    super();
    this.ldapDao = null;
    this.attributeMapper = null;
    this.properties = null;
  }

  /**
   * Creates a new group in LDAP and returns the resulting SCIM representation.
   *
   * <p>Before creating the LDAP entry, member SCIM identifiers ({@code entryUUID} values)
   * are resolved to LDAP distinguished names. After creation, the entry is read back to
   * obtain the server-assigned {@code entryUUID}, and member DNs are resolved back to
   * SCIM identifiers in the returned resource.</p>
   *
   * @param resource       the SCIM group to create
   * @param requestContext the current SCIM request context
   * @return the created {@link ScimGroup} with the server-assigned {@code id} and resolved members
   * @throws ResourceException if member resolution or the LDAP operation fails
   */
  @Override
  public ScimGroup create(ScimGroup resource, ScimRequestContext requestContext) throws ResourceException {
    try {
      // Resolve member SCIM ids to DNs
      resolveMemberIds(resource);

      Entry entry = attributeMapper.toEntry(resource, properties.getGroupBaseDn());
      ldapDao.create(entry);

      Entry created = ldapDao.lookup(entry.getDn().toString());
      return toScimGroupWithResolvedMembers(created);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to create group", e);
      throw new ResourceException(500, "Failed to create group");
    }
  }

  /**
   * Replaces an existing LDAP group with the given SCIM resource.
   *
   * <p>Locates the existing entry by searching for the {@code entryUUID} matching the given
   * {@code id}. Member SCIM identifiers are resolved to LDAP DNs before building the
   * LDAP modifications. The updated attributes are applied via {@code REPLACE_ATTRIBUTE}
   * operations, and the modified entry is read back with member DNs resolved to SCIM
   * identifiers.</p>
   *
   * @param id             the SCIM resource id (LDAP {@code entryUUID})
   * @param resource       the replacement SCIM group data
   * @param requestContext the current SCIM request context
   * @return the updated {@link ScimGroup} as read back from LDAP with resolved members
   * @throws ResourceNotFoundException if no entry with the given {@code entryUUID} exists
   * @throws ResourceException         if member resolution or the LDAP operation fails
   */
  @Override
  public ScimGroup update(String id, ScimGroup resource, ScimRequestContext requestContext) throws ResourceException {
    try {
      Entry existing = findByEntryUuid(id);
      if (existing == null) {
        throw new ResourceNotFoundException(id);
      }
      String dn = existing.getDn().toString();

      resolveMemberIds(resource);

      // Detect displayName change → LDAP modifyDn before attribute modifications.
      // LDAP does not support multi-operation transactions (RFC 4511). If the
      // rename succeeds but the subsequent modify fails, the entry will remain at
      // the new DN with attributes only partially updated. Re-sending the request
      // to the new DN will recover the entry to a consistent state.
      String rdnAttr = attributeMapper.getGroupRdnAttribute();
      Attribute currentRdnAttr = existing.get(rdnAttr);
      String existingDisplayName = currentRdnAttr != null ? currentRdnAttr.getString() : null;
      String newDisplayName = resource.getDisplayName();
      if (newDisplayName != null && !newDisplayName.equals(existingDisplayName)) {
        String newRdn = new Rdn(rdnAttr, newDisplayName).toString();
        ldapDao.rename(dn, newRdn);
        Dn newDn = new Dn(new Rdn(rdnAttr, newDisplayName), existing.getDn().getParent());
        dn = newDn.toString();
      }

      Entry updated = attributeMapper.toEntry(resource, properties.getGroupBaseDn());

      List<Modification> modifications = buildReplaceModifications(updated);
      if (!modifications.isEmpty()) {
        ldapDao.modify(dn, modifications.toArray(new Modification[0]));
      }

      Entry result = ldapDao.lookup(dn);
      return toScimGroupWithResolvedMembers(result);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to update group", e);
      throw new ResourceException(500, "Failed to update group");
    }
  }

  /**
   * Retrieves a single SCIM group by its id.
   *
   * <p>Performs an LDAP search under the group base DN for an entry whose {@code entryUUID}
   * matches the given {@code id}. Member DNs in the resulting entry are resolved to SCIM
   * identifiers. Returns {@code null} if no matching entry is found.</p>
   *
   * @param id             the SCIM resource id (LDAP {@code entryUUID})
   * @param requestContext the current SCIM request context
   * @return the matching {@link ScimGroup} with resolved members, or {@code null} if not found
   * @throws ResourceException if the LDAP operation fails
   */
  @Override
  public ScimGroup get(String id, ScimRequestContext requestContext) throws ResourceException {
    try {
      Entry entry = findByEntryUuid(id);
      if (entry == null) {
        return null;
      }
      return toScimGroupWithResolvedMembers(entry);
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to get group", e);
      throw new ResourceException(500, "Failed to get group");
    }
  }

  /**
   * Deletes a SCIM group from LDAP.
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
   * Searches for SCIM groups matching the given filter.
   *
   * <p>Delegates to {@link LdapDao#findGroups(Filter, PageRequest)} which translates the SCIM
   * filter, performs a paged LDAP search under the group base DN, and applies SCIM pagination.
   * The returned entries are mapped to {@link ScimGroup} instances with member DNs resolved
   * to SCIM identifiers.</p>
   *
   * @param filter         the SCIM filter to apply, or {@code null} for all groups
   * @param requestContext the current SCIM request context (includes pagination parameters)
   * @return a {@link FilterResponse} containing the matching groups and total count
   * @throws ResourceException if the LDAP search or mapping fails
   */
  @Override
  public FilterResponse<ScimGroup> find(Filter filter, ScimRequestContext requestContext) throws ResourceException {
    try {
      // LdapDao handles LDAP paged results control and SCIM pagination
      PageRequest pageRequest = requestContext.getPageRequest().orElse(null);
      FilterResponse<Entry> ldapResults = ldapDao.findGroups(filter, pageRequest);

      // Map LDAP entries to SCIM groups with resolved members
      List<ScimGroup> groups = new ArrayList<>();
      for (Entry entry : ldapResults.getResources()) {
        try {
          groups.add(toScimGroupWithResolvedMembers(entry));
        } catch (Exception e) {
          LOG.error("Failed to map LDAP entry to ScimGroup: {}", entry.getDn(), e);
          throw new ResourceException(500, "Failed to map LDAP entry: " + entry.getDn());
        }
      }

      return new FilterResponse<>(groups, ldapResults.getTotalResults());
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Failed to search groups", e);
      throw new ResourceException(500, "Failed to search groups");
    }
  }

  private Entry findByEntryUuid(String entryUuid) throws ResourceException {
    return ldapDao.searchByAttribute(properties.getGroupBaseDn(), "entryUUID", entryUuid);
  }

  private ScimGroup toScimGroupWithResolvedMembers(Entry entry) throws Exception {
    ScimGroup group = attributeMapper.toScimGroup(entry);

    // Resolve member DNs to SCIM ids (entryUUIDs).
    // NOTE: N+1 query pattern — each member DN triggers an individual LDAP lookup.
    // Production deployments with large groups should consider batch resolution
    // (e.g., a single search with an OR filter on all member DNs) to reduce round-trips.
    if (group.getMembers() != null) {
      List<GroupMembership> resolved = new ArrayList<>();
      for (GroupMembership member : group.getMembers()) {
        try {
          // member.getValue() currently holds the DN
          Entry memberEntry = ldapDao.lookup(member.getValue());
          if (memberEntry != null) {
            String memberUuid = memberEntry.get("entryUUID") != null
              ? memberEntry.get("entryUUID").getString() : member.getValue();
            // Prefer mail for display (matches SCIM convention), fall back to cn
            String display = memberEntry.get("mail") != null
              ? memberEntry.get("mail").getString()
              : (memberEntry.get("cn") != null ? memberEntry.get("cn").getString() : null);

            GroupMembership resolvedMember = new GroupMembership();
            resolvedMember.setValue(memberUuid);
            if (display != null) {
              resolvedMember.setDisplay(display);
            }
            resolved.add(resolvedMember);
          }
        } catch (ResourceNotFoundException e) {
          LOG.warn("Member DN not found (stale reference), skipping: {}", member.getValue());
        }
      }
      group.setMembers(resolved);
    }

    return group;
  }

  private void resolveMemberIds(ScimGroup group) throws ResourceException {
    if (group.getMembers() == null) {
      return;
    }
    List<GroupMembership> resolved = new ArrayList<>();
    for (GroupMembership member : group.getMembers()) {
      String memberId = member.getValue();
      if (memberId == null) {
        continue;
      }
      // If it looks like a DN, validate it parses as one and is under a known base DN
      if (memberId.contains("=")) {
        validateMemberDn(memberId);
        resolved.add(member);
        continue;
      }
      // Otherwise treat it as an entryUUID and resolve to DN
      Entry memberEntry = ldapDao.searchByAttribute(properties.getUserBaseDn(), "entryUUID", memberId);
      if (memberEntry == null) {
        // Also search in groups
        memberEntry = ldapDao.searchByAttribute(properties.getGroupBaseDn(), "entryUUID", memberId);
      }
      if (memberEntry == null) {
        throw new ResourceException(400, "Member not found: " + memberId);
      }
      GroupMembership resolvedMember = new GroupMembership();
      resolvedMember.setValue(memberEntry.getDn().toString());
      if (member.getDisplay() != null) {
        resolvedMember.setDisplay(member.getDisplay());
      }
      resolved.add(resolvedMember);
    }
    group.setMembers(resolved);
  }

  private void validateMemberDn(String memberDn) throws ResourceException {
    try {
      Dn parsed = new Dn(memberDn);
      Dn userDn = new Dn(properties.getUserBaseDn());
      Dn groupDn = new Dn(properties.getGroupBaseDn());
      if (!parsed.isDescendantOf(userDn) && !parsed.isDescendantOf(groupDn)) {
        throw new ResourceException(400,
          "Member DN is not under a recognized base DN: " + memberDn);
      }
    } catch (LdapInvalidDnException e) {
      throw new ResourceException(400, "Invalid member DN: " + memberDn);
    }
  }

  private List<Modification> buildReplaceModifications(Entry updated) {
    List<Modification> modifications = new ArrayList<>();

    for (Attribute attr : updated) {
      String attrId = attr.getId();
      if ("objectClass".equalsIgnoreCase(attrId) || attributeMapper.getGroupRdnAttribute().equalsIgnoreCase(attrId)) {
        continue;
      }
      modifications.add(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attr));
    }

    return modifications;
  }
}
