package org.apache.directory.scim.server.utility.patch;

import static org.apache.directory.scim.test.helpers.ScimTestHelper.createRegistry;
import static org.apache.directory.scim.test.helpers.ScimTestHelper.validateExtensions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.helpers.ScimTestHelper;
import org.apache.directory.scim.test.helpers.builder.AddressBuilder;
import org.apache.directory.scim.test.helpers.builder.EmailBuilder;
import org.apache.directory.scim.test.helpers.builder.NameBuilder;
import org.apache.directory.scim.test.helpers.builder.PatchOperationBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PatchOperationsTest {
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
  };

  Registry registry;
  ObjectMapper objectMapper;

  PatchOperations patchOperations;

  @BeforeEach
  void setUp() throws Exception {
    this.registry = createRegistry();
    this.objectMapper = new ObjectMapperFactory(registry).createObjectMapper();

    this.patchOperations = new PatchOperations(registry);
  }

  @AfterEach
  void tearDown() {
  }

  // SCIM USER PATCH ADD -----------------------------------------------------------------------------
  @ParameterizedTest
  @CsvSource({"displayName, DisplayName", "locale, Locale", "nickName, NickName",
    "profileUrl, http://example.com/Users/ProfileUrl", "preferredLanguage, PreferredLanguage",
    "timezone, Timezone", "title, Title", "userType, UserType"})
  void apply_simpleAttributeAdd_successfullyPatched(final String path, final String patchValue) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(patchValue)
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(ScimTestHelper.getValueByAttributeName(result, path)).isEqualTo(patchValue);
  }

  @ParameterizedTest
  @CsvSource({"displayName, DisplayName", "locale, Locale", "nickName, NickName",
    "profileUrl, http://example.com/Users/ProfileUrl", "preferredLanguage, PreferredLanguage",
    "timezone, Timezone", "title, Title", "userType, UserType"})
  void apply_simpleAttributeAddAsMap_successfullyPatched(final String path, final String patchValue) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .value(ImmutableMap.of(path, patchValue))
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(ScimTestHelper.getValueByAttributeName(result, path)).isEqualTo(patchValue);
  }

  @ParameterizedTest
  @CsvSource({"active, true", "active, false"})
  void apply_toggleActiveFlag(final String path, final String patchValue)
    throws Exception {

    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .value(patchValue)
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getActive()).isEqualTo(Boolean.valueOf(patchValue));
  }

  @ParameterizedTest
  @CsvSource({"active, true", "active, false"})
  void apply_toggleActiveFlagAsMap(final String path, final String patchValue)
    throws Exception {

    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .value(ImmutableMap.of(path, patchValue))
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getActive()).isEqualTo(Boolean.valueOf(patchValue));
  }

  @Test
  void apply_complexAddressAttributeAdd_successfullyPatched() throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    Map<String, Object> values = objectMapper.convertValue(AddressBuilder.builder()
      .country("US")
      .display("Home Address")
      .formatted("2571 Wallingford Dr, Beverly Hills, CA 90210")
      .locality("Beverly Hills")
      .postalCode("90210")
      .region("CA")
      .streetAddress("2571 Wallingford Dr")
      .type("home")
      .primary(true)
      .build(), MAP_TYPE);

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .value(ImmutableMap.of("addresses", Lists.newArrayList(values)))
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getAddresses()).hasSize(1);

    final Address address = result.getAddresses().get(0);

    assertThat(address.getCountry()).isEqualTo("US");
    assertThat(address.getDisplay()).isEqualTo("Home Address");
    assertThat(address.getFormatted()).isEqualTo("2571 Wallingford Dr, Beverly Hills, CA 90210");
    assertThat(address.getLocality()).isEqualTo("Beverly Hills");
    assertThat(address.getPostalCode()).isEqualTo("90210");
    assertThat(address.getRegion()).isEqualTo("CA");
    assertThat(address.getStreetAddress()).isEqualTo("2571 Wallingford Dr");
    assertThat(address.getType()).isEqualTo("home");
    assertThat(address.getPrimary()).isTrue();
  }

  @Test
  void apply_complexEmailAttributeAdd_successfullyPatched() throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    Map<String, Object> values = objectMapper.convertValue(EmailBuilder.builder()
      .value("first.last@example.com")
      .display("Home Email Address")
      .type("home")
      .primary(true)
      .build(), MAP_TYPE);

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .value(ImmutableMap.of("emails", Lists.newArrayList(values)))
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getEmails()).hasSize(1);

    final Email email = result.getEmails().get(0);

    assertThat(email.getValue()).isEqualTo("first.last@example.com");
    assertThat(email.getDisplay()).isEqualTo("Home Email Address");
    assertThat(email.getType()).isEqualTo("home");
    assertThat(email.getPrimary()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("EmailArguments")
  void apply_complexEmailAttributeReplace_successfullyPatched(final String filter,
                                                              final String patchAttrName,
                                                              final Object patchValue,
                                                              int index)
    throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();

    List<Email> list = new ArrayList<>();
    user.setEmails(list);

    user.getEmails().add(0, EmailBuilder.builder()
      .primary(false)
      .type("work")
      .build());
    user.getEmails().add(1, EmailBuilder.builder()
      .primary(false)
      .type("home")
      .build());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(String.format("%s.%s", filter, patchAttrName))
      .value(patchValue)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getEmails()).hasSize(2);

    Email patched = result.getEmails().get(index);
    Object valuePatched = ScimTestHelper.getValueByAttributeName(patched, patchAttrName);

    assertThat(patchValue).isEqualTo(valuePatched);
  }

  @ParameterizedTest
  @CsvSource({"name.familyName, LastName", "name.givenName, GivenName", "name.middleName, MiddleName"})
  void apply_complexNameAttributeAdd_successfullyPatched(final String path, final String patchValue)
    throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(patchValue)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();

    assertThat(ScimTestHelper.getValueByAttributeName(result.getName(),
      path.substring(path.indexOf(".") + 1))).isEqualTo(patchValue);
  }

  @Test
  void apply_complexNameAttributeAsMapAdd_successfullyPatched()
    throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    final Name name = NameBuilder.builder()
      .familyName("FamilyName")
      .middleName("MiddleName")
      .givenName("GivenName")
      .build();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path("name")
      .value(this.objectMapper.convertValue(name, MAP_TYPE))
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();
    assertThat(result.getName()).isEqualTo(name);
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":department, Department",
    EnterpriseExtension.URN + ":division, Division",
    EnterpriseExtension.URN + ":costCenter, CostCenter",
    EnterpriseExtension.URN + ":employeeNumber, EmployeeNumber",
    EnterpriseExtension.URN + ":organization, Organization"})
  void apply_enterpriseExtensionAttributeAdd_successfullyPatched(final String path, final Object value) throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(value)
      .build();

    assertThat(user.getExtension(EnterpriseExtension.URN)).isNotNull();

    try {
      final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

      assertThat(result).isNotNull();
      assertThat(result.getExtensions()).isNotNull();
      assertThat(result.getExtensions()).hasSize(2);

      validateExtensions(path, value, result.getExtension(EnterpriseExtension.class));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":manager.displayName, DisplayName",
    EnterpriseExtension.URN + ":manager.value, Value"})
  void apply_complexExtensionAttributeAdd_successfullyPatched(final String path, final Object value) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(value)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(1);
    assertThat(result.getExtension(EnterpriseExtension.class)).isNotNull();

    validateExtensions(path, value, result.getExtension(EnterpriseExtension.class));
  }

  // SCIM GROUP PATCH ADD -----------------------------------------------------------------------------

  // SCIM USER PATCH REPLACE -----------------------------------------------------------------------------
  @ParameterizedTest
  @CsvSource({"displayName, DisplayName", "locale, Locale", "nickName, NickName",
    "profileUrl, http://example.com/Users/ProfileUrl", "preferredLanguage, PreferredLanguage",
    "timezone, Timezone", "title, Title", "userType, UserType"})
  void apply_simpleAttributeReplace_successfullyPatched(final String path, final String patchValue) throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .value(patchValue)
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(ScimTestHelper.getValueByAttributeName(result, path)).isEqualTo(patchValue);
  }

  @ParameterizedTest
  @CsvSource({"name.familyName, LastName", "name.givenName, GivenName", "name.middleName, MiddleName"})
  void apply_complexNameAttributeReplace_successfullyPatched(final String path, final String patchValue)
    throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .value(patchValue)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();

    assertThat(ScimTestHelper.getValueByAttributeName(result.getName(),
      path.substring(path.indexOf(".") + 1))).isEqualTo(patchValue);
  }

  @ParameterizedTest
  @MethodSource("AddressArguments")
  void apply_complexAddressAttributeReplace_successfullyPatched(final String filter,
                                                                final String patchAttrName,
                                                                final Object patchValue,
                                                                int index)
    throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();

    List<Address> list = new ArrayList<>();
    user.setAddresses(list);

    user.getAddresses().add(0, AddressBuilder.builder()
      .primary(false)
      .type("work")
      .build());
    user.getAddresses().add(1, AddressBuilder.builder()
      .primary(false)
      .type("home")
      .build());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(String.format("%s.%s", filter, patchAttrName))
      .value(patchValue)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getAddresses()).hasSize(2);

    Address patched = result.getAddresses().get(index);
    Object valuePatched = ScimTestHelper.getValueByAttributeName(patched, patchAttrName);

    assertThat(patchValue).isEqualTo(valuePatched);
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":department",
    EnterpriseExtension.URN + ":division",
    EnterpriseExtension.URN + ":costCenter",
    EnterpriseExtension.URN + ":employeeNumber",
    EnterpriseExtension.URN + ":organization"})
  void apply_enterpriseExtensionAttributeReplace_successfullyPatched(final String path) throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();
    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .build();

    assertThat(user.getExtension(EnterpriseExtension.URN)).isNotNull();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(2);

    validateExtensions(path, null, result.getExtension(EnterpriseExtension.class));
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":manager.displayName", EnterpriseExtension.URN + ":manager.value"})
  void apply_complexExtensionAttributeReplace_successfullyPatched(final String path) throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(2);
    assertThat(result.getExtension(EnterpriseExtension.class)).isNotNull();

    validateExtensions(path, null, result.getExtension(EnterpriseExtension.class));
  }

  // SCIM GROUP PATCH REPLACE -----------------------------------------------------------------------------

  // SCIM USER PATCH REMOVE -----------------------------------------------------------------------------
  @ParameterizedTest
  @CsvSource({"displayName", "locale", "nickName", "profileUrl", "preferredLanguage", "timezone", "title", "userType"})
  void apply_simpleAttributeRemove_successfullyPatched(final String path) throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    Map<String, Object> resultAsMap = this.objectMapper.convertValue(result, MAP_TYPE);

    assertThat(resultAsMap.get(path)).isNull();
  }

  @ParameterizedTest
  @MethodSource("AddressRemoveArguments")
  void apply_complexAddressAttributeRemove_successfullyPatched(final String filter,
                                                               final String patchAttrName,
                                                               int index) throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();

    List<Address> list = new ArrayList<>();
    user.setAddresses(list);

    user.getAddresses().add(0, AddressBuilder.builder()
      .primary(false)
      .type("work")
      .country("US")
      .region("CO")
      .locality("Broomfield")
      .streetAddress("11800 Ridge Parkway")
      .postalCode("80020")
      .build());
    user.getAddresses().add(1, AddressBuilder.builder()
      .primary(false)
      .type("home")
      .country("US")
      .region("MN")
      .locality("Minneapolis")
      .streetAddress("Washington Avenue South")
      .postalCode("55401")
      .build());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(String.format("%s.%s", filter, patchAttrName))
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getAddresses()).hasSize(2);

    Address patched = result.getAddresses().get(index);
    Object patchedValue = ScimTestHelper.getValueByAttributeName(patched, patchAttrName);

    assertThat(patchedValue).isNull();
  }

  @ParameterizedTest
  @MethodSource("EmailRemoveArguments")
  void apply_complexEmailAttributeRemove_successfullyPatched(final String filter,
                                                             final String patchAttrName,
                                                             int index) throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();

    List<Email> list = new ArrayList<>();
    user.setEmails(list);

    user.getEmails().add(0, EmailBuilder.builder()
      .primary(false)
      .type("work")
      .display("display")
      .value("value")
      .build());
    user.getEmails().add(1, EmailBuilder.builder()
      .primary(false)
      .type("home")
      .display("display")
      .value("value")
      .build());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(String.format("%s.%s", filter, patchAttrName))
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getEmails()).hasSize(2);

    Email patched = result.getEmails().get(index);
    Object valuePatched = ScimTestHelper.getValueByAttributeName(patched, patchAttrName);

    assertThat(valuePatched).isNull();
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @CsvSource({"name.familyName", "name.givenName", "name.middleName"})
  void apply_complexNameAttributeRemove_successfullyPatched(final String path)
    throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();
    final Map<String, Object> resultAsMap = this.objectMapper.convertValue(result, MAP_TYPE);

    final AttributeReference reference = new AttributeReference(user.getBaseUrn(), path);

    assertThat(reference.getAttributeName()).isNotNull();
    assertThat(reference.getSubAttributeName()).isNotNull();

    final Map<String, Object> nameAsMap = (Map<String, Object>) resultAsMap.get(reference.getAttributeName());
    assertThat(nameAsMap.size()).isEqualTo(5);
    assertThat(nameAsMap.get(reference.getSubAttributeName())).isNull();
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":department",
    EnterpriseExtension.URN + ":division",
    EnterpriseExtension.URN + ":costCenter",
    EnterpriseExtension.URN + ":employeeNumber",
    EnterpriseExtension.URN + ":organization"})
  void apply_enterpriseExtensionAttributeRemove_successfullyPatched(final String path) throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();
    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    assertThat(user.getExtension(EnterpriseExtension.URN)).isNotNull();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(2);

    validateExtensions(path, null, result.getExtension(EnterpriseExtension.class));
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":manager.displayName", EnterpriseExtension.URN + ":manager.value"})
  void apply_complexExtensionAttributeRemove_successfullyPatched(final String path) throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(2);
    assertThat(result.getExtension(EnterpriseExtension.class)).isNotNull();

    validateExtensions(path, null, result.getExtension(EnterpriseExtension.class));
  }

  @ParameterizedTest
  @CsvSource({"userName"})
  void apply_removeMutableAttribute_exceptionThrow(final String path) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    Throwable t = catchThrowable(() -> this.patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    ScimTestHelper.assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.MUTABILITY,
      ErrorMessageType.MUTABILITY.getDetail());
  }

  @ParameterizedTest
  @CsvSource({"active"})
  void apply_removeOperationOnAttributeNotSupported_exceptionThrow(final String path) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path(path)
      .build();

    Throwable t = catchThrowable(() -> this.patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    ScimTestHelper.assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_SYNTAX,
      ErrorMessageType.INVALID_SYNTAX.getDetail());
  }

  // SCIM GROUP PATCH REMOVE -----------------------------------------------------------------------------

  // HELPER METHODS -----------------------------------------------------------------------------
  private static Stream<Arguments> AddressArguments() {
    return Stream.of(
      Arguments.of("addresses[type eq \"work\"]", "primary", true, 0),
      Arguments.of("addresses[type eq \"work\"]", "country", "AnyCountry", 0),
      Arguments.of("addresses[type EQ \"work\"]", "region", "AnyState", 0),
      Arguments.of("addresses[type EQ \"work\"]", "locality", "AnyCity", 0),
      Arguments.of("addresses[type EQ \"work\"]", "streetAddress", "123 Any Street", 0),
      Arguments.of("addresses[type EQ \"work\"]", "postalCode", "AnyPostCode", 0),
      Arguments.of("addresses[type eq \"home\"]", "primary", true, 1),
      Arguments.of("addresses[type eq \"home\"]", "country", "AnyCountry", 1),
      Arguments.of("addresses[type EQ \"home\"]", "region", "AnyState", 1),
      Arguments.of("addresses[type EQ \"home\"]", "locality", "AnyCity", 1),
      Arguments.of("addresses[type EQ \"home\"]", "streetAddress", "123 Any Street", 1),
      Arguments.of("addresses[type EQ \"home\"]", "postalCode", "AnyPostCode", 1));
  }

  private static Stream<Arguments> AddressRemoveArguments() {
    return Stream.of(
      Arguments.of("addresses[type eq \"work\"]", "country", 0),
      Arguments.of("addresses[type EQ \"work\"]", "region", 0),
      Arguments.of("addresses[type EQ \"work\"]", "locality", 0),
      Arguments.of("addresses[type EQ \"work\"]", "streetAddress", 0),
      Arguments.of("addresses[type EQ \"work\"]", "postalCode", 0),
      Arguments.of("addresses[type eq \"home\"]", "country", 1),
      Arguments.of("addresses[type EQ \"home\"]", "region", 1),
      Arguments.of("addresses[type EQ \"home\"]", "locality", 1),
      Arguments.of("addresses[type EQ \"home\"]", "streetAddress", 1),
      Arguments.of("addresses[type EQ \"home\"]", "postalCode", 1));
  }

  private static Stream<Arguments> EmailArguments() {
    return Stream.of(
      Arguments.of("emails[type eq \"work\"]", "value", "ReplacedValue", 0),
      Arguments.of("emails[type EQ \"work\"]", "display", "ReplacedValue", 0),
      Arguments.of("emails[type eq \"home\"]", "value", "ReplacedValue", 1),
      Arguments.of("emails[type EQ \"home\"]", "display", "ReplacedValue", 1));
  }

  private static Stream<Arguments> EmailRemoveArguments() {
    return Stream.of(
      Arguments.of("emails[type eq \"work\"]", "value", 0),
      Arguments.of("emails[type EQ \"work\"]", "display", 0),
      Arguments.of("emails[type eq \"home\"]", "value", 1),
      Arguments.of("emails[type EQ \"home\"]", "display", 1));
  }
}
