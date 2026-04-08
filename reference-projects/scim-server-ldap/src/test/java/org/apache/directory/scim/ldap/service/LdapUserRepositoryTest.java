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

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapUserRepositoryTest {

  static final String BASE_DN = "ou=users,dc=example,dc=com";

  LdapDao ldapDao = mock(LdapDao.class);
  AttributeMapper attributeMapper = mock(AttributeMapper.class);
  ScimLdapConfig config = mock(ScimLdapConfig.class);

  LdapUserRepository repository;

  @BeforeEach
  void setUp() {
    when(config.getUserBaseDn()).thenReturn(BASE_DN);
    when(attributeMapper.getUserRdnAttribute()).thenReturn("uid");

    repository = new LdapUserRepository(ldapDao, attributeMapper, config);
  }

  // =========================================================================
  // find
  // =========================================================================

  @Nested
  @DisplayName("find")
  class FindTest {

    @Test
    void nullFilterSearchesWithObjectClassFilter() throws Exception {
      Entry entry1 = new DefaultEntry("uid=alice," + BASE_DN);
      ScimUser user1 = new ScimUser();
      user1.setId("uuid-1");

      when(ldapDao.findUsers(any(), any())).thenReturn(new FilterResponse<>(List.of(entry1), 1));
      when(attributeMapper.toScimUser(entry1)).thenReturn(user1);

      FilterResponse<ScimUser> response = repository.find(null, ScimRequestContext.empty());

      assertThat(response.getResources()).containsExactly(user1);
      assertThat(response.getTotalResults()).isEqualTo(1);
    }

    @Test
    void withFilterDelegatesToFindUsers() throws Exception {
      Filter filter = new Filter("userName eq \"john\"");

      Entry entry = new DefaultEntry("uid=john," + BASE_DN);
      ScimUser user = new ScimUser();
      user.setId("uuid-john");

      when(ldapDao.findUsers(any(), any())).thenReturn(new FilterResponse<>(List.of(entry), 1));
      when(attributeMapper.toScimUser(entry)).thenReturn(user);

      FilterResponse<ScimUser> response = repository.find(filter, ScimRequestContext.empty());

      assertThat(response.getResources()).containsExactly(user);
      assertThat(response.getTotalResults()).isEqualTo(1);
    }

    @Test
    void returnsAllMappedUsers() throws Exception {
      Entry entry1 = new DefaultEntry("uid=alice," + BASE_DN);
      Entry entry2 = new DefaultEntry("uid=bob," + BASE_DN);
      ScimUser user1 = new ScimUser();
      user1.setId("uuid-1");
      ScimUser user2 = new ScimUser();
      user2.setId("uuid-2");

      when(ldapDao.findUsers(any(), any())).thenReturn(new FilterResponse<>(List.of(entry1, entry2), 2));
      when(attributeMapper.toScimUser(entry1)).thenReturn(user1);
      when(attributeMapper.toScimUser(entry2)).thenReturn(user2);

      FilterResponse<ScimUser> response = repository.find(null, ScimRequestContext.empty());

      assertThat(response.getResources()).containsExactly(user1, user2);
      assertThat(response.getTotalResults()).isEqualTo(2);
    }

    @Test
    void emptyResultsReturnsEmptyFilterResponse() throws Exception {
      when(ldapDao.findUsers(any(), any())).thenReturn(new FilterResponse<>(Collections.emptyList(), 0));

      FilterResponse<ScimUser> response = repository.find(null, ScimRequestContext.empty());

      assertThat(response.getResources()).isEmpty();
      assertThat(response.getTotalResults()).isEqualTo(0);
    }

    // ----- Pagination helpers and tests using 5 entries -----

    private static ScimUser newUser(String id) {
      ScimUser u = new ScimUser();
      u.setId(id);
      return u;
    }

    ScimUser pUser1 = newUser("uuid-p1");
    ScimUser pUser2 = newUser("uuid-p2");
    ScimUser pUser3 = newUser("uuid-p3");
    ScimUser pUser4 = newUser("uuid-p4");
    ScimUser pUser5 = newUser("uuid-p5");

    Entry pEntry1;
    Entry pEntry2;
    Entry pEntry3;
    Entry pEntry4;
    Entry pEntry5;

    private void setupEntryMappings() throws Exception {
      pEntry1 = new DefaultEntry("uid=alice," + BASE_DN);
      pEntry2 = new DefaultEntry("uid=bob," + BASE_DN);
      pEntry3 = new DefaultEntry("uid=carol," + BASE_DN);
      pEntry4 = new DefaultEntry("uid=dave," + BASE_DN);
      pEntry5 = new DefaultEntry("uid=eve," + BASE_DN);

      when(attributeMapper.toScimUser(pEntry1)).thenReturn(pUser1);
      when(attributeMapper.toScimUser(pEntry2)).thenReturn(pUser2);
      when(attributeMapper.toScimUser(pEntry3)).thenReturn(pUser3);
      when(attributeMapper.toScimUser(pEntry4)).thenReturn(pUser4);
      when(attributeMapper.toScimUser(pEntry5)).thenReturn(pUser5);
    }

    @Test
    void paginationFirstPage() throws Exception {
      setupEntryMappings();

      when(ldapDao.findUsers(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(pEntry1, pEntry2), 5));

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(1).setCount(2));

      FilterResponse<ScimUser> response = repository.find(null, context);

      assertThat(response.getResources()).containsExactly(pUser1, pUser2);
      assertThat(response.getTotalResults()).isEqualTo(5);
    }

    @Test
    void paginationMiddlePage() throws Exception {
      setupEntryMappings();

      when(ldapDao.findUsers(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(pEntry3, pEntry4), 5));

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(3).setCount(2));

      FilterResponse<ScimUser> response = repository.find(null, context);

      assertThat(response.getResources()).containsExactly(pUser3, pUser4);
      assertThat(response.getTotalResults()).isEqualTo(5);
    }

    @Test
    void paginationLastPage() throws Exception {
      setupEntryMappings();

      when(ldapDao.findUsers(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(pEntry5), 5));

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(5).setCount(10));

      FilterResponse<ScimUser> response = repository.find(null, context);

      assertThat(response.getResources()).containsExactly(pUser5);
      assertThat(response.getTotalResults()).isEqualTo(5);
    }

    @Test
    void paginationBeyondResults() throws Exception {
      setupEntryMappings();

      when(ldapDao.findUsers(any(), any()))
        .thenReturn(new FilterResponse<>(Collections.emptyList(), 5));

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(6).setCount(2));

      FilterResponse<ScimUser> response = repository.find(null, context);

      assertThat(response.getResources()).isEmpty();
      assertThat(response.getTotalResults()).isEqualTo(5);
    }

    @Test
    void noPaginationReturnsAll() throws Exception {
      setupEntryMappings();

      when(ldapDao.findUsers(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(pEntry1, pEntry2, pEntry3, pEntry4, pEntry5), 5));

      FilterResponse<ScimUser> response = repository.find(null, ScimRequestContext.empty());

      assertThat(response.getResources()).containsExactly(pUser1, pUser2, pUser3, pUser4, pUser5);
      assertThat(response.getTotalResults()).isEqualTo(5);
    }

    @Test
    void resourceExceptionFromSearchPropagates() throws Exception {
      when(ldapDao.findUsers(any(), any()))
        .thenThrow(new ResourceException(503, "LDAP unavailable"));

      assertThatThrownBy(() -> repository.find(null, ScimRequestContext.empty()))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(503));
    }

    @Test
    void mappingExceptionWrapsIn500() throws Exception {
      Entry entry = new DefaultEntry("uid=bad," + BASE_DN);
      when(ldapDao.findUsers(any(), any())).thenReturn(new FilterResponse<>(List.of(entry), 1));
      when(attributeMapper.toScimUser(entry)).thenThrow(new RuntimeException("bad attribute"));

      assertThatThrownBy(() -> repository.find(null, ScimRequestContext.empty()))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(500));
    }
  }

  // =========================================================================
  // get
  // =========================================================================

  @Nested
  @DisplayName("get")
  class GetTest {

    @Test
    void foundReturnsScimUser() throws Exception {
      Entry entry = new DefaultEntry("uid=alice," + BASE_DN);
      ScimUser user = new ScimUser();
      user.setId("uuid-1");

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", "uuid-1")).thenReturn(entry);
      when(attributeMapper.toScimUser(entry)).thenReturn(user);

      ScimUser result = repository.get("uuid-1", ScimRequestContext.empty());

      assertThat(result).isSameAs(user);
    }

    @Test
    void notFoundReturnsNull() throws Exception {
      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", "missing")).thenReturn(null);

      ScimUser result = repository.get("missing", ScimRequestContext.empty());

      assertThat(result).isNull();
    }

    @Test
    void resourceExceptionPropagates() throws Exception {
      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", "uuid-1"))
        .thenThrow(new ResourceException(503, "LDAP unavailable"));

      assertThatThrownBy(() -> repository.get("uuid-1", ScimRequestContext.empty()))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(503));
    }
  }

  // =========================================================================
  // create
  // =========================================================================

  @Nested
  @DisplayName("create")
  class CreateTest {

    @Test
    void successVerifiesChain() throws Exception {
      ScimUser input = new ScimUser();
      input.setUserName("alice");

      Entry newEntry = new DefaultEntry("uid=alice," + BASE_DN);
      Entry createdEntry = new DefaultEntry("uid=alice," + BASE_DN);
      ScimUser createdUser = new ScimUser();
      createdUser.setId("uuid-created");

      when(attributeMapper.toEntry(input, BASE_DN)).thenReturn(newEntry);
      when(ldapDao.lookup("uid=alice," + BASE_DN)).thenReturn(createdEntry);
      when(attributeMapper.toScimUser(createdEntry)).thenReturn(createdUser);

      ScimUser result = repository.create(input, ScimRequestContext.empty());

      assertThat(result).isSameAs(createdUser);
      verify(attributeMapper).toEntry(input, BASE_DN);
      verify(ldapDao).create(newEntry);
      verify(ldapDao).lookup("uid=alice," + BASE_DN);
      verify(attributeMapper).toScimUser(createdEntry);
    }

    @Test
    void resourceExceptionPropagates() throws Exception {
      ScimUser input = new ScimUser();
      input.setUserName("alice");

      Entry newEntry = new DefaultEntry("uid=alice," + BASE_DN);
      when(attributeMapper.toEntry(input, BASE_DN)).thenReturn(newEntry);
      when(ldapDao.lookup(anyString())).thenThrow(new ResourceException(409, "Conflict"));

      // ldapDao.create does not throw, but lookup does
      assertThatThrownBy(() -> repository.create(input, ScimRequestContext.empty()))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(409));
    }
  }

  // =========================================================================
  // update
  // =========================================================================

  @Nested
  @DisplayName("update")
  class UpdateTest {

    @Test
    void foundVerifiesChain() throws Exception {
      String id = "uuid-1";
      ScimUser resource = new ScimUser();
      resource.setUserName("alice");

      Entry existingEntry = new DefaultEntry("uid=alice," + BASE_DN);
      Entry updatedEntry = new DefaultEntry("uid=alice," + BASE_DN);
      Entry resultEntry = new DefaultEntry("uid=alice," + BASE_DN);
      ScimUser resultUser = new ScimUser();
      resultUser.setId(id);

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", id)).thenReturn(existingEntry);
      when(attributeMapper.toEntry(resource, BASE_DN)).thenReturn(updatedEntry);
      when(ldapDao.lookup("uid=alice," + BASE_DN)).thenReturn(resultEntry);
      when(attributeMapper.toScimUser(resultEntry)).thenReturn(resultUser);

      ScimUser result = repository.update(id, resource, ScimRequestContext.empty());

      assertThat(result).isSameAs(resultUser);
      verify(ldapDao).searchByAttribute(BASE_DN, "entryUUID", id);
      verify(attributeMapper).toEntry(resource, BASE_DN);
      verify(ldapDao).lookup("uid=alice," + BASE_DN);
      verify(attributeMapper).toScimUser(resultEntry);
    }

    @Test
    void notFoundThrowsResourceNotFoundException() throws Exception {
      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", "missing")).thenReturn(null);

      assertThatThrownBy(() -> repository.update("missing", new ScimUser(), ScimRequestContext.empty()))
        .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userNameChanged_callsRenameBeforeModify() throws Exception {
      String id = "uuid-1";
      ScimUser resource = new ScimUser();
      resource.setUserName("new");

      Entry existingEntry = new DefaultEntry("uid=old," + BASE_DN, "uid: old");
      Entry updatedEntry = new DefaultEntry("uid=new," + BASE_DN);
      Entry resultEntry = new DefaultEntry("uid=new," + BASE_DN);
      ScimUser resultUser = new ScimUser();
      resultUser.setId(id);

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", id)).thenReturn(existingEntry);
      when(attributeMapper.toEntry(resource, BASE_DN)).thenReturn(updatedEntry);
      when(ldapDao.lookup(anyString())).thenReturn(resultEntry);
      when(attributeMapper.toScimUser(resultEntry)).thenReturn(resultUser);

      repository.update(id, resource, ScimRequestContext.empty());

      verify(ldapDao).rename("uid=old," + BASE_DN, "uid=new");
    }

    @Test
    void userNameUnchanged_doesNotCallRename() throws Exception {
      String id = "uuid-1";
      ScimUser resource = new ScimUser();
      resource.setUserName("alice");

      Entry existingEntry = new DefaultEntry("uid=alice," + BASE_DN, "uid: alice");
      Entry updatedEntry = new DefaultEntry("uid=alice," + BASE_DN);
      Entry resultEntry = new DefaultEntry("uid=alice," + BASE_DN);
      ScimUser resultUser = new ScimUser();
      resultUser.setId(id);

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", id)).thenReturn(existingEntry);
      when(attributeMapper.toEntry(resource, BASE_DN)).thenReturn(updatedEntry);
      when(ldapDao.lookup("uid=alice," + BASE_DN)).thenReturn(resultEntry);
      when(attributeMapper.toScimUser(resultEntry)).thenReturn(resultUser);

      repository.update(id, resource, ScimRequestContext.empty());

      verify(ldapDao, never()).rename(anyString(), anyString());
    }

    @Test
    void userNameChanged_renameSucceeds_modifyFails_throwsResourceException() throws Exception {
      String id = "uuid-1";
      ScimUser resource = new ScimUser();
      resource.setUserName("new");

      Entry existingEntry = new DefaultEntry("uid=old," + BASE_DN, "uid: old");
      // Include a non-RDN, non-objectClass attr so buildReplaceModifications produces a modification
      Entry updatedEntry = new DefaultEntry("uid=new," + BASE_DN, "cn: New Name");

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", id)).thenReturn(existingEntry);
      when(attributeMapper.toEntry(resource, BASE_DN)).thenReturn(updatedEntry);
      doThrow(new ResourceException(500, "LDAP modify failed"))
        .when(ldapDao).modify(anyString(), any(Modification[].class));

      assertThatThrownBy(() -> repository.update(id, resource, ScimRequestContext.empty()))
        .isInstanceOf(ResourceException.class);
    }
  }

  // =========================================================================
  // delete
  // =========================================================================

  @Nested
  @DisplayName("delete")
  class DeleteTest {

    @Test
    void foundVerifiesDelete() throws Exception {
      String id = "uuid-1";
      Entry entry = new DefaultEntry("uid=alice," + BASE_DN);

      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", id)).thenReturn(entry);

      repository.delete(id);

      verify(ldapDao).searchByAttribute(BASE_DN, "entryUUID", id);
      verify(ldapDao).delete("uid=alice," + BASE_DN);
    }

    @Test
    void notFoundThrowsResourceNotFoundException() throws Exception {
      when(ldapDao.searchByAttribute(BASE_DN, "entryUUID", "missing")).thenReturn(null);

      assertThatThrownBy(() -> repository.delete("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
    }
  }
}
