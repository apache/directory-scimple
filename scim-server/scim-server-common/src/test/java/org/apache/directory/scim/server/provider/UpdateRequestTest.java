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

package org.apache.directory.scim.server.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.ExampleObjectExtension;
import org.apache.directory.scim.server.utility.Subobject;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.*;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.inject.Instance;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateRequestTest {
  
  private static final String FIRST = "first";
  private static final String SECOND = "second";
  private static final String THIRD = "third";
  private static final String FOURTH = "fourth";
  
  private static final String A = "A";
  private static final String B = "B";
  private static final String C = "C";

  private Registry registry;

  @Mock
  Provider<ScimUser> provider;

  @Mock
  Instance<Provider<ScimUser>> providerInstance;

  ProviderRegistry providerRegistry;

  @BeforeEach
  public void initialize() throws Exception {
    providerRegistry = new ProviderRegistry();
    registry = new Registry();

    providerRegistry.registry = registry;
    providerRegistry.scimExtensionRegistry = ScimExtensionRegistry.getInstance();

    Mockito.when(providerInstance.get())
           .thenReturn(provider);
    Mockito.when(provider.getExtensionList())
           .thenReturn(Stream.of(EnterpriseExtension.class,ExampleObjectExtension.class).collect(Collectors.toList()));

    providerRegistry.registerProvider(ScimUser.class, providerInstance);
  }

  @Test
  public void testResourcePassthrough() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithResource("1234", createUser1(), createUser1());
    ScimUser result = updateRequest.getResource();
    log.info("testResourcePassthrough: " + result);
    Assertions.assertThat(result)
              .isNotNull();
  }

  @Test
  public void testPatchPassthrough() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithPatch("1234", createUser1(), createUser1PatchOps());
    List<PatchOperation> result = updateRequest.getPatchOperations();
    log.info("testPatchPassthrough: " + result);
    Assertions.assertThat(result)
              .isNotNull();
  }

  @Test
  public void testPatchToUpdate() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithPatch("1234", createUser1(), createUser1PatchOps());

    List<PatchOperation> result = updateRequest.getPatchOperations();
    log.info("testPatchToUpdate: " + result);
    Assertions.assertThat(result).isNotNull();
  }

  @Test
  public void testAddSingleAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.setNickName("Jon");

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "nickName", "Jon");
  }
  
  @Test
  public void testAddSingleExtension() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    EnterpriseExtension ext = user1.removeExtension(EnterpriseExtension.class);
    ScimUser user2 = copy(user1);
    user2.addExtension(ext);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", ext);
  }

  @Test
  public void testAddComplexAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.getName()
         .setHonorificPrefix("Dr.");

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "name.honorificPrefix", "Dr.");
  }

  @Test
  public void testAddMultiValuedAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(false);
    user2.getPhoneNumbers().add(mobilePhone);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

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
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    user1.setPhoneNumbers(new ArrayList<>());
    ScimUser user2 = copy(user1);
    
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(true);
    user2.getPhoneNumbers().add(mobilePhone);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    assertNotNull(operations);
    assertEquals(1, operations.size());
    PatchOperation operation = operations.get(0);
    assertNotNull(operation.getValue());
    assertEquals(Type.ADD, operation.getOperation());
    assertEquals(PhoneNumber.class, operation.getValue().getClass());
  }
  
  @Test
  public void testAddObjectsToEmptyCollection() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    user1.setPhoneNumbers(new ArrayList<>());
    ScimUser user2 = copy(user1);
    
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(true);
    
    PhoneNumber homePhone = new GlobalPhoneNumberBuilder().globalNumber("+1(814)867-5307").build();
    homePhone.setType("home");
    homePhone.setPrimary(true);
    
    user2.getPhoneNumbers().add(mobilePhone);
    user2.getPhoneNumbers().add(homePhone);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
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
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.setActive(false);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "active", false);
  }
  
  @Test
  public void testReplaceExtensionSingleAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.getExtension(EnterpriseExtension.class).setDepartment("Dept XYZ.");

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department", "Dept XYZ.");
  }

  @Test
  public void testReplaceComplexAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.getName()
         .setFamilyName("Nobody");

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "name.familyName", "Nobody");
  }

  @Test
  public void testReplaceMultiValuedAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.getEmails()
         .stream()
         .filter(e -> e.getType()
                       .equals("work"))
         .forEach(e -> e.setValue("nobody@example.com"));

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REPLACE, "emails[type EQ \"work\"].value", "nobody@example.com");
  }

  @Test
  public void testRemoveSingleAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.setUserName(null);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "userName", null);
  }
  
  @Test
  public void testRemoveSingleExtension() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.removeExtension(EnterpriseExtension.class);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", null);
  }

  @Test
  public void testRemoveComplexAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.getName()
         .setMiddleName(null);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "name.middleName", null);
  }

  @Test
  public void testRemoveFullComplexAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    user2.setName(null);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "name", null);
  }

  @Test
  public void testRemoveMultiValuedAttribute() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    List<Email> newEmails = user2.getEmails()
                                 .stream()
                                 .filter(e -> e.getType()
                                               .equals("work"))
                                 .collect(Collectors.toList());
    user2.setEmails(newEmails);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "emails[type EQ \"home\"]", null);
  }
  
  @Test
  public void testRemoveMultiValuedAttributeWithSorting() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    Address localAddress = new Address();
    localAddress.setStreetAddress("123 Main Street");
    localAddress.setLocality("State College");
    localAddress.setRegion("PA");
    localAddress.setCountry("USA");
    localAddress.setType("local");
    
    user1.getAddresses().add(localAddress);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.REMOVE, "addresses[type EQ \"local\"]", null);
  }
  
  @Test
  public void testAddMultiValuedAttributeWithSorting() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    Address localAddress = new Address();
    localAddress.setStreetAddress("123 Main Street");
    localAddress.setLocality("State College");
    localAddress.setRegion("PA");
    localAddress.setCountry("USA");
    localAddress.setType("local");
    
    user2.getAddresses().add(localAddress);
    user1.getAddresses().get(0).setKey("asdf");
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    assertEquals(2, result.size());

    checkAssertions(result.get(1), Type.ADD, "addresses", localAddress);
  }
  
  @Test
  public void verifyEmptyArraysDoNotCauseMove() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    user1.setPhotos(new ArrayList<>());
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(new ArrayList<>());
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    assertTrue(operations.isEmpty(), "Empty Arrays caused a diff");
  }
  
  @Test
  public void verifyEmptyArraysAreNulled() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    //Set empty list on root object and verify no differences
    user1.setPhotos(new ArrayList<>());
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
    
    //Reset user 1 and empty list on Extension and verify no differences
    user1 = createUser1();
    ExampleObjectExtension ext = new ExampleObjectExtension();
    ext.setList(new ArrayList<>());
    updateRequest.initWithResource("1234", user1, user2);
    operations = updateRequest.getPatchOperations();
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
    
    //Reset extension and set empty list on element of extension then verify no differences
    Subobject subobject = new Subobject();
    subobject.setList1(new ArrayList<>());
    ext = new ExampleObjectExtension();
    ext.setSubobject(subobject);
    updateRequest.initWithResource("1234", user1, user2);
    operations = updateRequest.getPatchOperations();
    assertTrue(operations.isEmpty(), "Empty Arrays are not being nulled out");
  }
  
  /**
   * This unit test is to replicate the issue where
   */
  @Test
  public void testAddArray() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
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
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    assertNotNull(operations);
    assertEquals(3, operations.size());
    PatchOperation operation = operations.get(0);
    assertNotNull(operation.getValue());
    assertEquals(Type.ADD, operation.getOperation());
    assertEquals(Photo.class, operation.getValue().getClass());
  }
  
  @Test
  public void testRemoveArray() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
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
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    assertNotNull(operations);
    assertEquals(2, operations.size());
    PatchOperation operation = operations.get(0);
    assertEquals(Type.REMOVE, operation.getOperation());
    assertNull(operation.getValue());
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testNonTypedAttributeListGetUseablePath() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(Stream.of(FIRST,SECOND,THIRD).collect(Collectors.toList()));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(Stream.of(FIRST,SECOND,FOURTH).collect(Collectors.toList()));
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
    
    //TODO: perform assert that proper add and remove paths are correct
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testMoveFormatNameToNicknamePart1() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    
    user2.getName().setFormatted(nickname);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testMoveFormatNameToNicknamePart2() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName("");
    
    user2.getName().setFormatted(nickname);
    user1.getName().setFormatted("");
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testMoveFormatNameToNicknamePart3() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName(null);
    
    user2.getName().setFormatted(nickname);
    user1.getName().setFormatted("");
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testMoveFormatNameToNicknamePart4() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName(nickname);
    user2.setNickName("");
    
    user2.getName().setFormatted(nickname);
    user1.getName().setFormatted(null);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @Test
  @Disabled
  //TODO: do asserts
  public void testMoveFormatNameToNicknamePart5() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName("");
    user2.setNickName(nickname);
    
    user2.getName().setFormatted(null);
    user1.getName().setFormatted(nickname);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @ParameterizedTest
  @MethodSource("testListOfStringsParameters")
  public void testListOfStringsParameterized(List<String> list1, List<String> list2, List<ExpectedPatchOperation> ops) throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(list1);
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(list2);
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
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
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  @SuppressWarnings("unused")
  private static Object[] testListOfStringsParameters() {
    List<Object> params = new ArrayList<>();
    String nickName = "John Xander Anyman";
    //Parameter order
    //1 Original list of Strings
    //2 Update list of Strings
    //3 Array of Expected Operations
    //  3a Operation
    //  3b Path
    //  3c Value
    
    List<ExpectedPatchOperation> multipleOps = new ArrayList<>();
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
  //TODO: do parameterized test
  public void offsetTest1() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(Stream.of("D","M","Y","Z","Z","Z","Z","Z").collect(Collectors.toList()));
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    //ext2.setList(Stream.of("A","A","B","B","D","F","N","Q","Z").collect(Collectors.toList()));
    ext2.setList(Stream.of("A","Z").collect(Collectors.toList()));
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
    
  }
  
  @Test
  public void testMoveFormatNameToNicknamePart6() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    ScimUser user2 = copy(user1);
    
    String nickname = "John Xander Anyman";
    user1.setNickName(null);
    user2.setNickName(nickname);
    
    user2.getName().setFormatted("");
    user1.getName().setFormatted(nickname);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    System.out.println("Number of operations: "+operations.size());
    operations.forEach(System.out::println);
  }
  
  /**
   * This is used to test an error condition. In this scenario a user has multiple phone numbers where home is marked primary and work is not. A SCIM update
   * is performed in which the new user only contains a work phone number where the type is null. When this happens it should only only be a single DELETE 
   * operation. Instead it creates four operations: replace value of the home number with the work number value, replace the home type to work,
   * remove the primary flag, and remove the work number 
   */
  @Test
  @Disabled
  public void testShowBugWhereDeleteIsTreatedAsMultipleReplace() {
//    final int expectedNumberOfOperationsWithoutBug = 1;
//    final int expectedNumberOfOperationsWithBug = 4;
//    
//    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
//    ScimUser user1 = createUser1();
//    ScimUser user2 = copy(user1);
//    user2.getPhoneNumbers().removeIf(p -> p.getType().equals("home"));
//    
//    PhoneNumber workNumber = user2.getPhoneNumbers().stream().filter(p -> p.getType().equals("work")).findFirst().orElse(null);
//    workNumber.setType("home");
//    assertNotNull(workNumber);
//    
//    updateRequest.initWithResource("1234", user1, user2);
//    List<PatchOperation> operations = updateRequest.getPatchOperations();
//    assertNotNull(operations);
//    assertEquals(expectedNumberOfOperationsWithBug, operations.size());
//    assertNotEquals(expectedNumberOfOperationsWithoutBug, operations.size());
  }

  private PatchOperation assertSingleResult(List<PatchOperation> result) {
    Assertions.assertThat(result)
              .isNotNull();
    Assertions.assertThat(result)
              .hasSize(1);
    return result.get(0);
  }

  private void checkAssertions(PatchOperation actual, Type op, String path, Object value) {
    Assertions.assertThat(actual.getOperation())
              .isEqualTo(op);
    Assertions.assertThat(actual.getPath()
                                .toString())
              .isEqualTo(path);
    Assertions.assertThat(actual.getValue())
              .isEqualTo(value);
  }
  
  @Data
  @AllArgsConstructor
  private static class ExpectedPatchOperation {
    private String op;
    private String path;
    private String value;
    
    
  }

  @SuppressWarnings("unused")
  public static Address createHomeAddress() {
    Address homeAddress = new Address();
    homeAddress.setType("home");
    homeAddress.setStreetAddress("123 Fake Street");
    homeAddress.setLocality("State College");
    homeAddress.setRegion("Pennsylvania");
    homeAddress.setCountry("USA");
    homeAddress.setPostalCode("16801");
    return homeAddress;
  }

  public static ScimUser createUser1() throws PhoneNumberParseException {
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

  private ScimUser copy(ScimUser scimUser) throws IOException {
    ObjectMapper objMapper = new ObjectMapperFactory(registry).createObjectMapper();
    String json = objMapper.writeValueAsString(scimUser);
    return objMapper.readValue(json, ScimUser.class);
  }

  private List<PatchOperation> createUser1PatchOps() throws FilterParseException {
    List<PatchOperation> patchOperations = new ArrayList<>();
    PatchOperation removePhoneNumberOp = new PatchOperation();
    removePhoneNumberOp.setOperation(Type.REMOVE);
    removePhoneNumberOp.setPath(new PatchOperationPath("phoneNumbers[type eq \"home\"]"));
    patchOperations.add(removePhoneNumberOp);
    return patchOperations;
  }

}
