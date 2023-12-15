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

package org.apache.directory.scim.tools.diff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.directory.scim.test.stub.ExampleObjectExtension;
import org.apache.directory.scim.test.stub.Subobject;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperation.Type;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.Photo;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.Schemas;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import static java.util.Collections.emptyList;
import static org.apache.directory.scim.test.assertj.ScimpleAssertions.patchOpMatching;
import static org.apache.directory.scim.test.assertj.ScimpleAssertions.scimAssertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PatchGeneratorTest {
  
  private static final String FIRST = "first";
  private static final String SECOND = "second";
  private static final String THIRD = "third";
  private static final String FOURTH = "fourth";
  
  private static final String A = "A";
  private static final String B = "B";
  private static final String C = "C";

  private SchemaRegistry schemaRegistry;

  @BeforeEach
  public void initialize() throws Exception {
    schemaRegistry = mock(SchemaRegistry.class);
    when(schemaRegistry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(Schemas.schemaFor(ScimUser.class));
    when(schemaRegistry.getSchema(EnterpriseExtension.URN)).thenReturn(Schemas.schemaForExtension(EnterpriseExtension.class));
    when(schemaRegistry.getSchema(ExampleObjectExtension.URN)).thenReturn(Schemas.schemaForExtension(ExampleObjectExtension.class));

    when(schemaRegistry.getSchema(ScimGroup.SCHEMA_URI)).thenReturn(Schemas.schemaFor(ScimGroup.class));
  }

  @Test
  public void testAddSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setNickName("Jon");
    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
        .matches(Type.ADD, "nickName", "Jon");
  }
  
  @Test
  public void testAddSingleExtension() throws Exception {
    ScimUser user1 = createUser();
    EnterpriseExtension ext = user1.removeExtension(EnterpriseExtension.class);
    ScimUser user2 = createUser();
    user2.addExtension(ext);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.ADD, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", ext);
  }

  @Test
  public void testAddComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setHonorificPrefix("Dr.");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.ADD, "name.honorificPrefix", "Dr.");
  }

  @Test
  public void testAddMultiValuedAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(false);
    user2.getPhoneNumbers().add(mobilePhone);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.ADD, "phoneNumbers", mobilePhone);
  }
  
  /**
   * This unit test is to replicate the issue where a replace is sent back
   * from the differencing engine for a collection that is currently empty
   * but is having an object added to it. This should produce an ADD with an
   * ArrayList of objects to add.
   */
  @Test
  public void testAddObjectToEmptyCollection() throws Exception {
    ScimUser user1 = createUser();
    user1.setPhoneNumbers(new ArrayList<>());
    ScimUser user2 = createUser();
    user2.setPhoneNumbers(new ArrayList<>());
    
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(true);
    user2.getPhoneNumbers().add(mobilePhone);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    assertNotNull(operations);
    assertThat(operations).hasSize(1);
    PatchOperation operation = operations.get(0);
    assertNotNull(operation.getValue());
    assertEquals(Type.ADD, operation.getOperation());
    assertEquals(PhoneNumber.class, operation.getValue().getClass());
  }
  
  @Test
  public void testAddObjectsToEmptyCollection() throws Exception {
    ScimUser user1 = createUser();
    user1.setPhoneNumbers(new ArrayList<>());
    ScimUser user2 = createUser();
    user2.setPhoneNumbers(new ArrayList<>());
    
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(true);
    
    PhoneNumber homePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5307").build();
    homePhone.setType("home");
    homePhone.setPrimary(true);
    
    user2.getPhoneNumbers().add(mobilePhone);
    user2.getPhoneNumbers().add(homePhone);


    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    assertNotNull(operations);
    assertEquals(2, operations.size());
    
    PatchOperation operation = operations.get(0);
    assertNotNull(operation.getValue());
    assertEquals(Type.ADD, operation.getOperation());
    assertEquals(PhoneNumber.class, operation.getValue().getClass());
    
    operation = operations.get(1);
    assertNotNull(operation.getValue());
    assertEquals(Type.ADD, operation.getOperation());
    assertEquals(PhoneNumber.class, operation.getValue().getClass());
  }

  @Test
  public void testReplaceSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setActive(false);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REPLACE, "active", false);
  }
  
  @Test
  public void testReplaceExtensionSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getExtension(EnterpriseExtension.class).setDepartment("Dept XYZ.");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department", "Dept XYZ.");
  }

  @Test
  public void testReplaceComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setFamilyName("Nobody");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REPLACE, "name.familyName", "Nobody");
  }

  @Test
  public void testReplaceMultiValuedAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    Email workEmail = user2.getEmails().get(1);
    workEmail.setValue("nobody@example.com");
    workEmail.setDisplay("nobody@example.com");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    // Changing contents of a collection, should REMOVE the old and ADD a new
    scimAssertThat(result).containsOnly(
        patchOpMatching(Type.REMOVE, "emails[type EQ \"work\"]"),
        patchOpMatching(Type.ADD, "emails", workEmail));
  }

  @Test
  public void testRemoveSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setUserName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "userName", null);
  }
  
  @Test
  public void testRemoveSingleExtension() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.removeExtension(EnterpriseExtension.class);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", null);
  }

  @Test
  public void testRemoveComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setMiddleName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "name.middleName", null);
  }

  @Test
  public void testRemoveFullComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "name", null);
  }

  @Test
  public void testRemoveMultiValuedAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    List<Email> newEmails = user2.getEmails()
                                 .stream()
                                 .filter(e -> e.getType()
                                               .equals("work"))
                                 .collect(Collectors.toList());
    user2.setEmails(newEmails);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "emails[type EQ \"home\"]", null);
  }
  
  @Test
  public void testRemoveMultiValuedAttributeWithSorting() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    Address localAddress = new Address();
    localAddress.setStreetAddress("123 Main Street");
    localAddress.setLocality("State College");
    localAddress.setRegion("PA");
    localAddress.setCountry("USA");
    localAddress.setType("local");
    
    user1.getAddresses().add(localAddress);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result).single()
      .matches(Type.REMOVE, "addresses[type EQ \"local\"]", null);
  }
  
  @Test
  public void testAddMultiValuedAttributeWithSorting() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    Address localAddress = new Address();
    localAddress.setStreetAddress("123 Main Street");
    localAddress.setLocality("State College");
    localAddress.setRegion("PA");
    localAddress.setCountry("USA");
    localAddress.setType("local");

    Address newWorkAddress = user2.getAddresses().get(0);
    newWorkAddress.setPostalCode("01234"); // changes whole address, expect REMOVE/ADD

    user2.getAddresses().add(localAddress); // ADD new Address

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(result)
      .containsOnly(
          patchOpMatching(Type.REMOVE, "addresses[type EQ \"work\"]"),
          patchOpMatching(Type.ADD, "addresses", newWorkAddress),
          patchOpMatching(Type.ADD, "addresses", localAddress));
  }
  
  @Test
  public void verifyEmptyArraysDoNotCauseDiff() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    user1.setPhotos(new ArrayList<>());

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    scimAssertThat(operations)
      .describedAs("Empty Arrays caused a diff")
      .isEmpty();
  }

  @Test
  public void verifyEmptyExtensionArraysDoNotCauseDiff() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();

    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    user1.addExtension(ext1);

    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(new ArrayList<>());
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    scimAssertThat(operations)
      .describedAs("Empty Arrays caused a diff")
      .isEmpty();
  }
  
  @Test
  public void verifyEmptyArraysAreNulled() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();

    PatchGenerator patchGenerator = new PatchGenerator(schemaRegistry);

    //Set empty list on root object and verify no differences
    user1.setPhotos(new ArrayList<>());
    List<PatchOperation> operations = patchGenerator.diff(user1, user2);
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
    
    //Reset user 1 and empty list on Extension and verify no differences
    user1 = createUser();
    ExampleObjectExtension ext = new ExampleObjectExtension();
    ext.setList(new ArrayList<>());
    operations = patchGenerator.diff(user1, user2);
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
    
    //Reset extension and set empty list on element of extension then verify no differences
    Subobject subobject = new Subobject();
    subobject.setList1(new ArrayList<>());
    ext = new ExampleObjectExtension();
    ext.setSubobject(subobject);
    operations = patchGenerator.diff(user1, user2);
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
  }

  @Test
  public void testAddArray() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    Photo photo = new Photo();
    photo.setType("photo");
    photo.setValue("photo1.png");
    user2.setPhotos(Stream.of(photo).collect(Collectors.toList()));

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.ADD, "photos", photo)
    );
  }

  @Test
  public void testAddExtensionArray() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();

    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(null);
    user1.addExtension(ext1);

    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(List.of(FIRST,SECOND));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.ADD, ExampleObjectExtension.URN + ":list", List.of(FIRST,SECOND))
    );
  }
  
  @Test
  public void testRemoveArray() throws Exception {
    ScimUser user1 = createUser();
    Photo photo = new Photo();
    photo.setType("photo");
    photo.setValue("photo1.png");
    user1.setPhotos(List.of(photo));

    ScimUser user2 = createUser();

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REMOVE, "photos")
    );
  }

  @Test
  public void testRemoveExtensionArray() throws Exception {
    ScimUser user1 = createUser();
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(List.of(FIRST,SECOND));
    user1.addExtension(ext1);

    ScimUser user2 = createUser();
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(null);
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REMOVE, ExampleObjectExtension.URN + ":list")
    );
  }
  
  @Test
  public void testNonTypedAttributeListGetUseablePath() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(List.of(FIRST,SECOND,THIRD));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(List.of(FIRST,SECOND,FOURTH));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    // Changing content of list should REMOVE old, and ADD new
    scimAssertThat(operations).containsOnly(
        patchOpMatching(Type.REPLACE, ExampleObjectExtension.URN + ":list", List.of(FIRST,SECOND,FOURTH)));
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart1() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    
    user2.getName().setFormatted(nickname);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.ADD, "name.formatted", nickname),
      patchOpMatching(Type.REMOVE, "nickName", null)
    );
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart2() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName("");

    user1.getName().setFormatted("");
    user2.getName().setFormatted(nickname);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REPLACE, "name.formatted", nickname),
      patchOpMatching(Type.REPLACE, "nickName", ""));
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart3() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName(null);

    user1.getName().setFormatted("");
    user2.getName().setFormatted(nickname);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REPLACE, "name.formatted", nickname),
      patchOpMatching(Type.REMOVE, "nickName"));
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart4() throws Exception {

    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName("");

    user1.getName().setFormatted(null);
    user2.getName().setFormatted(nickname);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.ADD, "name.formatted", nickname),
      patchOpMatching(Type.REPLACE, "nickName", ""));
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart5() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName("");
    user2.setNickName(nickname);

    user1.getName().setFormatted(nickname);
    user2.getName().setFormatted(null);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REMOVE, "name.formatted"),
      patchOpMatching(Type.REPLACE, "nickName", nickname));
  }
  
  @ParameterizedTest
  @MethodSource("testListOfStringsParameters")
  public void testListOfStringsParameterized(List<String> list1, List<String> list2, List<Condition<PatchOperation>> opConditions) throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(list1);
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(list2);
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    scimAssertThat(operations).containsOnly(opConditions);
  }
  
  @SuppressWarnings("unused")
  private static Object[] testListOfStringsParameters() throws Exception {
    List<Object> params = new ArrayList<>();
    String nickName = "John Xander Anyman";
    //Parameter order
    //1 Original list of Strings
    //2 Update list of Strings
    //3 Array of Expected Operations
    //  3a Operation
    //  3b Path
    //  3c Value
    
    List<Condition<PatchOperation>> multipleOps = new ArrayList<>();
    multipleOps.add(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "A"));
    multipleOps.add(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "B"));
    multipleOps.add(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "C"));

    params.add(new Object[] {List.of(A), emptyList(), List.of(patchOpMatching(Type.REMOVE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list"))});
    params.add(new Object[] {List.of(A), null, List.of(patchOpMatching(Type.REMOVE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list"))});
    params.add(new Object[] {null, List.of(A), List.of(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(A)))});
    params.add(new Object[] {null, List.of(C,B,A), List.of(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(C,B,A)))});
    params.add(new Object[] {List.of(A,B,C), emptyList(), List.of(patchOpMatching(Type.REMOVE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list"))});
    params.add(new Object[] {List.of(C,B,A), emptyList(), List.of(patchOpMatching(Type.REMOVE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list"))});
    params.add(new Object[] {emptyList(), List.of(A), List.of(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(A)))});
    params.add(new Object[] {emptyList(), List.of(C,B,A), List.of(patchOpMatching(Type.ADD, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(C,B,A)))});

    params.add(new Object[] {List.of(A, B), List.of(B), List.of(patchOpMatching(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(B)))});
    params.add(new Object[] {List.of(B, A), List.of(B), List.of(patchOpMatching(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(B)))});
    
    params.add(new Object[] {List.of(B), List.of(A,B), List.of(patchOpMatching(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(A,B)))});
    params.add(new Object[] {List.of(B), List.of(B,A), List.of(patchOpMatching(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(B,A)))});
    
    params.add(new Object[] {List.of(A), List.of(A,B,C), List.of(patchOpMatching(Type.REPLACE, "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", List.of(A,B,C)))});
    
    return params.toArray();
  }
  
  @Test
  public void testMultiplePrimitivesInArray() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(List.of("D","M","Y","Z","Z","Z","Z","Z"));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(List.of("A","Z"));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).contains(
        patchOpMatching(Type.REPLACE, ExampleObjectExtension.URN + ":list", List.of("A","Z")));
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart6() throws Exception {

    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(null);
    user2.setNickName(nickname);

    user1.getName().setFormatted(nickname);
    user2.getName().setFormatted("");

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REPLACE, "name.formatted", ""),
      patchOpMatching(Type.ADD, "nickName", nickname));
  }
  
  /**
   * This is used to test an error condition. In this scenario a user has multiple phone numbers where home is marked primary and work is not. A SCIM update
   * is performed in which the new user only contains a work phone number where the type is null. When this happens it should only be a single DELETE
   * operation. Instead it creates four operations: replace value of the home number with the work number value, replace the home type to work,
   * remove the primary flag, and remove the work number 
   */
  @Test
  public void testRemoveAndAddTypedElements() throws Exception {

    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    // remove home number REMOVE
    user2.getPhoneNumbers().remove(0);

    // change work number REMOVE old and ADD new
    PhoneNumber workNumber = user2.getPhoneNumbers().get(0);
    assertNotNull(workNumber);
    workNumber.setType(null);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    scimAssertThat(operations).contains(
      patchOpMatching(Type.REMOVE, "phoneNumbers[type EQ \"home\"]"),
      patchOpMatching(Type.REMOVE, "phoneNumbers[type EQ \"work\"]"),
      patchOpMatching(Type.ADD, "phoneNumbers", workNumber)
    );
  }

  @Test
  public void testGroupUpdate() throws FilterParseException {
    ScimGroup group1 = new ScimGroup();
    group1.setDisplayName("Test Group");
    group1.setMembers(new ArrayList<>());

    ScimGroup group2 = new ScimGroup();
    group2.setDisplayName("Test Group - updated");
    group2.setMembers(new ArrayList<>());

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(group1, group2);
    scimAssertThat(operations).single()
      .matches(Type.REPLACE, "displayName", "Test Group - updated");
  }

  @Test
  public void testAddGroupMember() throws FilterParseException {
    ScimGroup group1 = new ScimGroup();
    group1.setDisplayName("Test Group");
    group1.setMembers(new ArrayList<>());
    group1.getMembers().add(new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user1"));

    ScimGroup group2 = new ScimGroup();
    group2.setDisplayName("Test Group");
    group2.setMembers(new ArrayList<>());
    group2.getMembers().add(new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user1"));

    ResourceReference user2Ref = new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user2");
    group2.getMembers().add(user2Ref);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(group1, group2);
    assertNotNull(operations);
    assertEquals(1, operations.size());
    PatchOperation operation = operations.get(0);

    scimAssertThat(operation).matches(Type.ADD, "members", user2Ref);
  }

  @Test
  public void testRemoveGroupMember() throws FilterParseException {
    ScimGroup group1 = new ScimGroup();
    group1.setDisplayName("Test Group");
    group1.setMembers(new ArrayList<>());
    group1.getMembers().add(new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user1"));

    ResourceReference user2Ref = new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user2");
    group1.getMembers().add(user2Ref);

    ScimGroup group2 = new ScimGroup();
    group2.setDisplayName("Test Group");
    group2.setMembers(new ArrayList<>());
    group2.getMembers().add(new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user1"));

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(group1, group2);
    scimAssertThat(operations).contains(patchOpMatching(Type.REMOVE, "members[value EQ \"user2\"]"));
  }


  @Test
  public void testGroupReplace() throws FilterParseException {

    ScimGroup group1 = new ScimGroup();
    group1.setDisplayName("Test Group");
    group1.setMembers(new ArrayList<>());

    group1.getMembers().add(new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user1"));

    ScimGroup group2 = new ScimGroup();
    group2.setDisplayName("Test Group");
    group2.setMembers(new ArrayList<>());

    ResourceReference user2Ref = new ResourceReference()
      .setType(ResourceReference.ReferenceType.USER)
      .setValue("user2");
    group2.getMembers().add(user2Ref);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(group1, group2);

    scimAssertThat(operations).containsOnly(
      patchOpMatching(Type.REMOVE, "members[value EQ \"user1\"]", null),
      patchOpMatching(Type.ADD, "members", user2Ref)
    );
  }
  
  public static final Address createHomeAddress() {
    Address homeAddress = new Address();
    homeAddress.setType("home");
    homeAddress.setStreetAddress("123 Fake Street");
    homeAddress.setLocality("State College");
    homeAddress.setRegion("Pennsylvania");
    homeAddress.setCountry("USA");
    homeAddress.setPostalCode("16801");
    return homeAddress;
  }

  public static ScimUser createUser() throws PhoneNumberParseException {
    ScimUser user = new ScimUser();
    user.setId("912345678");
    user.setExternalId("912345678");
    user.setActive(true);
    user.setDisplayName("John Anyman");
    user.setTitle("Professor");
    user.setUserName("jxa123");

    Name name = new Name();
    name.setGivenName("John");
    name.setMiddleName("Xander");
    name.setFamilyName("Anyman");
    name.setHonorificSuffix("Jr.");
    user.setName(name);

    Address homeAddress = new Address();
    homeAddress.setType("home");
    homeAddress.setStreetAddress("123 Fake Street");
    homeAddress.setLocality("State College");
    homeAddress.setRegion("Pennsylvania");
    homeAddress.setCountry("USA");
    homeAddress.setPostalCode("16801");

    Address workAddress = new Address();
    workAddress.setType("work");
    workAddress.setStreetAddress("2 Old Main");
    workAddress.setLocality("State College");
    workAddress.setRegion("Pennsylvania");
    workAddress.setCountry("USA");
    workAddress.setPostalCode("16802");

    List<Address> address = Stream.of(workAddress, homeAddress).collect(Collectors.toList());
    user.setAddresses(address);

    Email workEmail = new Email();
    workEmail.setPrimary(true);
    workEmail.setType("work");
    workEmail.setValue("jxa123@psu.edu");
    workEmail.setDisplay("jxa123@psu.edu");

    Email homeEmail = new Email();
    homeEmail.setPrimary(true);
    homeEmail.setType("home");
    homeEmail.setValue("john@gmail.com");
    homeEmail.setDisplay("john@gmail.com");

    Email otherEmail = new Email();
    otherEmail.setPrimary(true);
    otherEmail.setType("other");
    otherEmail.setValue("outside@version.net");
    otherEmail.setDisplay("outside@version.net");

    List<Email> emails = Stream.of(homeEmail, workEmail).collect(Collectors.toList());
    user.setEmails(emails);

    //"+1(814)867-5309"
    PhoneNumber homePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5309").build();
    homePhone.setType("home");
    homePhone.setPrimary(true);

    //"+1(814)867-5307"
    PhoneNumber workPhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5307").build();
    workPhone.setType("work");
    workPhone.setPrimary(false);

    List<PhoneNumber> phones = Stream.of(homePhone, workPhone).collect(Collectors.toList());
    user.setPhoneNumbers(phones);

    EnterpriseExtension enterpriseExtension = new EnterpriseExtension();
    enterpriseExtension.setEmployeeNumber("7865");
    enterpriseExtension.setDepartment("Dept B.");
    Manager manager = new Manager();
    manager.setValue("Pointy Haired Boss");
    manager.setRef("45353");
    enterpriseExtension.setManager(manager);
    user.addExtension(enterpriseExtension);

    return user;
  }

  private List<PatchOperation> createUser1PatchOps() throws FilterParseException {
    List<PatchOperation> patchOperations = new ArrayList<>();
    PatchOperation removePhoneNumberOp = new PatchOperation();
    removePhoneNumberOp.setOperation(Type.REMOVE);
    removePhoneNumberOp.setPath(PatchOperationPath.fromString("phoneNumbers[type eq \"home\"]"));
    patchOperations.add(removePhoneNumberOp);
    return patchOperations;
  }
}
