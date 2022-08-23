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

package org.apache.directory.scim.server.rest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.ExampleObjectExtension;
import org.apache.directory.scim.server.utility.ExampleObjectExtension.ComplexObject;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.json.ObjectMapperFactory;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttributeUtilTest {

  private static final Logger LOG = LoggerFactory.getLogger(AttributeUtilTest.class);

  Registry registry;

  AttributeUtil attributeUtil;

  private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() throws Exception {
    registry = Mockito.mock(Registry.class);
    attributeUtil = new AttributeUtil(registry);
    Schema scimUserSchema = ProviderRegistry.generateSchema(ScimUser.class);
    Schema scimEnterpriseUserSchema = ProviderRegistry.generateExtensionSchema(EnterpriseExtension.class);
    Schema scimExampleSchema = ProviderRegistry.generateExtensionSchema(ExampleObjectExtension.class);


    Mockito.when(registry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).thenReturn(scimUserSchema);
    Mockito.when(registry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(scimUserSchema);
    Mockito.when(registry.getSchema(EnterpriseExtension.URN)).thenReturn(scimEnterpriseUserSchema);
    Mockito.when(registry.getSchema(ExampleObjectExtension.URN)).thenReturn(scimExampleSchema);
    Mockito.when(registry.getAllSchemas()).thenReturn(Arrays.asList(scimUserSchema, scimEnterpriseUserSchema, scimExampleSchema));
    Mockito.when(registry.getAllSchemaUrns()).thenReturn(new HashSet<>(Arrays.asList(ScimUser.SCHEMA_URI, EnterpriseExtension.URN, ExampleObjectExtension.URN)));

    objectMapper = ObjectMapperFactory.getObjectMapper();
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
  
  @Test
  public void testIncludeAttributes() throws Exception {
    ScimUser resource = getScimUser();
    
    debugJson(resource);
    
    Set<AttributeReference> attributes = new HashSet<>();
    attributes.add(new AttributeReference("userName"));
    attributes.add(new AttributeReference("addresses.streetAddress"));
    resource = attributeUtil.setAttributesForDisplay(resource, attributes);
    
    debugJson(resource);

    Assertions.assertThat(resource.getUserName()).isNotNull();
    Assertions.assertThat(resource.getId()).isNotNull();

    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getActive()).isNull();
    
    Assertions.assertThat(resource.getAddresses().get(0).getCountry()).isNull();
    Assertions.assertThat(resource.getAddresses().get(0).getStreetAddress()).isNotNull();

    
    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);
    
    Assertions.assertThat(extension).as("%s should have been removed from extensions", EnterpriseExtension.URN).isNull();
  }
  
  @Test
  public void testIncludeFullAttributes() throws Exception {
    ScimUser resource = getScimUser();
    
    debugJson(resource);
    
    Set<AttributeReference> attributes = new HashSet<>();
    attributes.add(new AttributeReference("userName"));
    attributes.add(new AttributeReference("name"));
    attributes.add(new AttributeReference("addresses"));
    resource = attributeUtil.setAttributesForDisplay(resource, attributes);
    
    debugJson(resource);

    Assertions.assertThat(resource.getUserName()).isNotNull();
    Assertions.assertThat(resource.getId()).isNotNull();

    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getActive()).isNull();
    
    Assertions.assertThat(resource.getAddresses().get(0).getCountry()).isNotNull();
    Assertions.assertThat(resource.getAddresses().get(0).getStreetAddress()).isNotNull();
    Assertions.assertThat(resource.getAddresses().get(0).getCountry()).isNotNull();

    
    EnterpriseExtension extension = resource.getExtension(EnterpriseExtension.class);

    Assertions.assertThat(extension).as("%s should have been removed from extensions", EnterpriseExtension.URN).isNull();
  }
  
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
    
    debugJson(resource);

    Set<AttributeReference> attributeSet = new HashSet<>();
    attributeSet.add(new AttributeReference("userName"));
    attributeSet.add(new AttributeReference("addresses"));
    attributeSet.add(new AttributeReference("name"));
    
    resource = attributeUtil.setExcludedAttributesForDisplay(resource, attributeSet);
    
    debugJson(resource);

    Assertions.assertThat(resource.getId()).isNotNull();
    Assertions.assertThat(resource.getPassword()).isNull();
    Assertions.assertThat(resource.getUserName()).isNull();
    Assertions.assertThat(resource.getActive()).isNotNull();
    Assertions.assertThat(resource.getAddresses()).isNull();
    Assertions.assertThat(resource.getName()).isNull();

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
    
    LocalPhoneNumberBuilder lpnb = new LocalPhoneNumberBuilder();
    
    PhoneNumber phoneNumber = lpnb.areaCode("123").countryCode("1").subscriberNumber("456-7890").build();
    phoneNumber.setDisplay("123-456-7890");
    phoneNumber.setPrimary(true);
    phoneNumber.setType("home");

    phoneNumbers.add(phoneNumber);

    phoneNumber = new PhoneNumber();
    phoneNumber.setValue("tel:+1-800-555-1234");
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
