package org.apache.directory.scim.server.utility.patch;

import static org.apache.directory.scim.test.helpers.ScimTestHelper.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.test.helpers.ScimTestHelper;
import org.apache.directory.scim.test.helpers.builder.AddressBuilder;
import org.apache.directory.scim.test.helpers.builder.EmailBuilder;
import org.apache.directory.scim.test.helpers.builder.NameBuilder;
import org.apache.directory.scim.test.helpers.builder.PatchOperationBuilder;
import org.apache.directory.scim.test.helpers.builder.ResourceReferenceBuilder;
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
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
  private static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE = new TypeReference<List<Map<String, Object>>>() {};

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

  @ParameterizedTest
  @CsvSource({"ADD", "REPLACE", "REMOVE"})
  void apply_invalidPath_exceptionThrow(final String operation) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    PatchOperation patchOperation = PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.valueOf(operation))
            .path("garbage")
            .build();

    Throwable t = catchThrowable(() -> this.patchOperations.apply(user,
            ImmutableList.of(patchOperation)));

    ScimTestHelper.assertScimException(t,
            Response.Status.BAD_REQUEST,
            ErrorMessageType.INVALID_PATH,
            ErrorMessageType.INVALID_PATH.getDetail());
  }

  @Test
  void apply_noFilterMatch_exceptionThrow() throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    List<PatchOperation> patchOperationList = new ArrayList<>();
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("addresses[type EQ \"work\"].locality")
            .value("Rochester")
            .build());

    Throwable t = catchThrowable(() -> this.patchOperations.apply(user,
            patchOperationList));

    ScimTestHelper.assertScimException(t,
            Response.Status.BAD_REQUEST,
            ErrorMessageType.NO_TARGET,
            ErrorMessageType.NO_TARGET.getDetail());
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
  void apply_multiplePatchOperationsReplaceAdd_successfullyPatched() throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();
    user.setTitle("");

    try {
      List<PatchOperation> patchOperationList = ImmutableList.of(
              PatchOperationBuilder.builder().operation(PatchOperation.Type.REPLACE)
                      .path("title")
                      .value("title -- 01")
                      .build(),
              // no address list entries are expected to exist
              PatchOperationBuilder.builder()
                      .operation(PatchOperation.Type.ADD)
                      .path("addresses[type EQ \"work\"].region")
                      .value("MN")
                      .build()
      );


      final ScimUser result = this.patchOperations.apply(user, patchOperationList);

      assertThat(result).isNotNull();
      assertThat(result.getTitle()).isEqualTo("title -- 01");
      assertThat(result.getAddresses()).hasSize(1);

      final Address address = result.getAddresses().get(0);
      assertThat(address).isNotNull();
      assertThat(address.getRegion()).isEqualTo("MN");
    } catch ( FilterParseException e ) {
      fail(e.getMessage());
    }
  }

  /*
   * {
   *   "schemas": [
   *     "urn:ietf:params:scim:api:messages:2.0:PatchOp"
   *   ],
   *   "Operations": [
   *     {
   *       "op": "Replace",
   *       "path": "active",
   *       "value": "False"
   *     },
   *     {
   *       "op": "Replace",
   *       "path": "name.familyName",
   *       "value": "User-001a"
   *     },
   *     {
   *       "op": "Add",
   *       "path": "addresses[type eq \"work\"].locality",
   *       "value": "Rochester"
   *     },
   *     {
   *       "op": "Add",
   *       "path": "addresses[type eq \"work\"].region",
   *       "value": "MN"
   *     }
   *   ]
   * }
   */
  @Test
  void apply_multiplePatchOperations_successfullyPatched() throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();
    user.setName(NameBuilder.builder()
            .givenName("Given Name")
            .familyName("Family Name")
            .build());

    List<PatchOperation> patchOperationList = new ArrayList<>();
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("active")
            .value(false)
            .build());
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("name.familyName")
            .value("User-001a")
            .build());
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("addresses[type eq \"work\"].region")
            .value("MN")
            .build());
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("addresses[type EQ \"work\"].locality")
            .value("Rochester")
            .build());
    patchOperationList.add(PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department")
            .value("** Department **")
            .build());

    final ScimUser result = this.patchOperations.apply(user, patchOperationList);

    assertThat(result).isNotNull();
    assertThat(result.getActive()).isFalse();
    assertThat(result.getName().getFamilyName()).isEqualTo("User-001a");

    Optional<Address> primaryAddress = result.getPrimaryAddress();
    assertThat(primaryAddress).isPresent();
    Address address = primaryAddress.get();
    assertThat(address.getRegion()).isEqualTo("MN");
    assertThat(address.getLocality()).isEqualTo("Rochester");
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

  @Test
  void apply_complexNameAttributeIsEmptyAdd_successfullyPatched()
    throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();
    user.setName(new Name());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path("name.familyName")
      .value("** Family Name **")
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();
    assertThat(result.getName().getFamilyName()).isEqualTo(patchOperation.getValue());
  }

  @Test
  void apply_complexNameAttributeIsNotEmptyReplace_successfullyPatched()
    throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();
    user.setName(NameBuilder.builder().familyName("## family name ##").build());

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path("name.familyName")
      .value("** Family Name **")
      .build();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotNull();
    assertThat(result.getName().getFamilyName()).isEqualTo(patchOperation.getValue());
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":department, Department",
    EnterpriseExtension.URN + ":division, Division",
    EnterpriseExtension.URN + ":costCenter, CostCenter",
    EnterpriseExtension.URN + ":employeeNumber, EmployeeNumber",
    EnterpriseExtension.URN + ":organization, Organization"})
  void apply_enterpriseExtensionAttributeAdd_successfullyPatched(final String path, final Object value) throws Exception {
    final ScimUser user = ScimTestHelper.generateMinimalScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(value)
      .build();

    assertThat(user.getExtension(EnterpriseExtension.URN)).isNull();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(1);

    validateExtensions(path, value, result.getExtension(EnterpriseExtension.class));
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

// TODO research is this is a validate path operation
//
//  @Test
//  void apply_complexExtensionAttributeAdd_successfullyPatched() throws Exception {
//    ScimUser user = ScimTestHelper.generateMinimalScimUser();
//
//    final PatchOperation patchOperation = PatchOperationBuilder.builder()
//            .operation(PatchOperation.Type.ADD)
//            .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager")
//            .value(UUID.randomUUID().toString())
//            .build();
//
//    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));
//
//    assertThat(result).isNotNull();
//    assertThat(result.getExtensions()).isNotNull();
//    assertThat(result.getExtensions()).hasSize(1);
//    assertThat(result.getExtension(EnterpriseExtension.class)).isNotNull();
//
//    validateExtensions("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.value",
//            patchOperation.getValue(), result.getExtension(EnterpriseExtension.class));
//  }

  // SCIM GROUP PATCH ADD -----------------------------------------------------------------------------

  @ParameterizedTest
  @CsvSource({"displayName, ** Display Name **"})
  void apply_simpleScimGroupAttributeAdd_successfullyPatched(final String path, final Object value) throws Exception {
    ScimGroup group = ScimTestHelper.generateMinimalScimGroup();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path(path)
      .value(value)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getDisplayName()).isEqualTo(value);
  }

  @Test
  void apply_scimGroupAddGroupWithExisting_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();
    ResourceReference resourceReference = new ResourceReference();
    resourceReference.setDisplay( "Second Group Membership" );
    resourceReference.setType( ResourceReference.ReferenceType.DIRECT );
    resourceReference.setValue( UUID.randomUUID().toString() );

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
            .operation( PatchOperation.Type.ADD )
            .path( "members" )
            .value( objectMapper.convertValue( ImmutableList.of(resourceReference), LIST_MAP_TYPE ) )
            .build();

    final ScimGroup result = this.patchOperations.apply( group, ImmutableList.of( patchOperation ) );

    assertThat( result ).isNotNull();
    assertThat( result.getMembers() ).hasSize( 2 );
  }

  @Test
  void apply_scimGroupRemoveGroupWithExisting_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();
    ResourceReference expected = group.getMembers().get(0);

    ResourceReference resourceReference = new ResourceReference();
    resourceReference.setDisplay("Second Group Membership");
    resourceReference.setType(ResourceReference.ReferenceType.DIRECT );
    resourceReference.setValue(UUID.randomUUID().toString());

    group.getMembers().add(resourceReference);

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
            .operation( PatchOperation.Type.REMOVE )
            .path( "members[value EQ \"" + resourceReference.getValue() + "\"]" )
            .build();

    final ScimGroup result = this.patchOperations.apply( group, ImmutableList.of( patchOperation ) );

    assertThat(result ).isNotNull();
    assertThat(result.getMembers()).hasSize(1);
    assertThat(result.getMembers().get(0)).isEqualTo(expected);
  }

  @Test
  void apply_complexScimGroupAttributeAdd_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateMinimalScimGroup();

    List<ResourceReference> referenceList = new ArrayList<>();
    referenceList.add(ResourceReferenceBuilder.builder()
      .type(ResourceReference.ReferenceType.DIRECT)
      .display("Group Membership Direct")
      .value(UUID.randomUUID().toString())
      .build());

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.ADD)
      .path("members")
      .value(referenceList)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getMembers()).hasSize(1);
    validateResourceReferences(result.getMembers(), referenceList);
  }

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
  @CsvSource({EnterpriseExtension.URN + ":department, ** DEPARTMENT **",
    EnterpriseExtension.URN + ":division, ** division **",
    EnterpriseExtension.URN + ":costCenter, ** costCenter **",
    EnterpriseExtension.URN + ":employeeNumber, ** organization **",
    EnterpriseExtension.URN + ":organization, ** DEPARTMENT **"})
  void apply_enterpriseExtensionAttributeReplace_successfullyPatched(final String path, final String value) throws Exception {
    final ScimUser user = ScimTestHelper.generateScimUser();
    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .value(value)
      .build();

    assertThat(user.getExtension(EnterpriseExtension.URN)).isNotNull();

    final ScimUser result = this.patchOperations.apply(user, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getExtensions()).isNotNull();
    assertThat(result.getExtensions()).hasSize(2);

    validateExtensions(path, value, result.getExtension(EnterpriseExtension.class));
  }

  @ParameterizedTest
  @CsvSource({EnterpriseExtension.URN + ":manager.displayName, ** DISPLAY NAME **",
          EnterpriseExtension.URN + ":manager.value, ** VALUE **"})
  void apply_complexExtensionAttributeReplace_successfullyPatched(final String path, final String value) throws Exception {
    ScimUser user = ScimTestHelper.generateMinimalScimUser();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
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

  // SCIM GROUP PATCH REPLACE -----------------------------------------------------------------------------

  /*
   * [method=PATCH, uri=http://console.sso.test.core.cloud.code42.com/scim/v2/Groups/933868861859790973, hasEntity=true,
   * contentType=application/scim+json;charset=utf-8, contentLanguage=null, acceptableTypes=[application/scim+json],
   * acceptableLanguages=[*], requestBody={"schemas":["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
   * "Operations":[{"op":"replace","value":{"id":"933868861859790973","displayName":"TestGroup1 -- Update12345"}}]}]
   */
  @Test
  void apply_groupReplaceForIdAndDisplayName_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    Map<String,Object> values = new HashMap<>();
    values.put("id", group.getId());
    final String expectedDisplayName = group.getDisplayName() + " -- Update12345";
    values.put("displayName", expectedDisplayName);

    PatchOperation patchOperation = PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .value(values)
            .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getId()).isEqualTo(group.getId());
    assertThat(result.getDisplayName()).isEqualTo(expectedDisplayName);
  }

  @ParameterizedTest
  @CsvSource({"displayName, ** Display Name **"})
  void apply_simpleScimGroupAttributeReplace_successfullyPatched(final String path, final Object value) throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path(path)
      .value(value)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getDisplayName()).isEqualTo(value);
  }

  @Test
  void apply_complexScimGroupMembersAttributeReplace_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    List<ResourceReference> referenceList = new ArrayList<>();
    referenceList.add(ResourceReferenceBuilder.builder()
      .type(ResourceReference.ReferenceType.INDIRECT)
      .display("** Display **")
      .value(UUID.randomUUID().toString())
      .build());

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path("members")
      .value(referenceList)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getMembers()).hasSize(2);
    validateResourceReferences(result.getMembers(), referenceList);
  }

  @ParameterizedTest
  @CsvSource({"value, ** VALUE **", "display, ** DISPLAY **"})
  void apply_complexScimGroupMembersAttributeReplace_successfullyPatched(final String path, final Object value) throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .path("members[value EQ \"" + group.getMembers().get(0).getValue() + "\"]." + path)
      .value(value)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getMembers()).hasSize(1);
    assertThat(getValueByAttributeName(result.getMembers().get(0), path)).isEqualTo(value);
  }

  @Test
  void apply_complexScimGroupMembersTypeReplace_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
            .operation(PatchOperation.Type.REPLACE)
            .path("members[value EQ \"" + group.getMembers().get(0).getValue() + "\"].type")
            .value(ResourceReference.ReferenceType.INDIRECT)
            .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getMembers()).hasSize(1);
    assertThat(getValueByAttributeName(result.getMembers().get(0), "type"))
            .isEqualTo(ResourceReference.ReferenceType.INDIRECT.name().toLowerCase(Locale.ROOT));
  }

  @Test
  void apply_complexScimGroupAttributeReplace_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    final String id = UUID.randomUUID().toString();
    final Map<String,Object> values = new HashMap<>();
    values.put("id", id);
    values.put("displayName", "** Display Name **");

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REPLACE)
      .value(values)
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result).isNotEqualTo(group);
    assertThat(result.getMembers()).hasSize(1);

    assertThat(result.getId()).isEqualTo(values.get("id"));
    assertThat(result.getDisplayName()).isEqualTo(values.get("displayName"));
  }

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

  @Test
  void apply_complexScimGroupAttributeRemove_successfullyPatched() throws Exception {
    ScimGroup group = ScimTestHelper.generateScimGroup();

    final PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(PatchOperation.Type.REMOVE)
      .path("members")
      .build();

    final ScimGroup result = this.patchOperations.apply(group, ImmutableList.of(patchOperation));

    assertThat(result).isNotNull();
    assertThat(result.getMembers()).isNull();
  }

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
