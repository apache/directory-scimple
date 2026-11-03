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
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapGroupRepositoryTest {

  static final String GROUP_BASE_DN = "ou=groups,dc=example,dc=com";
  static final String USER_BASE_DN = "ou=users,dc=example,dc=com";

  LdapDao ldapDao = mock(LdapDao.class);
  AttributeMapper attributeMapper = mock(AttributeMapper.class);
  ScimLdapConfig config = mock(ScimLdapConfig.class);

  LdapGroupRepository repository;
  ScimRequestContext requestContext;

  @BeforeEach
  void setUp() {
    when(config.getGroupBaseDn()).thenReturn(GROUP_BASE_DN);
    when(config.getUserBaseDn()).thenReturn(USER_BASE_DN);
    when(attributeMapper.getGroupRdnAttribute()).thenReturn("cn");

    repository = new LdapGroupRepository(ldapDao, attributeMapper, config);
    requestContext = ScimRequestContext.empty();
  }

  // =========================================================================
  // Helper methods
  // =========================================================================

  private static Entry memberEntryWithMailAndUuid(String dn, String uuid, String mail) throws Exception {
    Entry entry = new DefaultEntry(dn);
    entry.add("entryUUID", uuid);
    entry.add("mail", mail);
    return entry;
  }

  private static Entry memberEntryWithCnAndUuid(String dn, String uuid, String cn) throws Exception {
    Entry entry = new DefaultEntry(dn);
    entry.add("entryUUID", uuid);
    entry.add("cn", cn);
    return entry;
  }

  private static ScimGroup scimGroupWithMembers(String displayName, List<GroupMembership> members) {
    ScimGroup group = new ScimGroup();
    group.setDisplayName(displayName);
    group.setMembers(members);
    return group;
  }

  private static GroupMembership membership(String value) {
    GroupMembership m = new GroupMembership();
    m.setValue(value);
    return m;
  }

  private static ScimGroup firstResource(FilterResponse<ScimGroup> response) {
    return response.getResources().iterator().next();
  }

  // =========================================================================
  // find
  // =========================================================================

  @Nested
  @DisplayName("find")
  class FindTest {

    @Test
    @DisplayName("null filter delegates to findGroups")
    void nullFilter_delegatesToFindGroups() throws Exception {
      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(Collections.emptyList(), 0));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);

      assertThat(response.getResources()).isEmpty();
      verify(ldapDao).findGroups(any(), any());
    }

    @Test
    @DisplayName("with filter delegates to findGroups")
    void withFilter_delegatesToFindGroups() throws Exception {
      Filter filter = mock(Filter.class);
      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(Collections.emptyList(), 0));

      repository.find(filter, requestContext);

      verify(ldapDao).findGroups(any(), any());
    }

    @Test
    @DisplayName("returns groups with resolved members")
    void returnsGroupsWithResolvedMembers() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      groupEntry.add("entryUUID", "group-uuid-1");

      ScimGroup mappedGroup = scimGroupWithMembers("admins",
        new ArrayList<>(List.of(membership("uid=jdoe,ou=users,dc=example,dc=com"))));
      mappedGroup.setId("group-uuid-1");
      when(attributeMapper.toScimGroup(groupEntry)).thenReturn(mappedGroup);

      Entry memberEntry = memberEntryWithMailAndUuid("uid=jdoe,ou=users,dc=example,dc=com", "member-uuid-1", "jdoe@example.com");
      when(ldapDao.lookup("uid=jdoe,ou=users,dc=example,dc=com")).thenReturn(memberEntry);

      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(List.of(groupEntry), 1));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);

      assertThat(response.getResources()).hasSize(1);
      ScimGroup result = firstResource(response);
      assertThat(result.getMembers()).hasSize(1);
      assertThat(result.getMembers().get(0).getValue()).isEqualTo("member-uuid-1");
      assertThat(result.getMembers().get(0).getDisplay()).isEqualTo("jdoe@example.com");
    }

    @Test
    @DisplayName("member resolution prefers mail for display")
    void memberResolutionPrefersMailForDisplay() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      ScimGroup mappedGroup = scimGroupWithMembers("admins",
        new ArrayList<>(List.of(membership("uid=jdoe,ou=users,dc=example,dc=com"))));
      when(attributeMapper.toScimGroup(groupEntry)).thenReturn(mappedGroup);

      Entry memberEntry = new DefaultEntry("uid=jdoe,ou=users,dc=example,dc=com");
      memberEntry.add("entryUUID", "member-uuid-1");
      memberEntry.add("mail", "jdoe@example.com");
      memberEntry.add("cn", "John Doe");
      when(ldapDao.lookup("uid=jdoe,ou=users,dc=example,dc=com")).thenReturn(memberEntry);

      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(List.of(groupEntry), 1));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);
      ScimGroup result = firstResource(response);
      assertThat(result.getMembers().get(0).getDisplay()).isEqualTo("jdoe@example.com");
    }

    @Test
    @DisplayName("member resolution falls back to cn when no mail")
    void memberResolutionFallsBackToCn() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      ScimGroup mappedGroup = scimGroupWithMembers("admins",
        new ArrayList<>(List.of(membership("uid=jdoe,ou=users,dc=example,dc=com"))));
      when(attributeMapper.toScimGroup(groupEntry)).thenReturn(mappedGroup);

      Entry memberEntry = memberEntryWithCnAndUuid("uid=jdoe,ou=users,dc=example,dc=com", "member-uuid-1", "John Doe");
      when(ldapDao.lookup("uid=jdoe,ou=users,dc=example,dc=com")).thenReturn(memberEntry);

      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(List.of(groupEntry), 1));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);
      ScimGroup result = firstResource(response);
      assertThat(result.getMembers().get(0).getDisplay()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("stale member reference is skipped")
    void staleMemberReference_memberSkipped() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      ScimGroup mappedGroup = scimGroupWithMembers("admins",
        new ArrayList<>(List.of(membership("uid=deleted,ou=users,dc=example,dc=com"))));
      when(attributeMapper.toScimGroup(groupEntry)).thenReturn(mappedGroup);

      when(ldapDao.lookup("uid=deleted,ou=users,dc=example,dc=com"))
        .thenThrow(new ResourceNotFoundException("uid=deleted,ou=users,dc=example,dc=com"));

      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(List.of(groupEntry), 1));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);
      ScimGroup result = firstResource(response);
      assertThat(result.getMembers()).isEmpty();
    }

    @Test
    @DisplayName("empty results returns empty FilterResponse")
    void emptyResults_returnsEmptyFilterResponse() throws Exception {
      when(ldapDao.findGroups(any(), any())).thenReturn(new FilterResponse<>(Collections.emptyList(), 0));

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);

      assertThat(response.getResources()).isEmpty();
      assertThat(response.getTotalResults()).isZero();
    }

    // ----- Pagination tests -----

    @Test
    @DisplayName("totalResults reflects total matching entries, not page size")
    void totalResults_reflectsTotalNotPageSize() throws Exception {
      Entry groupEntry1 = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      Entry groupEntry2 = new DefaultEntry("cn=devs," + GROUP_BASE_DN);

      ScimGroup group1 = new ScimGroup();
      group1.setDisplayName("admins");
      ScimGroup group2 = new ScimGroup();
      group2.setDisplayName("devs");

      when(ldapDao.findGroups(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(groupEntry1, groupEntry2), 3));
      when(attributeMapper.toScimGroup(groupEntry1)).thenReturn(group1);
      when(attributeMapper.toScimGroup(groupEntry2)).thenReturn(group2);

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(1).setCount(2));

      FilterResponse<ScimGroup> response = repository.find(null, context);

      assertThat(response.getResources()).containsExactly(group1, group2);
      assertThat(response.getTotalResults()).isEqualTo(3);
    }

    @Test
    @DisplayName("pagination middle page returns correct slice with total count")
    void paginationMiddlePage_returnsCorrectSlice() throws Exception {
      Entry groupEntry2 = new DefaultEntry("cn=devs," + GROUP_BASE_DN);

      ScimGroup group2 = new ScimGroup();
      group2.setDisplayName("devs");

      when(ldapDao.findGroups(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(groupEntry2), 3));
      when(attributeMapper.toScimGroup(groupEntry2)).thenReturn(group2);

      ScimRequestContext context = new ScimRequestContext()
        .setPageRequest(new PageRequest().setStartIndex(2).setCount(1));

      FilterResponse<ScimGroup> response = repository.find(null, context);

      assertThat(response.getResources()).containsExactly(group2);
      assertThat(response.getTotalResults()).isEqualTo(3);
    }

    @Test
    @DisplayName("no pagination returns all with correct totalResults")
    void noPagination_returnsAllWithCorrectTotalResults() throws Exception {
      Entry groupEntry1 = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      Entry groupEntry2 = new DefaultEntry("cn=devs," + GROUP_BASE_DN);

      ScimGroup group1 = new ScimGroup();
      group1.setDisplayName("admins");
      ScimGroup group2 = new ScimGroup();
      group2.setDisplayName("devs");

      when(ldapDao.findGroups(any(), any()))
        .thenReturn(new FilterResponse<>(List.of(groupEntry1, groupEntry2), 2));
      when(attributeMapper.toScimGroup(groupEntry1)).thenReturn(group1);
      when(attributeMapper.toScimGroup(groupEntry2)).thenReturn(group2);

      FilterResponse<ScimGroup> response = repository.find(null, requestContext);

      assertThat(response.getResources()).containsExactly(group1, group2);
      assertThat(response.getTotalResults()).isEqualTo(2);
    }
  }

  // =========================================================================
  // get
  // =========================================================================

  @Nested
  @DisplayName("get")
  class GetTest {

    @Test
    @DisplayName("found returns ScimGroup with resolved members")
    void found_returnsScimGroupWithResolvedMembers() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      groupEntry.add("entryUUID", "group-uuid-1");

      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "group-uuid-1"))
        .thenReturn(groupEntry);

      ScimGroup mappedGroup = scimGroupWithMembers("admins",
        new ArrayList<>(List.of(membership("uid=jdoe,ou=users,dc=example,dc=com"))));
      mappedGroup.setId("group-uuid-1");
      when(attributeMapper.toScimGroup(groupEntry)).thenReturn(mappedGroup);

      Entry memberEntry = memberEntryWithMailAndUuid("uid=jdoe,ou=users,dc=example,dc=com", "member-uuid-1", "jdoe@example.com");
      when(ldapDao.lookup("uid=jdoe,ou=users,dc=example,dc=com")).thenReturn(memberEntry);

      ScimGroup result = repository.get("group-uuid-1", requestContext);

      assertThat(result).isNotNull();
      assertThat(result.getDisplayName()).isEqualTo("admins");
      assertThat(result.getMembers()).hasSize(1);
      assertThat(result.getMembers().get(0).getValue()).isEqualTo("member-uuid-1");
      assertThat(result.getMembers().get(0).getDisplay()).isEqualTo("jdoe@example.com");
    }

    @Test
    @DisplayName("not found returns null")
    void notFound_returnsNull() throws Exception {
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "missing-uuid"))
        .thenReturn(null);

      ScimGroup result = repository.get("missing-uuid", requestContext);

      assertThat(result).isNull();
    }
  }

  // =========================================================================
  // create
  // =========================================================================

  @Nested
  @DisplayName("create")
  class CreateTest {

    @Test
    @DisplayName("success with member UUID resolution")
    void successWithMemberUuidResolution() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      GroupMembership member = membership("member-uuid-1");
      resource.setMembers(new ArrayList<>(List.of(member)));

      // Resolve member UUID to DN
      Entry memberEntry = new DefaultEntry("uid=jdoe,ou=users,dc=example,dc=com");
      memberEntry.add("entryUUID", "member-uuid-1");
      when(ldapDao.searchByAttribute(USER_BASE_DN, "entryUUID", "member-uuid-1"))
        .thenReturn(memberEntry);

      Entry ldapEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(ldapEntry);

      Entry createdEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      createdEntry.add("entryUUID", "new-group-uuid");
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(createdEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId("new-group-uuid");
      mappedResult.setDisplayName("admins");
      when(attributeMapper.toScimGroup(createdEntry)).thenReturn(mappedResult);

      ScimGroup result = repository.create(resource, requestContext);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo("new-group-uuid");
      verify(ldapDao).create(ldapEntry);
      verify(ldapDao).lookup("cn=admins," + GROUP_BASE_DN);
    }

    @Test
    @DisplayName("member not found throws ResourceException 400")
    void memberNotFound_throwsResourceException400() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      resource.setMembers(new ArrayList<>(List.of(membership("nonexistent-uuid"))));

      when(ldapDao.searchByAttribute(USER_BASE_DN, "entryUUID", "nonexistent-uuid"))
        .thenReturn(null);
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "nonexistent-uuid"))
        .thenReturn(null);

      assertThatThrownBy(() -> repository.create(resource, requestContext))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("member with DN value (contains =) is validated and passed through")
    void memberWithDnValue_validatedAndPassedThrough() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      String memberDn = "uid=jdoe,ou=users,dc=example,dc=com";
      resource.setMembers(new ArrayList<>(List.of(membership(memberDn))));

      Entry ldapEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(ldapEntry);

      Entry createdEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      createdEntry.add("entryUUID", "new-group-uuid");
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(createdEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId("new-group-uuid");
      mappedResult.setDisplayName("admins");
      when(attributeMapper.toScimGroup(createdEntry)).thenReturn(mappedResult);

      ScimGroup result = repository.create(resource, requestContext);

      assertThat(result).isNotNull();
      // Verify the member DN was kept (not looked up as UUID)
      verify(ldapDao, never()).searchByAttribute(anyString(), eq("entryUUID"), eq(memberDn));
    }

    @Test
    @DisplayName("invalid DN throws ResourceException 400")
    void invalidDn_throwsResourceException400() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      resource.setMembers(new ArrayList<>(List.of(membership("not=a=valid=dn=at=all"))));

      assertThatThrownBy(() -> repository.create(resource, requestContext))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("DN not under known base throws ResourceException 400")
    void dnNotUnderKnownBase_throwsResourceException400() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      resource.setMembers(new ArrayList<>(List.of(membership("uid=jdoe,ou=unknown,dc=other,dc=com"))));

      assertThatThrownBy(() -> repository.create(resource, requestContext))
        .isInstanceOf(ResourceException.class)
        .satisfies(ex -> assertThat(((ResourceException) ex).getStatus()).isEqualTo(400));
    }
  }

  // =========================================================================
  // update
  // =========================================================================

  @Nested
  @DisplayName("update")
  class UpdateTest {

    @Test
    @DisplayName("found resolves members, modifies, and returns")
    void found_resolvesMembers_modifies_returns() throws Exception {
      String groupId = "group-uuid-1";
      Entry existingEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      existingEntry.add("entryUUID", groupId);
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", groupId)).thenReturn(existingEntry);

      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      resource.setMembers(new ArrayList<>(List.of(membership("uid=jdoe,ou=users,dc=example,dc=com"))));

      Entry updatedEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      updatedEntry.add("member", "uid=jdoe,ou=users,dc=example,dc=com");
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(updatedEntry);

      Entry resultEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      resultEntry.add("entryUUID", groupId);
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(resultEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId(groupId);
      mappedResult.setDisplayName("admins");
      when(attributeMapper.toScimGroup(resultEntry)).thenReturn(mappedResult);

      ScimGroup result = repository.update(groupId, resource, requestContext);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(groupId);
    }

    @Test
    @DisplayName("not found throws ResourceNotFoundException")
    void notFound_throwsResourceNotFoundException() throws Exception {
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "missing-uuid"))
        .thenReturn(null);

      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");

      assertThatThrownBy(() -> repository.update("missing-uuid", resource, requestContext))
        .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("displayName changed calls rename before modify")
    void displayNameChanged_callsRenameBeforeModify() throws Exception {
      String groupId = "group-uuid-1";
      Entry existingEntry = new DefaultEntry("cn=old," + GROUP_BASE_DN, "cn: old");
      existingEntry.add("entryUUID", groupId);
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", groupId)).thenReturn(existingEntry);

      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("new");

      Entry updatedEntry = new DefaultEntry("cn=new," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(updatedEntry);

      Entry resultEntry = new DefaultEntry("cn=new," + GROUP_BASE_DN);
      resultEntry.add("entryUUID", groupId);
      when(ldapDao.lookup(anyString())).thenReturn(resultEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId(groupId);
      mappedResult.setDisplayName("new");
      when(attributeMapper.toScimGroup(resultEntry)).thenReturn(mappedResult);

      repository.update(groupId, resource, requestContext);

      verify(ldapDao).rename("cn=old," + GROUP_BASE_DN, "cn=new");
    }

    @Test
    @DisplayName("displayName unchanged does not call rename")
    void displayNameUnchanged_doesNotCallRename() throws Exception {
      String groupId = "group-uuid-1";
      Entry existingEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN, "cn: admins");
      existingEntry.add("entryUUID", groupId);
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", groupId)).thenReturn(existingEntry);

      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");

      Entry updatedEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(updatedEntry);

      Entry resultEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      resultEntry.add("entryUUID", groupId);
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(resultEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId(groupId);
      mappedResult.setDisplayName("admins");
      when(attributeMapper.toScimGroup(resultEntry)).thenReturn(mappedResult);

      repository.update(groupId, resource, requestContext);

      verify(ldapDao, never()).rename(anyString(), anyString());
    }

    @Test
    @DisplayName("displayName changed, rename succeeds, modify fails — throws ResourceException")
    void displayNameChanged_renameSucceeds_modifyFails_throwsResourceException() throws Exception {
      String groupId = "group-uuid-1";
      Entry existingEntry = new DefaultEntry("cn=old," + GROUP_BASE_DN, "cn: old");
      existingEntry.add("entryUUID", groupId);
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", groupId)).thenReturn(existingEntry);

      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("new");

      // Include a member attribute so buildReplaceModifications produces a modification
      Entry updatedEntry = new DefaultEntry("cn=new," + GROUP_BASE_DN);
      updatedEntry.add("member", "uid=jdoe,ou=users,dc=example,dc=com");
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(updatedEntry);

      doThrow(new ResourceException(500, "LDAP modify failed"))
        .when(ldapDao).modify(anyString(), any(Modification[].class));

      assertThatThrownBy(() -> repository.update(groupId, resource, requestContext))
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
    @DisplayName("found deletes by DN")
    void found_deletesByDn() throws Exception {
      Entry groupEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      groupEntry.add("entryUUID", "group-uuid-1");
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "group-uuid-1"))
        .thenReturn(groupEntry);

      repository.delete("group-uuid-1");

      verify(ldapDao).delete("cn=admins," + GROUP_BASE_DN);
    }

    @Test
    @DisplayName("not found throws ResourceNotFoundException")
    void notFound_throwsResourceNotFoundException() throws Exception {
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "missing-uuid"))
        .thenReturn(null);

      assertThatThrownBy(() -> repository.delete("missing-uuid"))
        .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // =========================================================================
  // Member resolution details
  // =========================================================================

  @Nested
  @DisplayName("member resolution details")
  class MemberResolutionTest {

    @Test
    @DisplayName("UUID member searched in users first then groups")
    void uuidMember_searchedInUsersFirstThenGroups() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      resource.setMembers(new ArrayList<>(List.of(membership("some-uuid"))));

      // Not found in users
      when(ldapDao.searchByAttribute(USER_BASE_DN, "entryUUID", "some-uuid"))
        .thenReturn(null);
      // Found in groups
      Entry groupMemberEntry = new DefaultEntry("cn=subgroup," + GROUP_BASE_DN);
      groupMemberEntry.add("entryUUID", "some-uuid");
      when(ldapDao.searchByAttribute(GROUP_BASE_DN, "entryUUID", "some-uuid"))
        .thenReturn(groupMemberEntry);

      Entry ldapEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(ldapEntry);

      Entry createdEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      createdEntry.add("entryUUID", "new-group-uuid");
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(createdEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId("new-group-uuid");
      when(attributeMapper.toScimGroup(createdEntry)).thenReturn(mappedResult);

      repository.create(resource, requestContext);

      // Verify users searched first
      verify(ldapDao).searchByAttribute(USER_BASE_DN, "entryUUID", "some-uuid");
      // Then groups searched
      verify(ldapDao).searchByAttribute(GROUP_BASE_DN, "entryUUID", "some-uuid");
    }

    @Test
    @DisplayName("null member value is skipped")
    void nullMemberValue_skipped() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      GroupMembership nullMember = new GroupMembership();
      // value is null by default
      resource.setMembers(new ArrayList<>(List.of(nullMember)));

      Entry ldapEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(ldapEntry);

      Entry createdEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      createdEntry.add("entryUUID", "new-group-uuid");
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(createdEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId("new-group-uuid");
      when(attributeMapper.toScimGroup(createdEntry)).thenReturn(mappedResult);

      ScimGroup result = repository.create(resource, requestContext);

      assertThat(result).isNotNull();
      // No searchByAttribute should be called for null member
      verify(ldapDao, never()).searchByAttribute(anyString(), eq("entryUUID"), any());
    }

    @Test
    @DisplayName("member already a DN (contains =) is validated via Dn and base DN check")
    void memberAlreadyDn_validatedViaDnAndBaseDnCheck() throws Exception {
      ScimGroup resource = new ScimGroup();
      resource.setDisplayName("admins");
      String memberDn = "cn=subgroup,ou=groups,dc=example,dc=com";
      resource.setMembers(new ArrayList<>(List.of(membership(memberDn))));

      Entry ldapEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      when(attributeMapper.toEntry(any(ScimGroup.class), eq(GROUP_BASE_DN))).thenReturn(ldapEntry);

      Entry createdEntry = new DefaultEntry("cn=admins," + GROUP_BASE_DN);
      createdEntry.add("entryUUID", "new-group-uuid");
      when(ldapDao.lookup("cn=admins," + GROUP_BASE_DN)).thenReturn(createdEntry);

      ScimGroup mappedResult = new ScimGroup();
      mappedResult.setId("new-group-uuid");
      when(attributeMapper.toScimGroup(createdEntry)).thenReturn(mappedResult);

      ScimGroup result = repository.create(resource, requestContext);

      assertThat(result).isNotNull();
      // The DN member should not trigger UUID resolution
      verify(ldapDao, never()).searchByAttribute(anyString(), eq("entryUUID"), eq(memberDn));
    }
  }
}
