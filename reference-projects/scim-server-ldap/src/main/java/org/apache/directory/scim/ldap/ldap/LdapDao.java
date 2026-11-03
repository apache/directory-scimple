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

package org.apache.directory.scim.ldap.ldap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.scim.ldap.mapping.FilterTranslator;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.exception.ConflictResourceException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access object that performs LDAP CRUD operations on behalf of the SCIM resource providers.
 *
 * <p>Each public method obtains an {@link LdapConnection} from the
 * {@link LdapConnectionManager}, executes the operation, and ensures the connection is
 * released in a {@code finally} block. LDAP-specific exceptions are mapped to the
 * appropriate {@link ResourceException} subclass (e.g.
 * {@link ResourceNotFoundException}, {@link ConflictResourceException}).</p>
 *
 * <p>Search operations use the LDAP Simple Paged Results Control (RFC 2696) to
 * iterate through large result sets in server-sized pages, rather than loading all
 * matching entries into memory at once. SCIM pagination parameters ({@code startIndex}
 * and {@code count}) are applied during iteration.</p>
 */
@ApplicationScoped
public class LdapDao {

  private static final Logger LOG = LoggerFactory.getLogger(LdapDao.class);

  /** Default page size for the LDAP Simple Paged Results Control. */
  private static final int LDAP_PAGE_SIZE = 100;

  @Inject
  LdapConnectionManager connectionManager;

  @Inject
  FilterTranslator filterTranslator;

  @Inject
  ScimLdapConfig config;

  protected LdapDao() {}

  /**
   * Adds a new entry to the LDAP directory.
   *
   * @param entry the LDAP entry to create
   * @throws ConflictResourceException if an entry with the same DN already exists
   * @throws ResourceException         if the LDAP operation fails for any other reason
   */
  public void create(Entry entry) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      conn.add(entry);
    } catch (LdapEntryAlreadyExistsException e) {
      throw new ConflictResourceException("Entry already exists: " + entry.getDn(), e);
    } catch (LdapException e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Looks up an LDAP entry by its distinguished name, requesting all user and operational attributes.
   *
   * @param dn the distinguished name of the entry to retrieve
   * @return the matching LDAP entry (never {@code null})
   * @throws ResourceNotFoundException if no entry exists at the given DN
   * @throws ResourceException         if the LDAP operation fails
   */
  public Entry lookup(String dn) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      Entry entry = conn.lookup(dn, "*", "+");
      if (entry == null) {
        throw new ResourceNotFoundException(dn);
      }
      return entry;
    } catch (ResourceNotFoundException e) {
      throw e;
    } catch (LdapNoSuchObjectException e) {
      throw new ResourceNotFoundException(dn, e);
    } catch (LdapException e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Searches for users matching a SCIM filter with pagination support.
   *
   * <p>Uses the LDAP Simple Paged Results Control (RFC 2696) to iterate through
   * results in server-sized pages. SCIM pagination is applied during iteration:
   * entries before {@code startIndex} are skipped without mapping, and iteration
   * stops after {@code count} entries are collected.</p>
   *
   * @param scimFilter  the SCIM filter, may be {@code null} for all users
   * @param pageRequest the SCIM pagination parameters, may be {@code null}
   * @return a {@link FilterResponse} with the requested page and correct {@code totalResults}
   * @throws ResourceException if the LDAP search fails
   */
  public FilterResponse<Entry> findUsers(Filter scimFilter, PageRequest pageRequest) throws ResourceException {
    ExprNode filter = filterTranslator.buildUserSearchFilter(scimFilter);
    return pagedSearch(config.getUserBaseDn(), filter, pageRequest);
  }

  /**
   * Searches for groups matching a SCIM filter with pagination support.
   *
   * @param scimFilter  the SCIM filter, may be {@code null} for all groups
   * @param pageRequest the SCIM pagination parameters, may be {@code null}
   * @return a {@link FilterResponse} with the requested page and correct {@code totalResults}
   * @throws ResourceException if the LDAP search fails
   */
  public FilterResponse<Entry> findGroups(Filter scimFilter, PageRequest pageRequest) throws ResourceException {
    ExprNode filter = filterTranslator.buildGroupSearchFilter(scimFilter);
    return pagedSearch(config.getGroupBaseDn(), filter, pageRequest);
  }

  /**
   * Searches for a single LDAP entry matching an exact attribute value under the given base DN.
   *
   * @param baseDn    the base DN from which to search
   * @param attrName  the LDAP attribute name to match
   * @param attrValue the attribute value to match
   * @return the first matching entry, or {@code null} if no match is found
   * @throws ResourceException if the LDAP search fails
   */
  public Entry searchByAttribute(String baseDn, String attrName, String attrValue) throws ResourceException {
    ExprNode filter = new EqualityNode<>(attrName, attrValue);
    List<Entry> results = searchAll(baseDn, filter);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }

  /**
   * Applies one or more modifications to an existing LDAP entry.
   *
   * @param dn            the distinguished name of the entry to modify
   * @param modifications the modifications to apply
   * @throws ResourceNotFoundException if no entry exists at the given DN
   * @throws ResourceException         if the LDAP operation fails
   */
  public void modify(String dn, Modification... modifications) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      conn.modify(dn, modifications);
    } catch (LdapNoSuchObjectException e) {
      throw new ResourceNotFoundException(dn, e);
    } catch (LdapException e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Renames an LDAP entry by replacing its RDN.
   *
   * <p>Uses {@code deleteOldRdn=true} so the old RDN attribute value is removed
   * from the entry after the rename, keeping the entry consistent with its new DN.</p>
   *
   * <p><strong>Note:</strong> LDAP does not support multi-operation transactions
   * (RFC 4511). Callers that rename and then modify in sequence accept the risk
   * that a modify failure after a successful rename leaves the entry at the new
   * DN with its attributes only partially updated.</p>
   *
   * @param dn     the current DN of the entry
   * @param newRdn the new RDN string, e.g. {@code "uid=jsmith"}
   * @throws ResourceNotFoundException  if no entry exists at {@code dn}
   * @throws ConflictResourceException  if an entry already exists at the new DN
   * @throws ResourceException          if the LDAP operation fails
   */
  public void rename(String dn, String newRdn) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      conn.rename(dn, newRdn, true);   // deleteOldRdn = true
    } catch (LdapEntryAlreadyExistsException e) {
      throw new ConflictResourceException("Entry already exists: " + newRdn, e);
    } catch (LdapNoSuchObjectException e) {
      throw new ResourceNotFoundException(dn, e);
    } catch (LdapException e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Deletes an LDAP entry by its distinguished name.
   *
   * @param dn the distinguished name of the entry to delete
   * @throws ResourceNotFoundException if no entry exists at the given DN
   * @throws ResourceException         if the LDAP operation fails
   */
  public void delete(String dn) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      conn.delete(dn);
    } catch (LdapNoSuchObjectException e) {
      throw new ResourceNotFoundException(dn, e);
    } catch (LdapException e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Executes a paged LDAP search using the Simple Paged Results Control (RFC 2696).
   *
   * <p>This method handles SCIM-to-LDAP pagination translation:</p>
   * <ul>
   *   <li>Iterates through LDAP result pages of {@value #LDAP_PAGE_SIZE} entries each</li>
   *   <li>Counts all matching entries to produce an accurate {@code totalResults}</li>
   *   <li>Skips entries before the SCIM {@code startIndex} without processing them</li>
   *   <li>Collects up to {@code count} entries for the requested page</li>
   *   <li>Continues iterating after the page is full to get the correct total count</li>
   * </ul>
   *
   * <p>The LDAP paged results control is forward-only (cookie-based), so random
   * access to arbitrary pages requires iterating from the beginning. For large
   * directories, consider LDAP-side filtering to reduce the result set.</p>
   *
   * @param baseDn      the base DN to search under
   * @param filter      the LDAP filter (ExprNode)
   * @param pageRequest SCIM pagination parameters (1-based startIndex + count), may be {@code null}
   * @return a {@link FilterResponse} containing the requested page of entries and the total count
   */
  private FilterResponse<Entry> pagedSearch(String baseDn, ExprNode filter, PageRequest pageRequest)
    throws ResourceException {

    // --- SCIM Pagination (RFC 7644 §3.4.2.4) ---
    //
    // SCIM uses 1-based indexing:
    //   startIndex=1 means "start from the first result"
    //   startIndex=11, count=10 means "return results 11-20"
    //
    // We convert to 0-based skip/limit for iteration.
    // totalResults MUST be the total number of matching entries, not the page size.
    long skip = 0;
    long limit = Long.MAX_VALUE;

    if (pageRequest != null) {
      if (pageRequest.getStartIndex() != null) {
        // Convert SCIM 1-based startIndex to 0-based skip count
        skip = Math.max(0, pageRequest.getStartIndex() - 1L);
      }
      if (pageRequest.getCount() != null) {
        limit = pageRequest.getCount();
      }
    }

    // --- LDAP Simple Paged Results Control (RFC 2696) ---
    //
    // Instead of fetching all matching entries at once (which can overwhelm memory
    // for large directories), we request entries in pages of LDAP_PAGE_SIZE.
    //
    // The control uses a cookie-based iteration model:
    //   1. First request: send PagedResultsControl with size=LDAP_PAGE_SIZE, empty cookie
    //   2. Server returns up to LDAP_PAGE_SIZE entries + a cookie
    //   3. Next request: send the cookie back to get the next page
    //   4. Repeat until the server returns an empty cookie (no more results)
    //
    // We iterate through ALL pages to get an accurate totalResults count,
    // but only collect entries that fall within the requested SCIM page.

    List<Entry> pageEntries = new ArrayList<>();
    int totalResults = 0;

    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();
      byte[] cookie = null;

      do {
        SearchRequest request = new SearchRequestImpl();
        request.setBase(new Dn(baseDn));
        request.setScope(SearchScope.SUBTREE);
        request.setFilter(filter);
        request.addAttributes("*", "+");

        // Add the paged results control with the current cookie
        PagedResults pagedControl = new PagedResultsImpl();
        pagedControl.setSize(LDAP_PAGE_SIZE);
        if (cookie != null) {
          pagedControl.setCookie(cookie);
        }
        request.addControl(pagedControl);

        try (SearchCursor cursor = conn.search(request)) {
          while (cursor.next()) {
            if (cursor.isEntry()) {
              // Count every matching entry for totalResults
              totalResults++;

              // Collect entries that fall within the requested SCIM page:
              //   - Skip the first 'skip' entries (before startIndex)
              //   - Collect up to 'limit' entries (the page)
              //   - Continue counting after the page for totalResults
              if (totalResults > skip && pageEntries.size() < limit) {
                pageEntries.add(cursor.getEntry());
              }
            }
          }

          // Extract the cookie from the response for the next page
          // An empty cookie (length 0) means no more results
          if (cursor.getSearchResultDone() != null) {
            PagedResults responseControl = (PagedResults) cursor.getSearchResultDone()
              .getControl(PagedResults.OID);
            if (responseControl != null && responseControl.getCookie() != null
              && responseControl.getCookie().length > 0) {
              cookie = responseControl.getCookie();
            } else {
              cookie = null; // No more pages
            }
          } else {
            cookie = null;
          }
        }
      } while (cookie != null);

      return new FilterResponse<>(pageEntries, totalResults);
    } catch (LdapException e) {
      throw mapException(e);
    } catch (Exception e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  /**
   * Searches for all entries matching the filter (no pagination). Used internally
   * for simple lookups like {@link #searchByAttribute}.
   */
  private List<Entry> searchAll(String baseDn, ExprNode filter) throws ResourceException {
    LdapConnection conn = null;
    try {
      conn = connectionManager.getConnection();

      SearchRequest request = new SearchRequestImpl();
      request.setBase(new Dn(baseDn));
      request.setScope(SearchScope.SUBTREE);
      request.setFilter(filter);
      request.addAttributes("*", "+");

      List<Entry> results = new ArrayList<>();
      try (SearchCursor cursor = conn.search(request)) {
        while (cursor.next()) {
          if (cursor.isEntry()) {
            results.add(cursor.getEntry());
          }
        }
      }
      return results;
    } catch (LdapException e) {
      throw mapException(e);
    } catch (Exception e) {
      throw mapException(e);
    } finally {
      connectionManager.releaseConnection(conn);
    }
  }

  private ResourceException mapException(Exception e) {
    if (e instanceof ResourceException re) {
      return re;
    }
    LOG.error("LDAP operation failed", e);
    return new ResourceException(500, "LDAP operation failed");
  }
}
