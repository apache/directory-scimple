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

package org.apache.directory.scim.core.repository;

import lombok.SneakyThrows;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperation.Type;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.Test;

import static org.apache.directory.scim.spec.patch.PatchOperation.Type.*;
import static java.util.Map.entry;
import static org.apache.directory.scim.test.assertj.ScimpleAssertions.scimAssertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatchHandlerTest {

  DefaultPatchHandler patchHandler;

  public PatchHandlerTest() {
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    schemaRegistry.addSchema(ScimUser.class, List.of(EnterpriseExtension.class));
    schemaRegistry.addSchema(ScimGroup.class, null);
    this.patchHandler = new DefaultPatchHandler(schemaRegistry);
  }

  @Test
  public void applyReplaceComplexAttribute() {
    // setup existing user
    Name existingName = new Name();
    existingName.setFamilyName("Family Name");
    existingName.setGivenName("Given Name");
    ScimUser user = user();
    user.setName(existingName);

    // setup patch ops
    Map<String, String> newName = Map.of("familyName", "New Family Name");
    PatchOperation op = patchOperation(REPLACE, "name", newName);

    //execute
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getName().getFamilyName()).isEqualTo("New Family Name");
    // assert that PATCH did not update fields not provided in PatchOperation
    assertThat(updatedUser.getName().getGivenName()).isEqualTo("Given Name");
  }

  @Test
  public void applyReplaceNestedComplexAttribute() {
    // setup existing user
    Name existingName = new Name();
    existingName.setFamilyName("Family Name");
    existingName.setGivenName("Given Name");
    ScimUser user = user();
    user.setName(existingName);

    // setup patch ops
    Map<String, Map<String, String>> newName = Map.of("name", Map.of("familyName", "New Family Name"));
    PatchOperation op = patchOperation(REPLACE, null, newName);

    //execute
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getName().getFamilyName()).isEqualTo("New Family Name");
    // assert that PATCH did not update fields not provided in PatchOperation
    assertThat(updatedUser.getName().getGivenName()).isEqualTo("Given Name");
  }

  @Test
  public void applyReplaceUserName()  {
    String newUserName = "testUser_updated@test.com";
    PatchOperation op = patchOperation(REPLACE, "userName", newUserName);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getUserName()).isEqualTo(newUserName);
  }

  @Test
  public void applyReplaceMappedValueWhenPathIsNull() {
    String newDisplayName = "Test User - Updated";
    PatchOperation op = patchOperation(REPLACE, null, Map.ofEntries(entry("displayName", newDisplayName)));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getDisplayName()).isEqualTo(newDisplayName);
  }

  @Test
  public void applyReplaceWithExpressionFilter()
  {
    String newNumber = "tel:+1-123-456-7890";
    PatchOperation op = patchOperation(REPLACE, "phoneNumbers[type eq \"mobile\"].value", newNumber);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getPhoneNumbers().get(0).getValue()).isEqualTo(newNumber);
  }

  @Test
  public void applyReplaceSubAttribute() {
    String newFormattedName = "Maverick";
    PatchOperation op = patchOperation(REPLACE, "name.formatted", newFormattedName);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getName().getFormatted()).isEqualTo(newFormattedName);
  }

  @Test
  public void applyAddWhenFilterDoesNotMatchAny() {
    String faxNumber = "tel:+1-123-456-7899";
    PatchOperation op = patchOperation(ADD, "phoneNumbers[type eq \"fax\"].value", faxNumber);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<PhoneNumber> phoneNumbers =  updatedUser.getPhoneNumbers();
    assertThat(phoneNumbers.size()).isEqualTo(2);

    Optional<PhoneNumber> addedFaxNumber = phoneNumbers.stream().filter(phoneNumber -> phoneNumber.getType().equals("fax")).findFirst();
    assertThat(addedFaxNumber).isNotEmpty();
    assertThat(addedFaxNumber.get().getValue()).isEqualTo(faxNumber);
  }

  @Test
  public void applyAddToMultiValuedComplexAttribute() {

    Address workAddress = new Address()
      .setType("work")
      .setStreetAddress("101 Main Street")
      .setRegion("Springfield");

    Address homeAddress = new Address()
      .setType("home")
      .setStreetAddress("202 Maple Street")
      .setRegion("Otherton")
      .setPostalCode("43210");

    List<Address> expectedAddresses = List.of(
      new Address()
        .setType("work")
        .setStreetAddress("101 Main Street")
        .setRegion("Springfield")
        .setPostalCode("ko4 8qq"),
      homeAddress);

    ScimUser user = user().setAddresses(List.of(workAddress, homeAddress));

    PatchOperation op = patchOperation(ADD, "addresses[type eq \"work\"].postalCode", "ko4 8qq");
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getAddresses()).isEqualTo(expectedAddresses);
  }

  @Test
  public void applyAddSingleComplexAttribute() {
    ScimUser user =  user();
    PatchOperation op = patchOperation(ADD, "name.honorificSuffix", "II");
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    Name expectedName = new Name()
      .setFormatted(user.getName().getFormatted())
      .setHonorificSuffix("II");
    assertThat(updatedUser.getName()).isEqualTo(expectedName);
  }

  @Test
  public void applyReplaceSingleComplexAttribute() {
    ScimUser user =  user();
    PatchOperation op = patchOperation(REPLACE, "name.formatted", "Charlie");
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    Name expectedName = new Name()
      .setFormatted("Charlie");
    assertThat(updatedUser.getName()).isEqualTo(expectedName);
  }

  @Test
  public void applyAddToMissingSingleComplexAttribute() {
    ScimUser user =  user();
    PatchOperation op = patchOperation(ADD, "addresses[type eq \"work\"].postalCode", "ko4 8qq");
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    List<Address> expectedAddresses = List.of(
      new Address()
        .setType("work")
        .setPostalCode("ko4 8qq"));
    assertThat(updatedUser.getAddresses()).isEqualTo(expectedAddresses);
  }

  @Test
  public void settingPrimaryOnMultiValuedShouldResetAllOthersToFalse() {
    // https://www.rfc-editor.org/rfc/rfc7644#section-3.5.2
    ScimUser user =  user();
    PatchOperation op = patchOperation(REPLACE, "emails[type eq \"home\"].primary", true);
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    List<Email> expectedEmails = List.of(
      new Email()
        .setPrimary(false)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setPrimary(true)
        .setType("home")
        .setValue("home@example.com"));
    assertThat(updatedUser.getEmails()).isEqualTo(expectedEmails);
  }

  @Test
  public void applyRemoveSubAttribute() {
    PatchOperation op = patchOperation(REMOVE, "name.formatted", null);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getName().getFormatted()).isNull();
  }

  @Test
  public void applyRemoveItemWithFilter() {
    ScimUser user = user().setAddresses(List.of(
      new Address()
        .setType("work")
        .setStreetAddress("101 Main Street")
        .setRegion("Springfield")
        .setPostalCode("01234"),
      new Address()
        .setType("home")
        .setStreetAddress("202 Maple Street")
        .setRegion("Otherton")
        .setPostalCode("43210")
    ));

    PatchOperation op = patchOperation(REMOVE, "addresses[type eq \"work\"]", null);
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(1);
    assertThat(updatedUser.getAddresses().get(0).getType()).isEqualTo("home");
    assertThat(updatedUser.getAddresses().get(0).getPostalCode()).isEqualTo("43210");
  }

  @Test
  public void applyRemoveAttributeWithFilter() {
    Address workAddress = new Address()
      .setType("work")
      .setStreetAddress("101 Main Street")
      .setRegion("Springfield")
      .setPostalCode("01234");

    Address homeAddress = new Address()
      .setType("home")
      .setStreetAddress("202 Maple Street")
      .setRegion("Otherton")
      .setPostalCode("43210");

    Address updatedWorkAddress = new Address()
      .setType("work")
      .setStreetAddress("101 Main Street")
      .setRegion("Springfield");

    ScimUser user = user().setAddresses(List.of(workAddress, homeAddress));

    PatchOperation op = patchOperation(REMOVE, "addresses[type eq \"work\"].postalCode", null);
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(2);
    assertThat(updatedUser.getAddresses().get(0)).isEqualTo(updatedWorkAddress);
  }

  @Test
  public void applyReplaceItemWithFilter() {
    Address workAddress = new Address()
      .setType("work")
      .setStreetAddress("101 Main Street")
      .setRegion("Springfield")
      .setPostalCode("01234");

    Address homeAddress = new Address()
      .setType("home")
      .setStreetAddress("202 Maple Street")
      .setRegion("Othertown")
      .setPostalCode("43210");

    Address otherAddress = new Address()
      .setType("other")
      .setStreetAddress("303 Loop Road")
      .setRegion("Thirdtown")
      .setPostalCode("11223")
      .setPrimary(true);

    Map<String, Object> newAddress = Map.of(
      "type", otherAddress.getType(),
      "streetAddress", otherAddress.getStreetAddress(),
      "region", otherAddress.getRegion(),
      "postalCode", otherAddress.getPostalCode(),
      "primary", otherAddress.getPrimary()
    );

    ScimUser user = user().setAddresses(List.of(workAddress, homeAddress));
    PatchOperation op = patchOperation(REPLACE, "addresses[type eq \"work\"]", newAddress);
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(2);
    assertThat(updatedUser.getAddresses().get(0)).isEqualTo(otherAddress);
    assertThat(updatedUser.getAddresses().get(1)).isEqualTo(homeAddress);
  }

  @Test
  public void applyReplaceItemAttributeWithFilter() {
    Address workAddress = new Address()
      .setType("work")
      .setStreetAddress("101 Main Street")
      .setRegion("Springfield")
      .setPostalCode("01234");

    Address homeAddress = new Address()
      .setType("home")
      .setStreetAddress("202 Maple Street")
      .setRegion("Othertown")
      .setPostalCode("43210");

    Address updatedHome = new Address()
      .setType("home")
      .setStreetAddress("202 Maple Street")
      .setRegion("Othertown")
      .setPostalCode("43210")
      .setPrimary(true);

    ScimUser user = user().setAddresses(List.of(workAddress, homeAddress));
    PatchOperation op = patchOperation(REPLACE, "addresses[type eq \"home\"].primary", true);
    ScimUser updatedUser = patchHandler.apply(user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(2);
    assertThat(updatedUser.getAddresses().get(0)).isEqualTo(workAddress);
    assertThat(updatedUser.getAddresses().get(1)).isEqualTo(updatedHome);
  }

  @Test
  public void applyReplaceEnterpriseExtension() {
    String employeeNumberUrn = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber";
    String employeeNumber = "NCIR48XM6D84";
    PatchOperation op = patchOperation(ADD, null, Map.ofEntries(entry(employeeNumberUrn, employeeNumber)));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getSchemas().size()).isEqualTo(2);
    EnterpriseExtension enterpriseUser = (EnterpriseExtension) updatedUser.getExtension(EnterpriseExtension.URN);
    assertThat(enterpriseUser.getEmployeeNumber()).isEqualTo(employeeNumber);
  }

  @Test
  public void applyRemove() {
    PatchOperation op = patchOperation(REMOVE, "displayName", null);
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getDisplayName()).isNull();
  }

  @Test
  public void applyWithFilterExpression() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REPLACE);
    op.setPath(PatchOperationPath.fromString("emails[type EQ \"home\"].value"));
    op.setValue("new-home@example.com");
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setType("home")
        .setValue("new-home@example.com") // updated email
    ));
  }

  @Test
  public void replaceItem() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REPLACE);
    op.setPath(PatchOperationPath.fromString("emails[type EQ \"home\"]"));
    op.setValue(Map.of(
      "type", "other",
      "value", "other@example.com"
    ));

    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setType("other")
        .setValue("other@example.com")
    ));
  }

  @Test
  public void replaceMultipleAttributes() {
    PatchOperation op = new PatchOperation();
    op.setOperation(REPLACE);
    op.setValue(Map.of(
      "emails", List.of(
        Map.of(
          "type", "home",
          "value", "first@example.com"),
        Map.of(
          "type", "work",
          "value", "second@example.com",
          "primary", true)
      ),
      "nickName", "Babs"
    ));

    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setType("home")
        .setValue("first@example.com"),
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("second@example.com")
    ));
    assertThat(updatedUser.getNickName()).isEqualTo("Babs");
  }

  @Test
  public void replaceCollection() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REPLACE);
    op.setPath(PatchOperationPath.fromString("emails"));
    op.setValue(List.of(
      Map.of(
        "value", "first@example.com",
        "type", "home"),
      Map.of(
        "primary", true,
        "value", "second@example.com",
        "type", "work")
    ));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setType("home")
        .setValue("first@example.com"),
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("second@example.com")
    ));
  }

  @Test
  public void deleteItemWithFilter() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REMOVE);
    op.setPath(PatchOperationPath.fromString("emails[type EQ \"home\"]"));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com")
    ));
  }

  @Test
  public void deleteAttributeWithPath() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REMOVE);
    op.setPath(PatchOperationPath.fromString("nickName"));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getNickName()).isNull();
  }

  @Test
  public void deleteCollectionWithPath() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REMOVE);
    op.setPath(PatchOperationPath.fromString("emails"));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getEmails()).isNull();
  }

  @Test
  public void deleteItemWithComplexFilter() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REMOVE);
    op.setPath(PatchOperationPath.fromString("emails[type EQ \"home\"] and value ew \"example.com\""));
    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser.getEmails()).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com")
    ));
  }

  @Test
  public void addItemToCollection() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(ADD);
    op.setPath(PatchOperationPath.fromString("members"));
    op.setValue(List.of(
      Map.of(
        "value", "9876",
        "display", "testUser2",
        "type", "User")
    ));

    GroupMembership member1 = userRef("1234");
    GroupMembership member2 = userRef("5678");
    GroupMembership member3 = new GroupMembership()
      .setValue("9876")
      .setDisplay("testUser2")
      .setType(GroupMembership.Type.USER);

    ScimGroup updatedGroup = patchHandler.apply(group(), List.of(op));
    scimAssertThat(updatedGroup).containsOnlyMembers(member1, member2, member3);
  }

  /**
   * This test covers Azure-style remove member operations, where a value is
   * specified in the operation body, rather than in the path.
   * For example: { "op": "remove", "path": "members", "value": [ { "value": "1234" } ] }
   */
  @Test
  public void removeItemFromCollection() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(REMOVE);
    op.setPath(PatchOperationPath.fromString("members"));
    op.setValue(List.of(Map.of("value", "1234")));

    ScimGroup updatedGroup = patchHandler.apply(group(), List.of(op));
    assertThat(updatedGroup.getMembers()).isNotNull();
    assertThat(updatedGroup.getMembers().size()).isEqualTo(1);
  }

  @Test
  public void addAttribute() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(ADD);
    op.setPath(PatchOperationPath.fromString("profileUrl"));
    op.setValue("https://profile.example.com");

    ScimUser expectedUser = user()
      .setProfileUrl("https://profile.example.com");

    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    assertThat(updatedUser).isEqualTo(expectedUser);
  }

  @Test
  public void addItem() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(ADD);
    op.setPath(PatchOperationPath.fromString("emails"));
    op.setValue(Map.of(
      "type", "other",
      "value", "other@example.com"));

    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setType("home")
        .setValue("home@example.com"),
      new Email()
        .setType("other")
        .setValue("other@example.com")
    ));
  }

  @Test
  public void addMultipleProperties() throws FilterParseException {
    // From Section 3.5.2.1 Add Operation of SCIM Protocol RFC
    PatchOperation op = new PatchOperation();
    op.setOperation(ADD);
    op.setValue(Map.of(
      "emails", Map.of(
        "value", "babs@example.com",
        "type", "other"),
      "profileUrl", "https://profile.example.com"
    ));

    ScimUser updatedUser = patchHandler.apply(user(), List.of(op));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setType("home")
        .setValue("home@example.com"),
      new Email()
        .setType("other")
        .setValue("babs@example.com")
    ));
    assertThat(updatedUser.getProfileUrl()).isEqualTo("https://profile.example.com");
  }

  @Test
  public void multiplePatchOperations() throws FilterParseException {
    PatchOperation opRm = new PatchOperation();
    opRm.setOperation(REMOVE);
    opRm.setPath(PatchOperationPath.fromString("emails[type EQ \"home\"]"));

    PatchOperation opAdd = new PatchOperation();
    opAdd.setOperation(ADD);
    opAdd.setPath(PatchOperationPath.fromString("emails"));
    opAdd.setValue(Map.of(
      "value", "babs@example.com",
      "type", "other")
    );

    ScimUser updatedUser = patchHandler.apply(user(), List.of(opRm, opAdd));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("work@example.com"),
      new Email()
        .setType("other")
        .setValue("babs@example.com")
    ));
  }

  @Test
  public void replaceCollectionWithMultipleOps() throws FilterParseException {
    PatchOperation opRm = new PatchOperation();
    opRm.setOperation(REMOVE);
    opRm.setPath(PatchOperationPath.fromString("emails"));

    PatchOperation opAdd = new PatchOperation();
    opAdd.setOperation(ADD);
    opAdd.setPath(PatchOperationPath.fromString("emails"));
    opAdd.setValue(List.of(
      Map.of(
        "value", "first@example.com",
        "type", "home"),
      Map.of(
        "primary", true,
        "value", "second@example.com",
        "type", "work")
    ));

    ScimUser updatedUser = patchHandler.apply(user(), List.of(opRm, opAdd));
    List<Email> emails = updatedUser.getEmails();
    assertThat(emails).isEqualTo(List.of(
      new Email()
        .setType("home")
        .setValue("first@example.com"),
      new Email()
        .setPrimary(true)
        .setType("work")
        .setValue("second@example.com")
    ));
  }

  @SneakyThrows
  private PatchOperation patchOperation(Type operationType, String path, Object value) {
    PatchOperation op = new PatchOperation();
    op.setOperation(operationType);
    if (path != null) {
      op.setPath(PatchOperationPath.fromString(path));
    }
    if (value != null) {
      op.setValue(value);
    }
    return op;
  }

  private static ScimUser user() {
    try {
      return new ScimUser()
        .setUserName("testUser@test.com")
        .setDisplayName("Test User")
        .setNickName("tester")
        .setName(new Name()
          .setFormatted("Berry"))
        .setPhoneNumbers(List.of(new PhoneNumber()
          .setType("mobile")
          .setValue("tel:+1-111-111-1111")))
        .setEmails(List.of(
          new Email()
            .setPrimary(true)
            .setType("work")
            .setValue("work@example.com"),
          new Email()
            .setType("home")
            .setValue("home@example.com")
        ));
    } catch (PhoneNumberParseException e) {
      throw new IllegalStateException("Invalid phone number", e);
    }
  }

  private static ScimGroup group() {
    GroupMembership member1 = userRef("1234");
    GroupMembership member2 = userRef("5678");

    return new ScimGroup()
      .setDisplayName("Test Group")
      .setExternalId("test-group-external-id")
      .setMembers(List.of(member1, member2));
  }

  private static GroupMembership userRef(String id) {
    GroupMembership member = new GroupMembership();
    member.setType(GroupMembership.Type.USER);
    member.setValue(id);
    member.setRef("https://example.com/Users/" + id);
    member.setDisplay("Test User" + id);

    return member;
  }
}
