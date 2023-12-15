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

import static org.apache.directory.scim.core.repository.PatchGeneratorTest.PatchOperationCondition.op;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.directory.scim.test.stub.ExampleObjectExtension;
import org.apache.directory.scim.test.stub.Subobject;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperation.Type;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.Photo;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.schema.Schemas;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
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
  }


  @Test
  public void testAddSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setNickName("Jon");
    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "nickName", "Jon");
  }
  
  @Test
  public void testAddSingleExtension() throws Exception {
    ScimUser user1 = createUser();
    EnterpriseExtension ext = user1.removeExtension(EnterpriseExtension.class);
    ScimUser user2 = createUser();
    user2.addExtension(ext);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", ext);
  }

  @Test
  public void testAddComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setHonorificPrefix("Dr.");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "name.honorificPrefix", "Dr.");
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

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "phoneNumbers", mobilePhone);
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

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "active", false);
  }
  
  @Test
  public void testReplaceExtensionSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getExtension(EnterpriseExtension.class).setDepartment("Dept XYZ.");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department", "Dept XYZ.");
  }

  @Test
  public void testReplaceComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setFamilyName("Nobody");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "name.familyName", "Nobody");
  }

  @Test
  public void testReplaceMultiValuedAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getEmails()
         .stream()
         .filter(e -> e.getType()
                       .equals("work"))
         .forEach(e -> e.setValue("nobody@example.com"));

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "emails[type EQ \"work\"].value", "nobody@example.com");
  }

  @Test
  public void testRemoveSingleAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setUserName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "userName", null);
  }
  
  @Test
  public void testRemoveSingleExtension() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.removeExtension(EnterpriseExtension.class);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", null);
  }

  @Test
  public void testRemoveComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getName()
         .setMiddleName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "name.middleName", null);
  }

  @Test
  public void testRemoveFullComplexAttribute() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.setName(null);

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "name", null);
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

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "emails[type EQ \"home\"]", null);
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

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "addresses[type EQ \"local\"]", null);
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
    
    user2.getAddresses().add(localAddress);
    user1.getAddresses().get(0).setPostalCode("01234");

    List<PatchOperation> result = new PatchGenerator(schemaRegistry).diff(user1, user2);

    assertEquals(2, result.size());

    checkAssertions(result.get(1), Type.ADD, "addresses", localAddress);
  }
  
  @Test
  public void verifyEmptyArraysDoNotCauseMove() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    user1.setPhotos(new ArrayList<>());
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(new ArrayList<>());
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    assertTrue(operations.isEmpty(), "Empty Arrays caused a diff");
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
  
  /**
   * This unit test is to replicate the issue where
   */
  @Test
  public void testAddArray() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    Photo photo = new Photo();
    photo.setType("photo");
    photo.setValue("photo1.png");
    user2.setPhotos(Stream.of(photo).collect(Collectors.toList()));
    
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(null);
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(Stream.of(FIRST,SECOND).collect(Collectors.toList()));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    assertThat(operations)
      .hasSize(3)
      .extracting("operation","value")
      .contains(
        tuple(Type.ADD, photo),
        tuple(Type.ADD, "first"),
        tuple(Type.ADD, "second"));
  }
  
  @Test
  public void testRemoveArray() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    Photo photo = new Photo();
    photo.setType("photo");
    photo.setValue("photo1.png");
    user1.setPhotos(Stream.of(photo).collect(Collectors.toList()));
    
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(Stream.of(FIRST,SECOND).collect(Collectors.toList()));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(null);
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    assertNotNull(operations);
    assertEquals(2, operations.size());
    PatchOperation operation = operations.get(0);
    assertEquals(Type.REMOVE, operation.getOperation());
    assertNull(operation.getValue());
  }
  
  @Test
  public void testNonTypedAttributeListGetUseablePath() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(Stream.of(FIRST,SECOND,THIRD).collect(Collectors.toList()));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(Stream.of(FIRST,SECOND,FOURTH).collect(Collectors.toList()));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    assertThat(operations)
      .hasSize(1);
    PatchOperation patchOp = operations.get(0);
    PatchOperationAssert.assertThat(patchOp)
        .hasType(Type.REPLACE)
        .hashPath(ExampleObjectExtension.URN + ":list[value EQ \"third\"]")
        .hasValue(FOURTH);
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart1() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    
    user2.getName().setFormatted(nickname);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.ADD, "name.formatted", nickname)).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REMOVE, "nickName")).hasSize(1);
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

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.REPLACE, "name.formatted", nickname)).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REPLACE, "nickName", "")).hasSize(1);
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

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.REPLACE, "name.formatted", nickname)).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REMOVE, "nickName")).hasSize(1);
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

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.ADD, "name.formatted", nickname)).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REPLACE, "nickName", "")).hasSize(1);
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

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.REMOVE, "name.formatted")).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REPLACE, "nickName", nickname)).hasSize(1);
  }
  
  @ParameterizedTest
  @MethodSource("testListOfStringsParameters")
  public void testListOfStringsParameterized(List<String> list1, List<String> list2, List<ExpectedPatchOperation> ops) throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(list1);
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(list2);
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    assertEquals(ops.size(), operations.size());
    for(int i = 0; i < operations.size(); i++) {
      PatchOperation actualOp = operations.get(i);
      ExpectedPatchOperation expectedOp = ops.get(i);
      assertEquals(expectedOp.getOp(), actualOp.getOperation().toString());
      assertEquals(expectedOp.getPath(), actualOp.getPath().toString());
      if (expectedOp.getValue() == null) {
        assertNull(actualOp.getValue());
      } else {
        assertEquals(expectedOp.getValue(), actualOp.getValue().toString());
      }
    }
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
    
    List<ExpectedPatchOperation> multipleOps = new ArrayList<ExpectedPatchOperation>();
    multipleOps.add(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "A"));
    multipleOps.add(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "B"));
    multipleOps.add(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "C"));
    params.add(new Object[] {Stream.of(A).collect(Collectors.toList()), new ArrayList<String>(), Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", null)).collect(Collectors.toList())});
    params.add(new Object[] {Stream.of(A).collect(Collectors.toList()), null, Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", null)).collect(Collectors.toList())});
    params.add(new Object[] {null, Stream.of(A).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "A")).collect(Collectors.toList())});
    params.add(new Object[] {null, Stream.of(C,B,A).collect(Collectors.toList()), multipleOps});
    params.add(new Object[] {Stream.of(A,B,C).collect(Collectors.toList()), new ArrayList<String>(), Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", null)).collect(Collectors.toList())});
    params.add(new Object[] {Stream.of(C,B,A).collect(Collectors.toList()), new ArrayList<String>(), Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", null)).collect(Collectors.toList())});
    params.add(new Object[] {new ArrayList<String>(), Stream.of(A).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", "A")).collect(Collectors.toList())});
    params.add(new Object[] {new ArrayList<String>(), Stream.of(C,B,A).collect(Collectors.toList()), multipleOps});
    
    
    params.add(new Object[] {Stream.of(A, B).collect(Collectors.toList()), Stream.of(B).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list[value EQ \"A\"]", null)).collect(Collectors.toList())});
    params.add(new Object[] {Stream.of(B, A).collect(Collectors.toList()), Stream.of(B).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("REMOVE", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list[value EQ \"A\"]", null)).collect(Collectors.toList())});
    
    params.add(new Object[] {Stream.of(B).collect(Collectors.toList()), Stream.of(A,B).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", A)).collect(Collectors.toList())});
    params.add(new Object[] {Stream.of(B).collect(Collectors.toList()), Stream.of(B,A).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", A)).collect(Collectors.toList())});
    
    params.add(new Object[] {Stream.of(A).collect(Collectors.toList()), Stream.of(A,B,C).collect(Collectors.toList()), Stream.of(new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", B),new ExpectedPatchOperation("ADD", "urn:ietf:params:scim:schemas:extension:example:2.0:Object:list", C)).collect(Collectors.toList())});
    
    return params.toArray();
  }
  
  @Test
  @Disabled
  public void offsetTest1() throws Exception {
    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(Stream.of("D","M","Y","Z","Z","Z","Z","Z").collect(Collectors.toList()));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    //ext2.setList(Stream.of("A","A","B","B","D","F","N","Q","Z").collect(Collectors.toList()));
    ext2.setList(Stream.of("A","Z").collect(Collectors.toList()));
    user2.addExtension(ext2);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    System.out.println("Number of operations: "+operations.size());
    operations.stream().forEach(op -> System.out.println(op));

    // TODO this is likely a flaky test, below, "A" replaces one of the "Z" values, but per order of the lists it should be "D"
    assertThat(operations).hasSize(7);
    assertThat(operations).filteredOn(op(Type.REPLACE, ExampleObjectExtension.URN + ":list[value EQ \"Z\"]", "A")).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REMOVE, ExampleObjectExtension.URN + ":list[value EQ \"Z\"]")).hasSize(3);
    assertThat(operations).filteredOn(op(Type.REMOVE, ExampleObjectExtension.URN + ":list[value EQ \"D\"]")).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REMOVE, ExampleObjectExtension.URN + ":list[value EQ \"M\"]")).hasSize(1);
    assertThat(operations).filteredOn(op(Type.REMOVE, ExampleObjectExtension.URN + ":list[value EQ \"Y\"]")).hasSize(1);
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

    assertThat(operations).hasSize(2);
    assertThat(operations).filteredOn(op(Type.REPLACE, "name.formatted", "")).hasSize(1);
    assertThat(operations).filteredOn(op(Type.ADD, "nickName", nickname)).hasSize(1);
  }
  
  /**
   * This is used to test an error condition. In this scenario a user has multiple phone numbers where home is marked primary and work is not. A SCIM update
   * is performed in which the new user only contains a work phone number where the type is null. When this happens it should only be a single DELETE
   * operation. Instead it creates four operations: replace value of the home number with the work number value, replace the home type to work,
   * remove the primary flag, and remove the work number 
   */
  @Test
  @Disabled
  public void testShowBugWhereDeleteIsTreatedAsMultipleReplace() throws Exception {
    final int expectedNumberOfOperationsWithoutBug = 1;
    final int expectedNumberOfOperationsWithBug = 4;

    ScimUser user1 = createUser();
    ScimUser user2 = createUser();
    user2.getPhoneNumbers().removeIf(p -> p.getType().equals("home"));

    PhoneNumber workNumber = user2.getPhoneNumbers().stream().filter(p -> p.getType().equals("work")).findFirst().orElse(null);
    assertNotNull(workNumber);
    workNumber.setType(null);

    List<PatchOperation> operations = new PatchGenerator(schemaRegistry).diff(user1, user2);
    assertNotNull(operations);

    System.out.println("Number of operations: "+operations.size());
    operations.stream().forEach(op -> System.out.println(op));

    assertEquals(expectedNumberOfOperationsWithBug, operations.size());
    assertNotEquals(expectedNumberOfOperationsWithoutBug, operations.size());
  }

  private PatchOperation assertSingleResult(List<PatchOperation> result) {
    assertThat(result)
              .isNotNull();
    assertThat(result)
              .hasSize(1);
    PatchOperation actual = result.get(0);
    return actual;
  }

  private void checkAssertions(PatchOperation actual, Type op, String path, Object value) throws FilterParseException {
    assertThat(actual.getOperation())
              .isEqualTo(op);
    assertThat(actual.getPath()
                                .toString())
              .isEqualTo(path);
    assertThat(actual.getValue())
              .isEqualTo(value);
  }
  
  @Data
  @AllArgsConstructor
  private static class ExpectedPatchOperation {
    private String op;
    private String path;
    private String value;
    
    
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

    List<Address> address = Stream.of(workAddress, homeAddress)
                                  .collect(Collectors.toList());
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

    List<Email> emails = Stream.of(homeEmail, workEmail)
                               .collect(Collectors.toList());
    user.setEmails(emails);

    //"+1(814)867-5309"
    PhoneNumber homePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5309").build();
    
    homePhone.setType("home");
    homePhone.setPrimary(true);

    //"+1(814)867-5307"
    PhoneNumber workPhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5307").build();
    workPhone.setType("work");
    workPhone.setPrimary(false);

    List<PhoneNumber> phones = Stream.of(homePhone, workPhone)
                                     .collect(Collectors.toList());
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

  static class PatchOperationAssert extends AbstractAssert<PatchOperationAssert, PatchOperation> {

    public PatchOperationAssert(PatchOperation actual) {
      super(actual, PatchOperationAssert.class);
    }

    public PatchOperationAssert hashPath(String path) {
      isNotNull();
      PatchOperationPath actualPath = actual.getPath();
      if (actualPath == null || !actualPath.toString().equals(path)) {
        failWithMessage("Expecting path in:\n  <%s>\nto be:    <%s>\nbut was: <%s>", actual, actualPath, path);
      }
      return this;
    }

    public PatchOperationAssert hasType(Type opType) {
      isNotNull();
      Type actualType = actual.getOperation();
      if (!Objects.equals(actualType, opType)) {
        failWithMessage("Expecting operation type in:\n  <%s>\nto be:   <%s>\nbut was: <%s>", actual, actualType, opType);
      }
      return this;
    }

    public PatchOperationAssert hasValue(Object value) {
      isNotNull();
      Object actualValue = actual.getValue();
      if (!Objects.equals(actualValue, value)) {
        failWithMessage("Expecting value in:\n  <%s>\nto be:    <%s>\nbut was: <%s>", actual, actualValue, value);
      }
      return this;
    }

    public PatchOperationAssert nullValue() {
      return hasValue(null);
    }

    public static PatchOperationAssert assertThat(PatchOperation actual) {
      return new PatchOperationAssert(actual);
    }
  }

  static class PatchOperationCondition extends Condition<PatchOperation> {

    private final Type type;
    private final String path;
    private final Object value;

    public PatchOperationCondition(Type type, String path, Object value) {
      this.type = type;
      this.path = path;
      this.value = value;
    }

    @Override
    public boolean matches(PatchOperation patchOperation){
      return Objects.equals(type, patchOperation.getOperation()) &&
        Objects.equals(path, Objects.toString(patchOperation.getPath().toString())) &&
        Objects.equals(value, patchOperation.getValue());
    }

    static PatchOperationCondition op(Type type, String path, Object value) {
      return new PatchOperationCondition(type, path, value);
    }

    static PatchOperationCondition op(Type type, String path) {
      return op(type, path, null);
    }
  }
}
