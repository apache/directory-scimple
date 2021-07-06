package org.apache.directory.scim.test.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.common.ScimUtils;
import org.apache.directory.scim.server.exception.InvalidProviderException;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.EtagGenerator;
import org.apache.directory.scim.server.utility.ExampleObjectExtension;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.test.helpers.builder.AddressBuilder;
import org.apache.directory.scim.test.helpers.builder.EmailBuilder;
import org.apache.directory.scim.test.helpers.builder.EnterpriseExtensionBuilder;
import org.apache.directory.scim.test.helpers.builder.EnterpriseExtensionManagerBuilder;
import org.apache.directory.scim.test.helpers.builder.MetaBuilder;
import org.apache.directory.scim.test.helpers.builder.NameBuilder;
import org.apache.directory.scim.test.helpers.builder.ResourceReferenceBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class ScimTestHelper {

  private static final TypeReference<List<Map<String,Object>>> LIST_MAP_TYPE = new TypeReference<List<Map<String,Object>>>(){};

  private static final EtagGenerator etagGenerator = createEtagGenerator();

  public static ScimGroup generateMinimalScimGroup() throws Exception {
    final String id = UUID.randomUUID().toString();

    ScimGroup group = new ScimGroup();
    group.setId(id);

    group.setMeta(MetaBuilder.builder()
      .created(LocalDateTime.now())
      .lastModified(LocalDateTime.now())
      .resourceType("User")
      .location("http://example.com/Groups/" + group.getId())
      .version(etagGenerator.generateEtag(group).toString())
      .build());

    return group;
  }

  public static ScimGroup generateScimGroup() throws Exception {
    return generateScimGroup(UUID.randomUUID().toString());
  }

  public static ScimGroup generateScimGroup(final String initialGroupMemberId) throws Exception {

    ScimGroup group = generateMinimalScimGroup();

    group.setDisplayName("Group " + group.getId());
    group.setExternalId("e-" +  group.getId());

    group.setMembers(new ArrayList<>());
    group.getMembers().add(ResourceReferenceBuilder.builder()
      .type(ResourceReference.ReferenceType.DIRECT)
      .display("First Group Membership")
      .value(initialGroupMemberId)
      .build());

    return group;
  }

  public static ScimUser generateMinimalScimUser() throws Exception {
    ScimUser user = new ScimUser();

    final String id = UUID.randomUUID().toString();

    user.setId(id);
    user.setUserName("jed-" + id + user.getUserName() + "@example.com");

    user.setMeta(MetaBuilder.builder()
      .created(LocalDateTime.now())
      .lastModified(LocalDateTime.now())
      .resourceType("User")
      .location("http://example.com/Users/" + user.getId())
      .version(etagGenerator.generateEtag(user).toString())
      .build());

    return user;
  }

  public static ScimUser generateScimUser() throws PhoneNumberParseException, NoSuchAlgorithmException, UnsupportedEncodingException, JsonProcessingException {
    ScimUser user = new ScimUser();

    final String id = UUID.randomUUID().toString();

    user.setActive(true);
    user.setDisplayName("John Doe");
    user.setExternalId("e-" + id);
    user.setId(id);
    user.setLocale("US");
    user.setNickName("Johnny");
    user.setPassword("@#$%^73840302-4=");
    user.setPreferredLanguage("English");
    user.setProfileUrl("http://example.com/Users/" + user.getId() + "/Profile");
    user.setTimezone(TimeZone.getDefault().getDisplayName());
    user.setTitle("The doe of Johns");
    user.setUserName("john.doe@example.com");
    user.setUserType(ScimUser.RESOURCE_NAME);

    user.setName(NameBuilder.builder()
      .honorificPrefix("Mr.")
      .givenName("John")
      .middleName("E")
      .familyName("Doe")
      .honorificSuffix("Jr.")
      .formatted("Mr. John E. Doe Jr.")
      .build());

    user.setEmails(new ArrayList<>());
    user.getEmails().add(EmailBuilder.builder()
      .display("User " + user.getId() + " email address")
      .primary(true)
      .type("home")
      .value(user.getUserName())
      .build());
    user.getEmails().add(EmailBuilder.builder()
      .display("User " + user.getId() + " email address")
      .primary(false)
      .type("work")
      .value(user.getUserName().substring(0, user.getUserName().lastIndexOf(".")) + "edu")
      .build());

    user.setAddresses(new ArrayList<>());
    user.getAddresses().add(AddressBuilder.builder()
      .streetAddress("123 Main St.")
      .locality("State College")
      .region("PA")
      .postalCode("16801")
      .country("USA")
      .type("home")
      .primary(true)
      .display("123 Main St. State College, PA 16801")
      .formatted("123 Main St. State College, PA 16801")
      .build());
    user.getAddresses().add(AddressBuilder.builder()
      .streetAddress("456 Main St.")
      .locality("State College")
      .region("PA")
      .postalCode("16801")
      .country("USA")
      .type("work")
      .primary(false)
      .display("456 Main St. State College, PA 16801")
      .formatted("456 Main St. State College, PA 16801")
      .build());

    user.setPhoneNumbers(new ArrayList<>());

    PhoneNumber.LocalPhoneNumberBuilder lpnb = new PhoneNumber.LocalPhoneNumberBuilder();

    PhoneNumber phoneNumber = lpnb.areaCode("123")
      .countryCode("1")
      .subscriberNumber("456-7890")
      .build();
    phoneNumber.setDisplay("123-456-7890");
    phoneNumber.setPrimary(true);
    phoneNumber.setType("home");

    user.getPhoneNumbers().add(phoneNumber);

    phoneNumber = new PhoneNumber();
    phoneNumber.setValue("tel:+1-800-555-1234");
    phoneNumber.setDisplay("1-800-555-1234");
    phoneNumber.setPrimary(false);
    phoneNumber.setType("work");

    user.getPhoneNumbers().add(phoneNumber);

    user.addExtension(EnterpriseExtensionBuilder.builder()
      .costCenter("CC-123")
      .department("DEPT-xyz")
      .division("DIV-1")
      .employeeNumber("1234567890")
      .manager(EnterpriseExtensionManagerBuilder.builder()
        .displayName("Bob Smith")
        .value("0987654321")
        .build())
      .organization("ORG-X")
      .build());

    ExampleObjectExtension exampleObjectExtension = new ExampleObjectExtension();
    exampleObjectExtension.setValueAlways("always");
    exampleObjectExtension.setValueDefault("default");
    exampleObjectExtension.setValueNever("never");
    exampleObjectExtension.setValueRequest("request");
    ExampleObjectExtension.ComplexObject valueComplex = new ExampleObjectExtension.ComplexObject();
    valueComplex.setDisplayName("ValueComplex");
    valueComplex.setValue("Value");
    exampleObjectExtension.setValueComplex(valueComplex);

    user.addExtension(exampleObjectExtension);

    user.setMeta(MetaBuilder.builder()
      .created(LocalDateTime.now())
      .lastModified(LocalDateTime.now())
      .resourceType("User")
      .location("http://example.com/Users/" + user.getId())
      .version(etagGenerator.generateEtag(user).toString())
      .build());

    return user;
  }

  public static void validateResourceReferences(final List<ResourceReference> actual, final List<ResourceReference> expected) throws Exception {
    assertThat(getObjectMapper().convertValue(actual, LIST_MAP_TYPE)).containsAll(getObjectMapper().convertValue(expected, LIST_MAP_TYPE));
  }

  public static Object getValueByAttributeName(final ScimUser user, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "displayName":
        return user.getDisplayName();
      case "locale":
        return user.getLocale();
      case "nickName":
        return user.getNickName();
      case "profileUrl":
        return user.getProfileUrl();
      case "preferredLanguage":
        return user.getPreferredLanguage();
      case "timezone":
        return user.getTimezone();
      case "title":
        return user.getTitle();
      case "userType":
        return user.getUserType();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final Address address, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "primary":
        return address.getPrimary();
      case "type":
        return address.getType();
      case "country":
        return address.getCountry();
      case "region":
        return address.getRegion();
      case "locality":
        return address.getLocality();
      case "streetAddress":
        return address.getStreetAddress();
      case "postalCode":
        return address.getPostalCode();
      case "display":
        return address.getDisplay();
      case "formatted":
        return address.getFormatted();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final Name name, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "familyName":
        return name.getFamilyName();
      case "givenName":
        return name.getGivenName();
      case "middleName":
        return name.getMiddleName();
      case "formatted":
        return name.getFormatted();
      case "honorificPrefix":
        return name.getHonorificPrefix();
      case "honorificSuffix":
        return name.getHonorificSuffix();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final Email email, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "primary":
        return email.getPrimary();
      case "type":
        return email.getType();
      case "value":
        return email.getValue();
      case "display":
        return email.getDisplay();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final EnterpriseExtension extension, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department":
      case "department":
        return extension.getDepartment();
      case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:division":
      case "division":
        return extension.getDivision();
      case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:costCenter":
      case "costCenter":
        return extension.getCostCenter();
      case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber":
      case "employeeNumber":
        return extension.getEmployeeNumber();
      case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:organization":
      case "organization":
        return extension.getOrganization();
    }

    if (attributeName.startsWith("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager")) {
      return getValueByAttributeName(extension.getManager(), attributeName);
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final ExampleObjectExtension extension, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "subobject":
        return extension.getEnumList();
      case "list":
        return extension.getList();
      case "division":
        return extension.getSubobject();
      case "valueAlways":
        return extension.getValueAlways();
      case "valueComplex":
        return extension.getValueComplex();
      case "valueDefault":
        return extension.getValueDefault();
      case "valueNever":
        return extension.getValueNever();
      case "valueRequest":
        return extension.getValueRequest();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static Object getValueByAttributeName(final EnterpriseExtension.Manager manager, final String attributeName)
    throws Exception {
    if (Objects.nonNull(manager)) {
      switch (attributeName) {
        case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.displayName":
        case "displayName":
          return manager.getDisplayName();
        case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.ref":
        case "ref":
          return manager.getRef();
        case "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.value":
        case "value":
          return manager.getValue();
      }

      throw new Exception("Unknown attribute name '" + attributeName + "'");
    } else {
      throw new Exception("Enterprise Extension Manager was not expected to be null.");
    }
  }

  public static Object getValueByAttributeName(final ResourceReference resourceReference, final String attributeName)
    throws Exception {
    switch (attributeName) {
      case "type":
        return resourceReference.getType() != null ? resourceReference.getType().name().toLowerCase(Locale.ROOT) : null;
      case "ref":
        return resourceReference.getRef();
      case "value":
        return resourceReference.getValue();
      case "display":
        return resourceReference.getDisplay();
    }

    throw new Exception("Unknown attribute name '" + attributeName + "'");
  }

  public static void validateMembers(final List<ResourceReference> actual, final List<ResourceReference> expected) {
    assertThat(actual).containsExactlyElementsOf(expected);
  }

  public static void validateExtensions(final String attribute, final Object expectedValue,
                                        final ScimExtension extension) throws Exception {

    if (extension.getUrn().equals(EnterpriseExtension.URN)) {
      validateEnterpriseExtension(attribute, expectedValue,
        (EnterpriseExtension) extension);
    } else if (extension.getUrn().equals(ExampleObjectExtension.URN)) {
      validateExampleObjectExtension(attribute, expectedValue,
        (ExampleObjectExtension) extension);
    } else {
      throw new UnsupportedOperationException("Extension " + extension.getUrn() + " isn't supported.");
    }
  }

  private static void validateEnterpriseExtension(final String attribute, final Object expectedValue,
                                                  final EnterpriseExtension extension) throws Exception {
    assertThat(getValueByAttributeName(extension, attribute)).isEqualTo(expectedValue);
  }

  private static void validateExampleObjectExtension(final String attribute, final Object expectedValue,
                                                     final ExampleObjectExtension extension) throws Exception {
    assertThat(getValueByAttributeName(extension, attribute)).isEqualTo(expectedValue);
  }

  public static void assertScimException(Throwable t,
                                         Response.Status expectedStatus,
                                         ErrorMessageType expectedErrorMessageType,
                                         String expectedMessageFragment) {

    assertThat(t).isInstanceOf(ScimException.class);

    ScimException sre = (ScimException) t;
    assertThat(sre.getStatus()).isEqualTo(expectedStatus);
    assertThat(sre.getError()).isNotNull();
    ErrorResponse errorResponse = sre.getError();

    assertThat(errorResponse.getScimType()).isEqualTo(expectedErrorMessageType);
    assertThat(errorResponse.getDetail()).isEqualTo(expectedMessageFragment);
  }

  public static EtagGenerator createEtagGenerator() {
    return new EtagGenerator();
  }

  public static Registry createRegistry()
    throws Exception {
    // Scim resources
    Schema scimUserSchema = ProviderRegistry.generateSchema(ScimUser.class,
      ScimUtils.getFieldsUpTo(ScimUser.class, BaseResource.class));
    Schema scimGroupSchema = ProviderRegistry.generateSchema(ScimGroup.class,
      ScimUtils.getFieldsUpTo(ScimGroup.class, BaseResource.class));

    // Scim extensions
    Schema scimEnterpriseUserSchema = ProviderRegistry.generateSchema(EnterpriseExtension.class,
      ScimUtils.getFieldsUpTo(EnterpriseExtension.class, Object.class));
    Schema scimExampleSchema = ProviderRegistry.generateSchema(ExampleObjectExtension.class,
      ScimUtils.getFieldsUpTo(ExampleObjectExtension.class, BaseResource.class));

    Registry registry = mock(Registry.class);

    when(registry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).thenReturn(scimUserSchema);
    when(registry.getBaseSchemaOfResourceType(ScimGroup.RESOURCE_NAME)).thenReturn(scimGroupSchema);
    when(registry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(scimUserSchema);
    when(registry.getSchema(ScimGroup.SCHEMA_URI)).thenReturn(scimGroupSchema);
    when(registry.getSchema(EnterpriseExtension.URN)).thenReturn(scimEnterpriseUserSchema);
    when(registry.getSchema(ExampleObjectExtension.URN)).thenReturn(scimExampleSchema);
    when(registry.getAllSchemas()).thenReturn(Arrays.asList(scimUserSchema, scimGroupSchema, scimEnterpriseUserSchema, scimExampleSchema));
    when(registry.getAllSchemaUrns()).thenReturn(new HashSet<>(Arrays.asList(ScimUser.SCHEMA_URI, ScimGroup.SCHEMA_URI, EnterpriseExtension.URN, ExampleObjectExtension.URN)));
    when(registry.getAllResourceTypes()).thenReturn(new HashSet<>(Arrays.asList(scimUserResourceType(), scimGroupResourceType())));

    return registry;
  }

  public static ObjectMapper getObjectMapper() throws Exception {
    return new ObjectMapperFactory(createRegistry()).createObjectMapper();
  }

  private static ResourceType scimUserResourceType() throws InvalidProviderException {
    /*
     * the following section is required to get the Scim User Extensions to serialize/deserialize to/from
     * Map<String,Object> representations.
     *
     * This code was copied here so that the mock is usable without instantiating real objects when testing.
     */
    ScimExtensionRegistry extensionRegistry = ScimExtensionRegistry.getInstance();
    final List<Class<? extends ScimExtension>> extensions = new ArrayList<>();
    extensions.add(EnterpriseExtension.class);
    extensions.add(ExampleObjectExtension.class);

    List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();

    ScimResourceType scimResourceType = ScimUser.class.getAnnotation(ScimResourceType.class);
    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    for (Class<? extends ScimExtension> se : extensions) {

      ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

      if (extensionType==null) {
        throw new InvalidProviderException(
          "Missing annotation: ScimExtensionType must be at the top of scim extension classes");
      }

      extensionRegistry.registerExtension(ScimUser.class, se);

      ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
      ext.setRequired(extensionType.required());
      ext.setSchemaUrn(extensionType.id());
      extensionSchemaList.add(ext);
    }

    resourceType.setSchemaExtensions(extensionSchemaList);

    return resourceType;
  }

  private static ResourceType scimGroupResourceType() throws InvalidProviderException {
    /*
     * the following section is required to get the Scim User Extensions to serialize/deserialize to/from
     * Map<String,Object> representations.
     *
     * This code was copied here so that the mock is usable without instantiating real objects when testing.
     */
    ScimExtensionRegistry extensionRegistry = ScimExtensionRegistry.getInstance();
    final List<Class<? extends ScimExtension>> extensions = new ArrayList<>();
    extensions.add(EnterpriseExtension.class);
    extensions.add(ExampleObjectExtension.class);

    List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();

    ScimResourceType scimResourceType = ScimGroup.class.getAnnotation(ScimResourceType.class);
    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    for (Class<? extends ScimExtension> se : extensions) {

      ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

      if (extensionType==null) {
        throw new InvalidProviderException(
          "Missing annotation: ScimExtensionType must be at the top of scim extension classes");
      }

      extensionRegistry.registerExtension(ScimUser.class, se);

      ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
      ext.setRequired(extensionType.required());
      ext.setSchemaUrn(extensionType.id());
      extensionSchemaList.add(ext);
    }

    resourceType.setSchemaExtensions(extensionSchemaList);

    return resourceType;
  }

  public static void logPatchOperationBuilder(final List<PatchOperation> operations) {
    operations.forEach(ScimTestHelper::logPatchOperationBuilder);
  }

  public static void logPatchOperationBuilder(final PatchOperation operation) {
    final StringBuilder sb = new StringBuilder();

    sb.append(System.getProperty("line.separator"));
    sb.append(System.getProperty("line.separator"));
    sb.append("Patch Operation Builder:");
    sb.append(System.getProperty("line.separator"));
    sb.append(System.getProperty("line.separator"));
    sb.append("PatchOperation patchOperation = PatchOperationBuilder.builder()");
    sb.append(System.getProperty("line.separator"));
    sb.append("\t\t\t.operation(").append(operation.getOperation()).append(")");
    sb.append(System.getProperty("line.separator"));
    sb.append("\t\t\t.path(\"").append(operation.getPath().toString()).append("\")");
    sb.append(System.getProperty("line.separator"));

    Object value = operation.getValue();
    if (Objects.nonNull(value)) {
      if (value instanceof String) {
        sb.append("\t\t\t.value(\"").append(operation.getValue()).append("\")");
      } else {
        sb.append("\t\t\t.value(").append(value).append(")");
      }

      sb.append(System.getProperty("line.separator"));
    }

    sb.append("\t\t\t.build();");
    sb.append(System.getProperty("line.separator"));
    log.info(sb.toString());
  }

  static void logAttributeReference(final PatchOperation operation) {
    final AttributeReference reference = operation.getPath().getValuePathExpression().getAttributePath();
    logAttributeReference(reference);
  }

  static void logAttributeReference(final AttributeReference reference) {
    log.info("ATTRIBUTE REFERENCE URN             : {}", reference.getUrn());
    log.info("ATTRIBUTE REFERENCE NAME            : {}", reference.getAttributeName());
    log.info("ATTRIBUTE REFERENCE SUB-NAME        : {}", reference.getSubAttributeName());
    log.info("ATTRIBUTE REFERENCE FULL NAME       : {}", reference.getFullAttributeName());
    log.info("ATTRIBUTE REFERENCE FULLY QUALIFIED : {}", reference.getFullyQualifiedAttributeName());
  }
}
