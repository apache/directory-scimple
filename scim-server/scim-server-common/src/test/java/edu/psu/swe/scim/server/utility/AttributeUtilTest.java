package edu.psu.swe.scim.server.utility;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParseException;

import edu.psu.swe.scim.common.ScimUtils;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.server.utility.ExampleObjectExtension.ComplexObject;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension.Manager;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.resources.Address;
import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.PhoneNumber;
import edu.psu.swe.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import edu.psu.swe.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.Schema;

@Ignore
public class AttributeUtilTest {

  private static final Logger LOG = LoggerFactory.getLogger(AttributeUtilTest.class);

  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();

  @Mock
  Registry registry;

  AttributeUtil attributeUtil;

  private ObjectMapper objectMapper;

  @Before
  public void setup() throws Exception {
    attributeUtil = new AttributeUtil();
    attributeUtil.registry = registry;
    Schema scimUserSchema = ProviderRegistry.generateSchema(ScimUser.class, ScimUtils.getFieldsUpTo(ScimUser.class, BaseResource.class));
    Schema scimEnterpriseUserSchema = ProviderRegistry.generateSchema(EnterpriseExtension.class, ScimUtils.getFieldsUpTo(EnterpriseExtension.class, Object.class));
    Schema scimExampleSchema = ProviderRegistry.generateSchema(ExampleObjectExtension.class, ScimUtils.getFieldsUpTo(ExampleObjectExtension.class, Object.class));


    Mockito.when(registry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).thenReturn(scimUserSchema);
    Mockito.when(registry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(scimUserSchema);
    Mockito.when(registry.getSchema(EnterpriseExtension.URN)).thenReturn(scimEnterpriseUserSchema);
    Mockito.when(registry.getSchema(ExampleObjectExtension.URN)).thenReturn(scimExampleSchema);
    Mockito.when(registry.getAllSchemas()).thenReturn(Arrays.asList(scimUserSchema, scimEnterpriseUserSchema, scimExampleSchema));
    Mockito.when(registry.getAllSchemaUrns()).thenReturn(new HashSet<String>(Arrays.asList(ScimUser.SCHEMA_URI, EnterpriseExtension.URN, ExampleObjectExtension.URN)));

    attributeUtil.init();
    
    objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
    AnnotationIntrospector pair = new AnnotationIntrospectorPair(jacksonIntrospector, jaxbIntrospector);
    objectMapper.setAnnotationIntrospector(pair);

    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

  }

  @Test
  public void testBaseResource() throws Exception {
    ScimUser resource = getScimUser();
    
    debugJson(resource);

    resource = attributeUtil.setAttributesForDisplay(resource);

    debugJson(resource);

    Assertions.assertThat(resource.getId()).isNotNull();
    Assertions.assertThat(resource.getPassword()).isNull();
    
    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
    
    Assertions.assertThat(extension.getCostCenter()).isNotNull();
    
    ExampleObjectExtension exampleObjectExtension = resource.getExtension(ExampleObjectExtension.class);
    
    Assertions.assertThat(exampleObjectExtension.getValueAlways()).isNotNull();
    Assertions.assertThat(exampleObjectExtension.getValueDefault()).isNotNull();
    Assertions.assertThat(exampleObjectExtension.getValueRequest()).isNull();
    Assertions.assertThat(exampleObjectExtension.getValueNever()).isNull();
  }
  
//  @Test
//  @Ignore
//  public void testIncludeAttributes() throws Exception {
//    ScimUser resource = getScimUser();
//    
//    debugJson(resource);
//    
//    Set<AttributeReference> attributes = new HashSet<>();
//    attributes.add(new AttributeReference("userName"));
//    attributes.add(new AttributeReference("addresses.streetAddress"));
//    resource = attributeUtil.setAttributesForDisplay(resource, attributes);
//    
//    debugJson(resource);
//
//    Assertions.assertThat(resource.getUserName()).isNotNull();
//    Assertions.assertThat(resource.getId()).isNotNull();
//
//    Assertions.assertThat(resource.getPassword()).isNull();
//    Assertions.assertThat(resource.getActive()).isNull();
//    
//    Assertions.assertThat(resource.getAddresses().get(0).getCountry()).isNull();
//    Assertions.assertThat(resource.getAddresses().get(0).getStreetAddress()).isNotNull();
//
//    
//    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
//    
//    // TODO Assertions.assertThat(extension).isNull();
//  }
  
  @Test
  public void testIncludeAttributesWithExtension() throws Exception {
    ScimUser resource = getScimUser();
    
    debugJson(resource);
    
    Set<AttributeReference> attributeSet = new HashSet<>();
    attributeSet.add(new AttributeReference("userName"));
    attributeSet.add(new AttributeReference(EnterpriseExtension.URN + ":costCenter"));
    
    resource = attributeUtil.setAttributesForDisplay(resource, attributeSet);
    
    debugJson(resource);

    Assertions.assertThat(resource.getUserName()).isNotNull();
    Assertions.assertThat(resource.getId()).isNotNull();

    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getActive()).isNull();
    
    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
    
    Assertions.assertThat(extension.getCostCenter()).isNotNull();
    Assertions.assertThat(extension.getDepartment()).isNull();

  }
  
  @Test
  public void testExcludeAttributes() throws Exception {
    ScimUser resource = getScimUser();
    
    resource = attributeUtil.setExcludedAttributesForDisplay(resource, Collections.singleton(new AttributeReference("userName")));
    

    Assertions.assertThat(resource.getId()).isNotNull();
    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getUserName()).isNull();
    Assertions.assertThat(resource.getActive()).isNotNull();

    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
    
    Assertions.assertThat(extension.getCostCenter()).isNotNull();
  }
  
  @Test
  public void testExcludeAttributesWithExtensions() throws Exception {
    ScimUser resource = getScimUser();
    
    Set<AttributeReference> attributeSet = new HashSet<>();
    attributeSet.add(new AttributeReference("userName"));
    attributeSet.add(new AttributeReference(EnterpriseExtension.URN + ":costCenter"));
    
    resource = attributeUtil.setExcludedAttributesForDisplay(resource, attributeSet);
    
    Assertions.assertThat(resource.getId()).isNotNull();
    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getUserName()).isNull();
    Assertions.assertThat(resource.getActive()).isNotNull();

    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
    
    Assertions.assertThat(extension.getCostCenter()).isNull();
    Assertions.assertThat(extension.getDepartment()).isNotNull();
  }

  private void debugJson(Object resource) throws JsonGenerationException, JsonMappingException, IOException {
    StringWriter sw = new StringWriter();
    objectMapper.writeValue(sw, resource);
    LOG.info(sw.toString());
  }
  
  private ScimUser getScimUser() throws PhoneNumberParseException {
    ScimUser user = new ScimUser();

    user.setActive(true);
    user.setId("1");
    user.setExternalId("e1");
    user.setUserName("jed1");
    user.setActive(true);
    user.setNickName("Jonny");
    user.setPassword("secret");
    user.setPreferredLanguage("English");
    user.setProfileUrl("http://example.com/Users/JohnDoe");
    user.setUserName("jed1");
    user.setDisplayName("John Doe");

    Name name = new Name();
    name.setHonorificPrefix("Mr.");
    name.setGivenName("John");
    name.setMiddleName("E");
    name.setFamilyName("Doe");
    name.setHonorificSuffix("Jr.");
    name.setFormatted("Mr. John E. Doe Jr.");
    user.setName(name);

    List<Address> addresses = new ArrayList<>();
    Address address = new Address();
    address.setStreetAddress("123 Main St.");
    address.setLocality("State College");
    address.setRegion("PA");
    address.setPostalCode("16801");
    address.setCountry("USA");
    address.setType("home");
    address.setPrimary(true);
    address.setDisplay("123 Main St. State College, PA 16801");
    address.setFormatted("123 Main St. State College, PA 16801");

    addresses.add(address);

    address = new Address();
    address.setStreetAddress("456 Main St.");
    address.setLocality("State College");
    address.setRegion("PA");
    address.setPostalCode("16801");
    address.setCountry("USA");
    address.setType("work");
    address.setPrimary(false);
    address.setDisplay("456 Main St. State College, PA 16801");
    address.setFormatted("456 Main St. State College, PA 16801");

    addresses.add(address);
    user.setAddresses(addresses);

    List<PhoneNumber> phoneNumbers = new ArrayList<>();
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder("123-456-7890", "+1", null).build();
    phoneNumber.setDisplay("123-456-7890");
    phoneNumber.setPrimary(true);
    phoneNumber.setType("home");

    phoneNumbers.add(phoneNumber);

    phoneNumber = new GlobalPhoneNumberBuilder("1-800-555-1234").build();
    phoneNumber.setDisplay("1-800-555-1234");
    phoneNumber.setPrimary(false);
    phoneNumber.setType("work");

    phoneNumbers.add(phoneNumber);
    user.setPhoneNumbers(phoneNumbers);

    EnterpriseExtension enterpriseEntension = new EnterpriseExtension();
    enterpriseEntension.setCostCenter("CC-123");
    enterpriseEntension.setDepartment("DEPT-xyz");
    enterpriseEntension.setDivision("DIV-1");
    enterpriseEntension.setEmployeeNumber("1234567890");
    Manager manager = new Manager();
    manager.setDisplayName("Bob Smith");
    manager.setValue("0987654321");
    enterpriseEntension.setManager(manager);
    enterpriseEntension.setOrganization("ORG-X");
    
    user.addExtension(enterpriseEntension);
    
    ExampleObjectExtension exampleObjectExtension = new ExampleObjectExtension();
    exampleObjectExtension.setValueAlways("always");
    exampleObjectExtension.setValueDefault("default");
    exampleObjectExtension.setValueNever("never");
    exampleObjectExtension.setValueRequest("request");
    ComplexObject valueComplex = new ComplexObject();
    valueComplex.setDisplayName("ValueComplex");
    valueComplex.setValue("Value");
    exampleObjectExtension.setValueComplex(valueComplex);
    
    user.addExtension(exampleObjectExtension);
    
    return user;
  }

}
