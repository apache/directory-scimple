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

package org.apache.directory.scim.ldap.mapping;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Meta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttributeMapperTest {

  static final String USER_BASE_DN = "ou=users,dc=example,dc=com";
  static final String GROUP_BASE_DN = "ou=groups,dc=example,dc=com";

  AttributeMapper mapper;

  @BeforeEach
  void setUp() throws Exception {
    ScimLdapConfig config = mock(ScimLdapConfig.class);

    when(config.getUserObjectClasses()).thenReturn(
      List.of("inetOrgPerson", "organizationalPerson", "person", "top"));
    when(config.getUserRdnAttribute()).thenReturn("uid");
    when(config.getUserAttributes()).thenReturn(defaultUserAttributes());

    when(config.getGroupObjectClasses()).thenReturn(List.of("groupOfNames", "top"));
    when(config.getGroupRdnAttribute()).thenReturn("cn");
    when(config.getGroupAttributes()).thenReturn(defaultGroupAttributes());

    mapper = AttributeMapper.class.getDeclaredConstructor().newInstance();

    Field propertiesField = AttributeMapper.class.getDeclaredField("properties");
    propertiesField.setAccessible(true);
    propertiesField.set(mapper, config);

    Method initMethod = AttributeMapper.class.getDeclaredMethod("init");
    initMethod.setAccessible(true);
    initMethod.invoke(mapper);
  }

  // ── toScimUser ──────────────────────────────────────────────────────

  @Test
  void toScimUser_allFieldsPopulated() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "entryUUID: 550e8400-e29b-41d4-a716-446655440000",
      "uid: jdoe",
      "givenName: John",
      "sn: Doe",
      "cn: John Doe",
      "displayName: Johnny Doe",
      "mail: john@example.com",
      "telephoneNumber: +1-555-1234",
      "street: 123 Main St",
      "l: Springfield",
      "postalCode: 62701",
      "title: Engineer",
      "employeeType: Employee",
      "scimActive: true"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    assertThat(user.getUserName()).isEqualTo("jdoe");
    assertThat(user.getName()).isNotNull();
    assertThat(user.getName().getGivenName()).isEqualTo("John");
    assertThat(user.getName().getFamilyName()).isEqualTo("Doe");
    assertThat(user.getName().getFormatted()).isEqualTo("John Doe");
    assertThat(user.getDisplayName()).isEqualTo("Johnny Doe");
    assertThat(user.getEmails()).hasSize(1);
    assertThat(user.getEmails().get(0).getValue()).isEqualTo("john@example.com");
    assertThat(user.getPhoneNumbers()).hasSize(1);
    assertThat(user.getPhoneNumbers().get(0).getValue()).isEqualTo("+1-555-1234");
    assertThat(user.getAddresses()).hasSize(1);
    assertThat(user.getAddresses().get(0).getStreetAddress()).isEqualTo("123 Main St");
    assertThat(user.getAddresses().get(0).getLocality()).isEqualTo("Springfield");
    assertThat(user.getAddresses().get(0).getPostalCode()).isEqualTo("62701");
    assertThat(user.getTitle()).isEqualTo("Engineer");
    assertThat(user.getUserType()).isEqualTo("Employee");
    assertThat(user.getActive()).isTrue();
  }

  @Test
  void toScimUser_minimalEntry_onlyUidAndEntryUuid() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "entryUUID: abc-123",
      "uid: jdoe"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getId()).isEqualTo("abc-123");
    assertThat(user.getUserName()).isEqualTo("jdoe");
    assertThat(user.getName()).isNull();
    assertThat(user.getDisplayName()).isNull();
    assertThat(user.getEmails()).isNull();
    assertThat(user.getPhoneNumbers()).isNull();
    assertThat(user.getAddresses()).isNull();
    assertThat(user.getTitle()).isNull();
    assertThat(user.getUserType()).isNull();
    assertThat(user.getActive()).isTrue();
  }

  @Test
  void toScimUser_noEntryUuid_idIsNull() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getId()).isNull();
    assertThat(user.getUserName()).isEqualTo("jdoe");
  }

  @Test
  void toScimUser_multipleEmails_firstIsPrimary() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "mail: primary@example.com",
      "mail: secondary@example.com"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getEmails()).hasSize(2);
    assertThat(user.getEmails().get(0).getValue()).isEqualTo("primary@example.com");
    assertThat(user.getEmails().get(0).getPrimary()).isTrue();
    assertThat(user.getEmails().get(1).getPrimary()).isFalse();
  }

  @Test
  void toScimUser_multiplePhones_firstIsPrimary() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "telephoneNumber: +1-555-0001",
      "telephoneNumber: +1-555-0002"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getPhoneNumbers()).hasSize(2);
    assertThat(user.getPhoneNumbers().get(0).getPrimary()).isTrue();
    assertThat(user.getPhoneNumbers().get(1).getPrimary()).isFalse();
  }

  @Test
  void toScimUser_phoneTypesFromScimPhoneTypes() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "telephoneNumber: +1-555-0001",
      "telephoneNumber: +1-555-0002",
      "scimPhoneTypes: work,mobile"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getPhoneNumbers()).hasSize(2);
    assertThat(user.getPhoneNumbers().get(0).getType()).isEqualTo("work");
    assertThat(user.getPhoneNumbers().get(1).getType()).isEqualTo("mobile");
  }

  @Test
  void toScimUser_noScimActive_defaultsToTrue() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getActive()).isTrue();
  }

  @Test
  void toScimUser_scimActiveFalse_activeIsFalse() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "scimActive: false"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getActive()).isFalse();
  }

  @Test
  void toScimUser_withBothTimestamps_populatesMetaVersionAndDates() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "createTimestamp: 20240101120000Z",
      "modifyTimestamp: 20240115143000Z"
    );

    ScimUser user = mapper.toScimUser(entry);

    Meta meta = user.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getResourceType()).isEqualTo("User");
    assertThat(meta.getVersion()).isEqualTo("W/\"20240115143000Z\"");
    assertThat(meta.getCreated()).isNotNull();
    assertThat(meta.getLastModified()).isNotNull();
  }

  @Test
  void toScimUser_onlyCreateTimestamp_versionUsesCreateTimestamp() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "createTimestamp: 20240101120000Z"
    );

    ScimUser user = mapper.toScimUser(entry);

    Meta meta = user.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getVersion()).isEqualTo("W/\"20240101120000Z\"");
    assertThat(meta.getCreated()).isNotNull();
    assertThat(meta.getLastModified()).isNull();
  }

  @Test
  void toScimUser_noTimestamps_metaHasResourceTypeOnly() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe"
    );

    ScimUser user = mapper.toScimUser(entry);

    Meta meta = user.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getResourceType()).isEqualTo("User");
    assertThat(meta.getVersion()).isNull();
    assertThat(meta.getCreated()).isNull();
    assertThat(meta.getLastModified()).isNull();
  }

  // ── toEntry(ScimUser, baseDn) ───────────────────────────────────────

  @Test
  void toEntryUser_constructsDnCorrectly() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.getDn().toString()).isEqualTo("uid=jdoe,ou=users,dc=example,dc=com");
  }

  @Test
  void toEntryUser_setsObjectClasses() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("objectClass")).isNotNull();
    assertThat(entry.get("objectClass").size()).isEqualTo(4);
  }

  @Test
  void toEntryUser_setsAllMappedAttributes() throws Exception {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");
    user.setDisplayName("John Doe");
    user.setName(new Name());
    user.getName().setGivenName("John");
    user.getName().setFamilyName("Doe");
    user.getName().setFormatted("John Doe");
    user.setTitle("Engineer");
    user.setUserType("Employee");
    user.setActive(false);

    Email email = new Email();
    email.setValue("john@example.com");
    user.setEmails(List.of(email));

    PhoneNumber phone = new PhoneNumber();
    phone.setValue("555-1234");
    phone.setType("work");
    user.setPhoneNumbers(List.of(phone));

    Address address = new Address();
    address.setStreetAddress("123 Main St");
    address.setLocality("Springfield");
    address.setPostalCode("62701");
    user.setAddresses(List.of(address));

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("uid").getString()).isEqualTo("jdoe");
    assertThat(entry.get("givenName").getString()).isEqualTo("John");
    assertThat(entry.get("sn").getString()).isEqualTo("Doe");
    assertThat(entry.get("cn").getString()).isEqualTo("John Doe");
    assertThat(entry.get("displayName").getString()).isEqualTo("John Doe");
    assertThat(entry.get("mail").getString()).isEqualTo("john@example.com");
    assertThat(entry.get("telephoneNumber").getString()).isEqualTo("555-1234");
    assertThat(entry.get("street").getString()).isEqualTo("123 Main St");
    assertThat(entry.get("l").getString()).isEqualTo("Springfield");
    assertThat(entry.get("postalCode").getString()).isEqualTo("62701");
    assertThat(entry.get("title").getString()).isEqualTo("Engineer");
    assertThat(entry.get("employeeType").getString()).isEqualTo("Employee");
    assertThat(entry.get("scimActive").getString()).isEqualTo("false");
  }

  @Test
  void toEntryUser_defaultsSnToUserName_whenFamilyNameMissing() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("sn").getString()).isEqualTo("jdoe");
  }

  @Test
  void toEntryUser_defaultsCnToDisplayName_thenUserName() throws LdapException {
    // When displayName is set, cn should be the displayName
    ScimUser userWithDisplay = new ScimUser();
    userWithDisplay.setUserName("jdoe");
    userWithDisplay.setDisplayName("John Doe");

    Entry entryWithDisplay = mapper.toEntry(userWithDisplay, USER_BASE_DN);
    // cn is set as the name.formatted mapping AND as the defaulted cn (displayName takes precedence)
    assertThat(entryWithDisplay.get("cn").getString()).isEqualTo("John Doe");

    // When displayName is null, cn should default to userName
    ScimUser userNoDisplay = new ScimUser();
    userNoDisplay.setUserName("jdoe");

    Entry entryNoDisplay = mapper.toEntry(userNoDisplay, USER_BASE_DN);
    assertThat(entryNoDisplay.get("cn").getString()).isEqualTo("jdoe");
  }

  @Test
  void toEntryUser_nullUserName_throwsIllegalArgument() {
    ScimUser user = new ScimUser();
    user.setUserName(null);

    assertThatThrownBy(() -> mapper.toEntry(user, USER_BASE_DN))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("userName is required");
  }

  @Test
  void toEntryUser_blankUserName_throwsIllegalArgument() {
    ScimUser user = new ScimUser();
    user.setUserName("   ");

    assertThatThrownBy(() -> mapper.toEntry(user, USER_BASE_DN))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("userName is required");
  }

  // ── toScimGroup ─────────────────────────────────────────────────────

  @Test
  void toScimGroup_allFieldsPopulated() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=admins,ou=groups,dc=example,dc=com",
      "entryUUID: group-uuid-1",
      "cn: Admins",
      "member: uid=jdoe,ou=users,dc=example,dc=com",
      "member: uid=asmith,ou=users,dc=example,dc=com"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    assertThat(group.getId()).isEqualTo("group-uuid-1");
    assertThat(group.getDisplayName()).isEqualTo("Admins");
    assertThat(group.getMembers()).hasSize(2);
    assertThat(group.getMembers().get(0).getValue())
      .isEqualTo("uid=jdoe,ou=users,dc=example,dc=com");
    assertThat(group.getMembers().get(1).getValue())
      .isEqualTo("uid=asmith,ou=users,dc=example,dc=com");
  }

  @Test
  void toScimGroup_blankMemberPlaceholder_isSkipped() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=empty,ou=groups,dc=example,dc=com",
      "entryUUID: group-uuid-2",
      "cn: Empty Group",
      "member: ",
      "member: uid=jdoe,ou=users,dc=example,dc=com"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    assertThat(group.getMembers()).hasSize(1);
    assertThat(group.getMembers().get(0).getValue())
      .isEqualTo("uid=jdoe,ou=users,dc=example,dc=com");
  }

  @Test
  void toScimGroup_noMembers_memberListIsNull() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=lonely,ou=groups,dc=example,dc=com",
      "entryUUID: group-uuid-3",
      "cn: Lonely Group"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    assertThat(group.getMembers()).isNull();
  }

  // ── toEntry(ScimGroup, baseDn) ──────────────────────────────────────

  @Test
  void toEntryGroup_constructsDnCorrectly() throws LdapException {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("Admins");

    Entry entry = mapper.toEntry(group, GROUP_BASE_DN);

    assertThat(entry.getDn().toString()).isEqualTo("cn=Admins,ou=groups,dc=example,dc=com");
  }

  @Test
  void toEntryGroup_membersSet() throws LdapException {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("Admins");

    GroupMembership m1 = new GroupMembership();
    m1.setValue("uid=jdoe,ou=users,dc=example,dc=com");
    GroupMembership m2 = new GroupMembership();
    m2.setValue("uid=asmith,ou=users,dc=example,dc=com");
    group.setMembers(List.of(m1, m2));

    Entry entry = mapper.toEntry(group, GROUP_BASE_DN);

    assertThat(entry.get("member")).isNotNull();
    assertThat(entry.get("member").size()).isEqualTo(2);
  }

  @Test
  void toEntryGroup_noMembers_emptyPlaceholderAdded() throws LdapException {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("Empty");

    Entry entry = mapper.toEntry(group, GROUP_BASE_DN);

    assertThat(entry.get("member")).isNotNull();
    assertThat(entry.get("member").getString()).isEmpty();
  }

  @Test
  void toEntryGroup_nullDisplayName_throwsIllegalArgument() {
    ScimGroup group = new ScimGroup();
    group.setDisplayName(null);

    assertThatThrownBy(() -> mapper.toEntry(group, GROUP_BASE_DN))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("displayName is required");
  }

  @Test
  void toEntryGroup_blankDisplayName_throwsIllegalArgument() {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("   ");

    assertThatThrownBy(() -> mapper.toEntry(group, GROUP_BASE_DN))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("displayName is required");
  }

  // ── getLdapUserAttribute ────────────────────────────────────────────

  @Test
  void getLdapUserAttribute_mappedAttribute_returnsLdapName() {
    assertThat(mapper.getLdapUserAttribute("userName")).isEqualTo("uid");
  }

  @Test
  void getLdapUserAttribute_subAttribute_returnsLdapName() {
    assertThat(mapper.getLdapUserAttribute("name.givenName")).isEqualTo("givenName");
  }

  @Test
  void getLdapUserAttribute_unmappedAttribute_returnsNull() {
    assertThat(mapper.getLdapUserAttribute("nonExistent")).isNull();
  }

  // ── getLdapGroupAttribute ───────────────────────────────────────────

  @Test
  void getLdapGroupAttribute_mappedAttribute_returnsLdapName() {
    assertThat(mapper.getLdapGroupAttribute("displayName")).isEqualTo("cn");
  }

  @Test
  void getLdapGroupAttribute_membersValue_returnsMember() {
    assertThat(mapper.getLdapGroupAttribute("members.value")).isEqualTo("member");
  }

  @Test
  void getLdapGroupAttribute_unmappedAttribute_returnsNull() {
    assertThat(mapper.getLdapGroupAttribute("nonExistent")).isNull();
  }

  // ── Getters ─────────────────────────────────────────────────────────

  @Test
  void getUserRdnAttribute_returnsUid() {
    assertThat(mapper.getUserRdnAttribute()).isEqualTo("uid");
  }

  @Test
  void getGroupRdnAttribute_returnsCn() {
    assertThat(mapper.getGroupRdnAttribute()).isEqualTo("cn");
  }

  @Test
  void getUserObjectClasses_returnsConfiguredList() {
    assertThat(mapper.getUserObjectClasses())
      .containsExactly("inetOrgPerson", "organizationalPerson", "person", "top");
  }

  @Test
  void getGroupObjectClasses_returnsConfiguredList() {
    assertThat(mapper.getGroupObjectClasses())
      .containsExactly("groupOfNames", "top");
  }

  // ── Additional edge cases ───────────────────────────────────────────

  @Test
  void toScimUser_partialName_onlyGivenName() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "givenName: John"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getName()).isNotNull();
    assertThat(user.getName().getGivenName()).isEqualTo("John");
    assertThat(user.getName().getFamilyName()).isNull();
    assertThat(user.getName().getFormatted()).isNull();
  }

  @Test
  void toScimUser_partialAddress_onlyStreet() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "uid=jdoe,ou=users,dc=example,dc=com",
      "uid: jdoe",
      "street: 456 Oak Ave"
    );

    ScimUser user = mapper.toScimUser(entry);

    assertThat(user.getAddresses()).hasSize(1);
    assertThat(user.getAddresses().get(0).getStreetAddress()).isEqualTo("456 Oak Ave");
    assertThat(user.getAddresses().get(0).getLocality()).isNull();
    assertThat(user.getAddresses().get(0).getPostalCode()).isNull();
  }

  @Test
  void toEntryUser_setsPhoneTypeMetadata() throws Exception {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");

    PhoneNumber workPhone = new PhoneNumber();
    workPhone.setValue("555-0001");
    workPhone.setType("work");

    PhoneNumber mobilePhone = new PhoneNumber();
    mobilePhone.setValue("555-0002");
    mobilePhone.setType("mobile");

    user.setPhoneNumbers(List.of(workPhone, mobilePhone));

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("scimPhoneTypes").getString()).isEqualTo("work,mobile");
  }

  @Test
  void toEntryUser_setsPassword() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");
    user.setPassword("s3cret");

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("userPassword").getString()).isEqualTo("s3cret");
  }

  @Test
  void toEntryGroup_setsObjectClasses() throws LdapException {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("Developers");

    Entry entry = mapper.toEntry(group, GROUP_BASE_DN);

    assertThat(entry.get("objectClass")).isNotNull();
    assertThat(entry.get("objectClass").size()).isEqualTo(2);
  }

  @Test
  void toEntryGroup_setsDisplayNameAttribute() throws LdapException {
    ScimGroup group = new ScimGroup();
    group.setDisplayName("Developers");

    Entry entry = mapper.toEntry(group, GROUP_BASE_DN);

    assertThat(entry.get("cn").getString()).isEqualTo("Developers");
  }

  @Test
  void toScimGroup_noEntryUuid_idIsNull() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=team,ou=groups,dc=example,dc=com",
      "cn: Team"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    assertThat(group.getId()).isNull();
    assertThat(group.getDisplayName()).isEqualTo("Team");
  }

  @Test
  void toScimGroup_withBothTimestamps_populatesMetaVersionAndDates() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=admins,ou=groups,dc=example,dc=com",
      "cn: Admins",
      "createTimestamp: 20240101120000Z",
      "modifyTimestamp: 20240115143000Z"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    Meta meta = group.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getResourceType()).isEqualTo("Group");
    assertThat(meta.getVersion()).isEqualTo("W/\"20240115143000Z\"");
    assertThat(meta.getCreated()).isNotNull();
    assertThat(meta.getLastModified()).isNotNull();
  }

  @Test
  void toScimGroup_onlyCreateTimestamp_versionUsesCreateTimestamp() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=admins,ou=groups,dc=example,dc=com",
      "cn: Admins",
      "createTimestamp: 20240101120000Z"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    Meta meta = group.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getVersion()).isEqualTo("W/\"20240101120000Z\"");
    assertThat(meta.getCreated()).isNotNull();
    assertThat(meta.getLastModified()).isNull();
  }

  @Test
  void toScimGroup_noTimestamps_metaHasResourceTypeOnly() throws LdapInvalidAttributeValueException, LdapException {
    Entry entry = new DefaultEntry(
      "cn=admins,ou=groups,dc=example,dc=com",
      "cn: Admins"
    );

    ScimGroup group = mapper.toScimGroup(entry);

    Meta meta = group.getMeta();
    assertThat(meta).isNotNull();
    assertThat(meta.getResourceType()).isEqualTo("Group");
    assertThat(meta.getVersion()).isNull();
    assertThat(meta.getCreated()).isNull();
    assertThat(meta.getLastModified()).isNull();
  }

  @Test
  void toEntryUser_activeFlagStoredAsCustomAttribute() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");
    user.setActive(true);

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("scimActive").getString()).isEqualTo("true");
  }

  @Test
  void toEntryUser_familyNameSet_snUsesFamilyName() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");
    Name name = new Name();
    name.setFamilyName("Doe");
    user.setName(name);

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    assertThat(entry.get("sn").getString()).isEqualTo("Doe");
  }

  @Test
  void toEntryUser_formattedNameSet_cnUsesFormatted() throws LdapException {
    ScimUser user = new ScimUser();
    user.setUserName("jdoe");
    Name name = new Name();
    name.setFormatted("John Doe");
    user.setName(name);

    Entry entry = mapper.toEntry(user, USER_BASE_DN);

    // cn is set from the name.formatted mapping (which maps to "cn")
    assertThat(entry.get("cn").getString()).isEqualTo("John Doe");
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  private static Map<String, String> defaultUserAttributes() {
    Map<String, String> attrs = new LinkedHashMap<>();
    attrs.put("userName", "uid");
    attrs.put("name.givenName", "givenName");
    attrs.put("name.familyName", "sn");
    attrs.put("name.formatted", "cn");
    attrs.put("displayName", "displayName");
    attrs.put("emails.value", "mail");
    attrs.put("phoneNumbers.value", "telephoneNumber");
    attrs.put("addresses.streetAddress", "street");
    attrs.put("addresses.locality", "l");
    attrs.put("addresses.postalCode", "postalCode");
    attrs.put("title", "title");
    attrs.put("userType", "employeeType");
    attrs.put("password", "userPassword");
    return attrs;
  }

  private static Map<String, String> defaultGroupAttributes() {
    Map<String, String> attrs = new LinkedHashMap<>();
    attrs.put("displayName", "cn");
    attrs.put("members.value", "member");
    return attrs;
  }
}
