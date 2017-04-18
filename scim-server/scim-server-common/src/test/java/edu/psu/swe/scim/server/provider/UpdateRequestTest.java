package edu.psu.swe.scim.server.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.psu.swe.scim.server.rest.ObjectMapperContextResolver;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.server.utility.ExampleObjectExtension;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension.Manager;
import edu.psu.swe.scim.spec.extension.ScimExtensionRegistry;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParseException;
import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import edu.psu.swe.scim.spec.protocol.data.PatchOperation.Type;
import edu.psu.swe.scim.spec.protocol.data.PatchOperationPath;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.resources.Address;
import edu.psu.swe.scim.spec.resources.Email;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.PhoneNumber;
import edu.psu.swe.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import edu.psu.swe.scim.spec.resources.ScimUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateRequestTest {

  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();
  private Registry registry;

  @Mock
  Provider<ScimUser> provider;

  @Mock
  Instance<Provider<ScimUser>> providerInstance;

  ProviderRegistry providerRegistry;

  @Before
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
  @Ignore
  public void testPatchToUpdate() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithPatch("1234", createUser1(), createUser1PatchOps());
    ScimUser result = updateRequest.getResource();
    log.info("testPatchToUpdate: " + result);
    Assertions.assertThat(result)
              .isNotNull();
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
    PhoneNumber mobilePhone = new GlobalPhoneNumberBuilder("+1(814)867-5306").build();
    mobilePhone.setType("mobile");
    mobilePhone.setPrimary(false);
    user2.getPhoneNumbers().add(mobilePhone);

    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> result = updateRequest.getPatchOperations();

    PatchOperation actual = assertSingleResult(result);

    checkAssertions(actual, Type.ADD, "phoneNumbers", mobilePhone);
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
  public void forceMoveError() throws Exception {
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
    try {
      updateRequest.getPatchOperations();
      Assert.fail("There should have been a runtime error where PatchOperation is a move");
    } catch (IllegalStateException e) {
      Assert.assertEquals("Error creating the patch list", e.getMessage());
    }
  }
  
  /**
   * This unit test is to replicate the issue where
   */
  @Test
  public void testAddArray() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    user1.setPhotos(new ArrayList<>());
    ScimUser user2 = copy(user1);
    
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(null);
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(new ArrayList<String>());
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    Assert.assertNotNull(operations);
    Assert.assertEquals(1, operations.size());
    PatchOperation operation = operations.get(0);
    Assert.assertNotNull(operation.getValue());
    Assert.assertEquals(Type.ADD, operation.getOperation());
    Assert.assertEquals(ArrayList.class, operation.getValue().getClass());
  }
  
  @Test
  public void testRemoveArray() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);

    ScimUser user1 = createUser1();
    user1.setPhotos(new ArrayList<>());
    ScimUser user2 = copy(user1);
    
    
    ExampleObjectExtension ext1 = new ExampleObjectExtension();
    ext1.setList(new ArrayList<String>());
    user1.addExtension(ext1);
    
    ExampleObjectExtension ext2 = new ExampleObjectExtension();
    ext2.setList(null);
    user2.addExtension(ext2);
    
    updateRequest.initWithResource("1234", user1, user2);
    List<PatchOperation> operations = updateRequest.getPatchOperations();
    Assert.assertNotNull(operations);
    Assert.assertEquals(1, operations.size());
    PatchOperation operation = operations.get(0);
    Assert.assertEquals(Type.REMOVE, operation.getOperation());
    Assert.assertNull(operation.getValue());
  }

  private PatchOperation assertSingleResult(List<PatchOperation> result) {
    Assertions.assertThat(result)
              .isNotNull();
    Assertions.assertThat(result)
              .hasSize(1);
    PatchOperation actual = result.get(0);
    return actual;
  }

  private void checkAssertions(PatchOperation actual, Type op, String path, Object value) throws FilterParseException {
    Assertions.assertThat(actual.getOperation())
              .isEqualTo(op);
    Assertions.assertThat(actual.getPath()
                                .toString())
              .isEqualTo(path);
    Assertions.assertThat(actual.getValue())
              .isEqualTo(value);
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

    PhoneNumber homePhone = new GlobalPhoneNumberBuilder("+1(814)867-5309").build();
    homePhone.setType("home");
    homePhone.setPrimary(true);

    PhoneNumber workPhone = new GlobalPhoneNumberBuilder("+1(814)867-5307").build();
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
    ObjectMapperContextResolver omcr = new ObjectMapperContextResolver();
    ObjectMapper objMapper = omcr.getContext(null);
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
