package edu.psu.swe.scim.server.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import edu.psu.swe.scim.server.schema.Registry;
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
    
    Mockito.when(providerInstance.get()).thenReturn(provider);
    Mockito.when(provider.getExtensionList()).thenReturn(Collections.singletonList(EnterpriseExtension.class));

    providerRegistry.registerProvider(ScimUser.class, providerInstance);
  }
  
  @Test
  public void testResourcePassthrough() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithResource("1234", createUser1(), createUser2());
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
    ScimUser result = updateRequest.getResource();
    log.info("testPatchToUpdate: " + result);
    Assertions.assertThat(result)
              .isNotNull();
  }

  @Test
  public void testResourceToPatch() throws Exception {
    UpdateRequest<ScimUser> updateRequest = new UpdateRequest<>(registry);
    updateRequest.initWithResource("1234", createUser1(), createUser2());
    List<PatchOperation> result = updateRequest.getPatchOperations();
    log.info("testResourceToPatch: " + result);
    Assertions.assertThat(result)
              .isNotNull();
  }

  public static ScimUser createUser1() throws PhoneNumberParseException {
    ScimUser user = new ScimUser();
    user.setId("912345678");
    user.setExternalId("912345678");
    user.setActive(true);
    user.setDisplayName("John Anyman");
    user.setNickName(user.getDisplayName());
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
    homeEmail.setDisplay("jxa123@psu.edu");

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

    PhoneNumber workPhone = new GlobalPhoneNumberBuilder("+1(814)867-5309").build();
    workPhone.setType("work");
    workPhone.setPrimary(true);

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

  public static ScimUser createUser2() throws PhoneNumberParseException {
    ScimUser user = new ScimUser();
    user.setId("912345678");
    user.setExternalId("912345678");
    user.setActive(false);
    user.setDisplayName("John Anyman");
    user.setNickName(user.getDisplayName());
    user.setTitle("Professor");
    user.setUserName("jxa123@psu.edu");

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
    workAddress.setStreetAddress("200 Science Park Road");
    workAddress.setLocality("State College");
    workAddress.setRegion("Pennsylvania");
    workAddress.setCountry("USA");
    workAddress.setPostalCode("16803");

    List<Address> address = Stream.of(homeAddress, workAddress)
                                  .collect(Collectors.toList());
    // List<Address> address =
    // Stream.of(workAddress,homeAddress).collect(Collectors.toList());
    user.setAddresses(address);

    Email workEmail = new Email();
    workEmail.setPrimary(true);
    workEmail.setType("work");
    workEmail.setValue("jxa123@psu.edu");
    workEmail.setDisplay(null);

    Email homeEmail = new Email();
    homeEmail.setPrimary(true);
    homeEmail.setType("home");
    homeEmail.setValue("john@hotmail.com");
    homeEmail.setDisplay("jxa123@psu.edu");

    List<Email> emails = Stream.of(homeEmail, workEmail)
                               .collect(Collectors.toList());
    user.setEmails(emails);

    PhoneNumber homePhone = new GlobalPhoneNumberBuilder("+1(814)867-5309").build();
    homePhone.setType("home");
    homePhone.setPrimary(true);

    PhoneNumber workPhone = new GlobalPhoneNumberBuilder("+1(814)867-5309").build();
    workPhone.setType("work");
    workPhone.setPrimary(true);

    List<PhoneNumber> phones = Stream.of(homePhone, workPhone)
                                     .collect(Collectors.toList());
    user.setPhoneNumbers(phones);

    EnterpriseExtension enterpriseExtension = new EnterpriseExtension();
    enterpriseExtension.setEmployeeNumber("1234");
    enterpriseExtension.setDepartment("Dept A.");
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
    removePhoneNumberOp.setOpreration(Type.REMOVE);
    removePhoneNumberOp.setPath(new PatchOperationPath("phoneNumbers[type eq \"home\"]"));
    patchOperations.add(removePhoneNumberOp);
    return patchOperations;
  }

}
