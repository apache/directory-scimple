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

import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.scim.ldap.mapping.FilterTranslator;
import org.apache.directory.scim.spec.exception.ConflictResourceException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapDaoTest {

  // =========================================================================
  // CRUD and search methods (mock LdapConnectionManager)
  // =========================================================================

  @Nested
  @DisplayName("CRUD operations")
  class CrudTest {

    LdapConnectionManager connectionManager = mock(LdapConnectionManager.class);
    FilterTranslator filterTranslator = mock(FilterTranslator.class);
    ScimLdapConfig config = mock(ScimLdapConfig.class);
    LdapConnection conn = mock(LdapConnection.class);
    LdapDao dao;

    @BeforeEach
    void setUp() throws Exception {
      dao = new LdapDao();
      setField(dao, "connectionManager", connectionManager);
      setField(dao, "filterTranslator", filterTranslator);
      setField(dao, "config", config);

      when(connectionManager.getConnection()).thenReturn(conn);
      when(config.getUserBaseDn()).thenReturn("ou=users,dc=example,dc=com");
      when(config.getGroupBaseDn()).thenReturn("ou=groups,dc=example,dc=com");
    }

    // ----- create -----

    @Test
    void createSuccess() throws Exception {
      Entry entry = new DefaultEntry("uid=test,ou=users,dc=example,dc=com");
      dao.create(entry);
      verify(conn).add(entry);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void createDuplicateThrowsConflict() throws Exception {
      Entry entry = new DefaultEntry("uid=test,ou=users,dc=example,dc=com");
      doThrow(new LdapEntryAlreadyExistsException()).when(conn).add(entry);
      assertThatThrownBy(() -> dao.create(entry))
        .isInstanceOf(ConflictResourceException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void createGenericLdapExceptionThrows500() throws Exception {
      Entry entry = new DefaultEntry("uid=test,ou=users,dc=example,dc=com");
      doThrow(new LdapException("fail")).when(conn).add(entry);
      assertThatThrownBy(() -> dao.create(entry))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(500));
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void createAlwaysReleasesConnection() throws Exception {
      Entry entry = new DefaultEntry("uid=test,ou=users,dc=example,dc=com");
      doThrow(new LdapException("fail")).when(conn).add(entry);
      try { dao.create(entry); } catch (ResourceException ignored) {}
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- lookup -----

    @Test
    void lookupSuccess() throws Exception {
      String dn = "uid=test,ou=users,dc=example,dc=com";
      Entry expected = new DefaultEntry(dn);
      when(conn.lookup(dn, "*", "+")).thenReturn(expected);
      assertThat(dao.lookup(dn)).isSameAs(expected);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void lookupNullThrowsNotFound() throws Exception {
      String dn = "uid=missing,ou=users,dc=example,dc=com";
      when(conn.lookup(dn, "*", "+")).thenReturn(null);
      assertThatThrownBy(() -> dao.lookup(dn))
        .isInstanceOf(ResourceNotFoundException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void lookupNoSuchObjectThrowsNotFound() throws Exception {
      String dn = "uid=missing,ou=users,dc=example,dc=com";
      when(conn.lookup(dn, "*", "+")).thenThrow(new LdapNoSuchObjectException());
      assertThatThrownBy(() -> dao.lookup(dn))
        .isInstanceOf(ResourceNotFoundException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- searchByAttribute -----

    @Test
    void searchByAttributeFound() throws Exception {
      Entry expected = new DefaultEntry("uid=test,ou=users,dc=example,dc=com");

      SearchCursor cursor = mock(SearchCursor.class);
      when(conn.search(any(SearchRequest.class))).thenReturn(cursor);
      when(cursor.next()).thenReturn(true, false);
      when(cursor.isEntry()).thenReturn(true);
      when(cursor.getEntry()).thenReturn(expected);

      assertThat(dao.searchByAttribute("ou=users,dc=example,dc=com", "uid", "testuser"))
        .isSameAs(expected);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void searchByAttributeNotFound() throws Exception {
      SearchCursor cursor = mock(SearchCursor.class);
      when(conn.search(any(SearchRequest.class))).thenReturn(cursor);
      when(cursor.next()).thenReturn(false);

      assertThat(dao.searchByAttribute("ou=users,dc=example,dc=com", "uid", "nobody"))
        .isNull();
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- findUsers -----

    @Test
    void findUsersReturnsEntries() throws Exception {
      ExprNode mockFilter = mock(ExprNode.class);
      when(filterTranslator.buildUserSearchFilter(any())).thenReturn(mockFilter);

      Entry entry1 = new DefaultEntry("uid=alice,ou=users,dc=example,dc=com");
      Entry entry2 = new DefaultEntry("uid=bob,ou=users,dc=example,dc=com");

      SearchCursor cursor = mock(SearchCursor.class);
      when(conn.search(any(SearchRequest.class))).thenReturn(cursor);
      when(cursor.next()).thenReturn(true, true, false);
      when(cursor.isEntry()).thenReturn(true, true);
      when(cursor.getEntry()).thenReturn(entry1, entry2);
      when(cursor.getSearchResultDone()).thenReturn(null);

      FilterResponse<Entry> result = dao.findUsers((Filter) null, null);
      assertThat(result.getResources()).containsExactly(entry1, entry2);
      assertThat(result.getTotalResults()).isEqualTo(2);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void findUsersEmptyReturnsEmptyList() throws Exception {
      ExprNode mockFilter = mock(ExprNode.class);
      when(filterTranslator.buildUserSearchFilter(any())).thenReturn(mockFilter);

      SearchCursor cursor = mock(SearchCursor.class);
      when(conn.search(any(SearchRequest.class))).thenReturn(cursor);
      when(cursor.next()).thenReturn(false);
      when(cursor.getSearchResultDone()).thenReturn(null);

      FilterResponse<Entry> result = dao.findUsers((Filter) null, null);
      assertThat(result.getResources()).isEmpty();
      assertThat(result.getTotalResults()).isEqualTo(0);
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- findGroups -----

    @Test
    void findGroupsReturnsEntries() throws Exception {
      ExprNode mockFilter = mock(ExprNode.class);
      when(filterTranslator.buildGroupSearchFilter(any())).thenReturn(mockFilter);

      Entry entry1 = new DefaultEntry("cn=admins,ou=groups,dc=example,dc=com");

      SearchCursor cursor = mock(SearchCursor.class);
      when(conn.search(any(SearchRequest.class))).thenReturn(cursor);
      when(cursor.next()).thenReturn(true, false);
      when(cursor.isEntry()).thenReturn(true);
      when(cursor.getEntry()).thenReturn(entry1);
      when(cursor.getSearchResultDone()).thenReturn(null);

      FilterResponse<Entry> result = dao.findGroups((Filter) null, null);
      assertThat(result.getResources()).containsExactly(entry1);
      assertThat(result.getTotalResults()).isEqualTo(1);
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- modify -----

    @Test
    void modifySuccess() throws Exception {
      String dn = "uid=test,ou=users,dc=example,dc=com";
      Modification mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", "New Name");
      dao.modify(dn, mod);
      verify(conn).modify(dn, mod);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void modifyNoSuchObjectThrowsNotFound() throws Exception {
      String dn = "uid=missing,ou=users,dc=example,dc=com";
      Modification mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", "New Name");
      doThrow(new LdapNoSuchObjectException()).when(conn).modify(dn, mod);
      assertThatThrownBy(() -> dao.modify(dn, mod))
        .isInstanceOf(ResourceNotFoundException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- delete -----

    @Test
    void deleteSuccess() throws Exception {
      String dn = "uid=test,ou=users,dc=example,dc=com";
      dao.delete(dn);
      verify(conn).delete(dn);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void deleteNoSuchObjectThrowsNotFound() throws Exception {
      String dn = "uid=missing,ou=users,dc=example,dc=com";
      doThrow(new LdapNoSuchObjectException()).when(conn).delete(dn);
      assertThatThrownBy(() -> dao.delete(dn))
        .isInstanceOf(ResourceNotFoundException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    // ----- rename -----

    @Test
    void rename_delegatesToConnectionRename() throws Exception {
      String dn = "uid=old,ou=users,dc=example,dc=com";
      String newRdn = "uid=new";
      dao.rename(dn, newRdn);
      verify(conn).rename(dn, newRdn, true);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void rename_entryNotFound_throwsResourceNotFoundException() throws Exception {
      String dn = "uid=missing,ou=users,dc=example,dc=com";
      doThrow(new LdapNoSuchObjectException()).when(conn).rename(anyString(), anyString(), anyBoolean());
      assertThatThrownBy(() -> dao.rename(dn, "uid=new"))
        .isInstanceOf(ResourceNotFoundException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    @Test
    void rename_entryAlreadyExists_throwsConflictResourceException() throws Exception {
      String dn = "uid=old,ou=users,dc=example,dc=com";
      doThrow(new LdapEntryAlreadyExistsException()).when(conn).rename(anyString(), anyString(), anyBoolean());
      assertThatThrownBy(() -> dao.rename(dn, "uid=existing"))
        .isInstanceOf(ConflictResourceException.class);
      verify(connectionManager).releaseConnection(conn);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    }
  }
}
