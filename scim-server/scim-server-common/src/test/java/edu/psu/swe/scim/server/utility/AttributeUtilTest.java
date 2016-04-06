package edu.psu.swe.scim.server.utility;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
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
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.resources.Address;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.PhoneNumber;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.Schema;

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
    Schema scimUserSchema = ProviderRegistry.generateSchema(ScimUser.class);

    Mockito.when(registry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).thenReturn(scimUserSchema);
    Mockito.when(registry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(scimUserSchema);
    Mockito.when(registry.getAllSchemas()).thenReturn(Collections.singleton(scimUserSchema));
    Mockito.when(registry.getAllSchemaUrns()).thenReturn(Collections.singleton(ScimUser.SCHEMA_URI));

    
    objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

  }

  @Test
  public void testBaseResource() throws Exception {
    ScimUser resource = getScimUser();
    
    resource = attributeUtil.setAttributesForDisplay(resource);
    
    Assertions.assertThat(resource.getId()).isNotNull();
    Assertions.assertThat(resource.getPassword()).isNull();
  }
  
  @Test
  public void testIncludeAttributes() throws Exception {
    ScimUser resource = getScimUser();
    
    debugJson(resource);
    
    resource = attributeUtil.setAttributesForDisplay(resource, "userName");
    
    debugJson(resource);

    Assertions.assertThat(resource.getUserName()).isNotNull();
    Assertions.assertThat(resource.getId()).isNotNull();

    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getActive()).isNull();

  }
  
  @Test
  public void testExcludeAttributes() throws Exception {
    ScimUser resource = getScimUser();
    
    resource = attributeUtil.setExcludedAttributesForDisplay(resource, "");
    
    Assertions.assertThat(resource.getPassword()).isNull();

  }

  private void debugJson(Object resource) throws JsonGenerationException, JsonMappingException, IOException {
    StringWriter sw = new StringWriter();
    objectMapper.writeValue(sw, resource);
    LOG.info(sw.toString());
  }
  
  private ScimUser getScimUser() {
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
    PhoneNumber phoneNumber = new PhoneNumber();
    phoneNumber.setValue("123-456-7890");
    phoneNumber.setDisplay("123-456-7890");
    phoneNumber.setPrimary(true);
    phoneNumber.setType("home");

    phoneNumbers.add(phoneNumber);

    phoneNumber = new PhoneNumber();
    phoneNumber.setValue("1-800-555-1234");
    phoneNumber.setDisplay("1-800-555-1234");
    phoneNumber.setPrimary(false);
    phoneNumber.setType("work");

    phoneNumbers.add(phoneNumber);
    user.setPhoneNumbers(phoneNumbers);

    return user;
  }

}
