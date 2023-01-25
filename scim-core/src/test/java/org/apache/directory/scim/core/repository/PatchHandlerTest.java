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
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperation.Type;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PatchHandlerTest {

  PatchHandlerImpl patchHandler;
  private ScimUser user;

  public PatchHandlerTest() {
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    schemaRegistry.addSchema(ScimUser.class, List.of(EnterpriseExtension.class));
    this.patchHandler = new PatchHandlerImpl(schemaRegistry);
  }

  @BeforeEach
  public void init() throws PhoneNumberParseException {
    this.user = new ScimUser();
    this.user.setUserName("testUser@test.com");
    this.user.setDisplayName("Test User");
    Name name = new Name();
    name.setFormatted("Berry");
    this.user.setName(name);
    PhoneNumber phone = new PhoneNumber();
    phone.setType("mobile");
    phone.setValue("tel:+1-111-111-1111");
    this.user.setPhoneNumbers(List.of(phone));
    this.user.setSchemas(Set.of(ScimUser.SCHEMA_URI));
  }

  @Test
  public void applyReplaceUserName()  {
    String newUserName = "testUser_updated@test.com";
    PatchOperation op = patchOperation(Type.REPLACE, "userName", newUserName);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getUserName()).isEqualTo(newUserName);
  }

  @Test
  public void applyReplaceMappedValueWhenPathIsNull() {
    String newDisplayName = "Test User - Updated";
    PatchOperation op = patchOperation(Type.REPLACE, null, Map.ofEntries(entry("displayName", newDisplayName)));
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getDisplayName()).isEqualTo(newDisplayName);
  }

  @Test
  public void applyReplaceWithExpressionFilter()
  {
    String newNumber = "tel:+1-123-456-7890";
    PatchOperation op = patchOperation(Type.REPLACE, "phoneNumbers[type eq \"mobile\"].value", newNumber);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getPhoneNumbers().get(0).getValue()).isEqualTo(newNumber);
  }

  @Test
  public void applyReplaceSubAttribute() {
    String newFormattedName = "Maverick";
    PatchOperation op = patchOperation(Type.REPLACE, "name.formatted", newFormattedName);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getName().getFormatted()).isEqualTo(newFormattedName);
  }

  @Test
  public void applyAddWhenFilterDoesNotMatchAny() {
    String faxNumber = "tel:+1-123-456-7899";
    PatchOperation op = patchOperation(Type.ADD, "phoneNumbers[type eq \"fax\"].value", faxNumber);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    List<PhoneNumber> phoneNumbers =  updatedUser.getPhoneNumbers();
    assertThat(phoneNumbers.size()).isEqualTo(2);

    Optional<PhoneNumber> addedFaxNumber = phoneNumbers.stream().filter(phoneNumber -> phoneNumber.getType().equals("fax")).findFirst();
    assertThat(addedFaxNumber).isNotEmpty();
    assertThat(addedFaxNumber.get().getValue()).isEqualTo(faxNumber);
  }

  @Test
  public void applyAddToMultiValuedComplexAttribute() {
    String postalCode = "ko4 8qq";
    PatchOperation op = patchOperation(Type.ADD, "addresses[type eq \"work\"].postalCode", postalCode);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(1);
    assertThat(updatedUser.getAddresses().get(0).getType()).isEqualTo("work");
    assertThat(updatedUser.getAddresses().get(0).getPostalCode()).isEqualTo(postalCode);
  }

  @Test
  public void applyRemoveItemWithFilter() {
    this.user.setAddresses(List.of(
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

    PatchOperation op = patchOperation(Type.REMOVE, "addresses[type eq \"work\"]", null);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
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

    this.user.setAddresses(List.of(workAddress, homeAddress));

    PatchOperation op = patchOperation(Type.REMOVE, "addresses[type eq \"work\"].postalCode", null);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
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

    this.user.setAddresses(List.of(workAddress, homeAddress));
    PatchOperation op = patchOperation(Type.REPLACE, "addresses[type eq \"work\"]", newAddress);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
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

    this.user.setAddresses(List.of(workAddress, homeAddress));
    PatchOperation op = patchOperation(Type.REPLACE, "addresses[type eq \"home\"].primary", true);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getAddresses().size()).isEqualTo(2);
    assertThat(updatedUser.getAddresses().get(0)).isEqualTo(workAddress);
    assertThat(updatedUser.getAddresses().get(1)).isEqualTo(updatedHome);
  }

  @Test
  public void applyReplaceEnterpriseExtension() {
    String employeeNumberUrn = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber";
    String employeeNumber = "NCIR48XM6D84";
    PatchOperation op = patchOperation(Type.ADD, null, Map.ofEntries(entry(employeeNumberUrn, employeeNumber)));
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getSchemas().size()).isEqualTo(2);
    EnterpriseExtension enterpriseUser = (EnterpriseExtension) updatedUser.getExtension(EnterpriseExtension.URN);
    assertThat(enterpriseUser.getEmployeeNumber()).isEqualTo(employeeNumber);
  }

  @Test
  public void applyRemove() {
    PatchOperation op = patchOperation(Type.REMOVE, "displayName", null);
    ScimUser updatedUser = patchHandler.apply(this.user, List.of(op));
    assertThat(updatedUser.getDisplayName()).isNull();
  }

  @SneakyThrows
  private PatchOperation patchOperation(Type operationType, String path, Object value) {
    PatchOperation op = new PatchOperation();
    op.setOperation(operationType);
    if (path != null) {
      op.setPath(new PatchOperationPath(path));
    }
    if (value != null) {
      op.setValue(value);
    }
    return op;
  }
}
