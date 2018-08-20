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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.apache.directory.scim.server.exception.ScimServerException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.utility.ExampleObjectExtension;
import org.apache.directory.scim.server.utility.ExampleObjectExtension.ComplexObject;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.PatchRequest;
import org.apache.directory.scim.spec.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseResourceTypeResourceImplTest {
  
  @Mock
  Provider<ScimUser> provider;
  
  AttributeReferenceListWrapper includedAttributeList = new AttributeReferenceListWrapper("name.givenName, name.familyName");
  AttributeReferenceListWrapper excludedAttributeList = new AttributeReferenceListWrapper("emails, phoneNumbers");
  
  @Test
  public void testGetProviderInternal_ScimServerExceptionThrownWhenNoProvider() throws ScimServerException {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    when(baseResourceImpl.getProviderInternal()).thenCallRealMethod();
    
    // when
    assertThrows(ScimServerException.class, () -> baseResourceImpl.getProviderInternal());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGetById_ForbiddenIfNoFilter() {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    baseResourceImpl.servletRequest = servletRequest;
    
    when(servletRequest.getParameter("filter")).thenReturn("not null");
    when(baseResourceImpl.getById("1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.getById("1", includedAttributeList, excludedAttributeList);
    
    // then
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.FORBIDDEN.getStatusCode());
  }
  
  @Test
  public void testQuery_NullParametersValid() {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(Collections.emptySet());
    searchRequest.setExcludedAttributes(Collections.emptySet());

    when(baseResourceImpl.find(searchRequest)).thenReturn(Response.ok().build());
    
    when(baseResourceImpl.query(null, null, null, null, null, null, null)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.query(null, null, null, null, null, null, null);
    
    // then
    verify(baseResourceImpl, times(1)).find(searchRequest);  
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.OK.getStatusCode());
  }
  
  @Test
  public void testCreate_ErrorIfBothAttributesAndExcludedAttributesExist() {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    ScimUser scimUser = null;
    try {
      scimUser = getScimUser();
    } catch (PhoneNumberParseException e) {
      fail("Parsing phone number in getScimUser failed");
    }
    
    when(baseResourceImpl.create(scimUser, includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.create(scimUser, includedAttributeList, excludedAttributeList);
    
    // then
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    assertTrue(response.getEntity() instanceof ErrorResponse);
    assertTrue(((ErrorResponse)response.getEntity()).getDetail().equals("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testFind_ErrorIfBothAttributesAndExcludedAttributesExist() {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(includedAttributeList.getAttributeReferences());
    searchRequest.setExcludedAttributes(excludedAttributeList.getAttributeReferences());

    when(baseResourceImpl.find(searchRequest)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.find(searchRequest);
    
    // then
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    assertTrue(response.getEntity() instanceof ErrorResponse);
    assertTrue(((ErrorResponse)response.getEntity()).getDetail().equals("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testUpdate_ErrorIfBothAttributesAndExcludedAttributesExist() {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    ScimUser scimUser = null;
    try {
      scimUser = getScimUser();
    } catch (PhoneNumberParseException e) {
      fail("Parsing phone number in getScimUser failed");
    }
    
    when(baseResourceImpl.update(scimUser, "1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.update(scimUser, "1", includedAttributeList, excludedAttributeList);
    
    // then
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    assertTrue(response.getEntity() instanceof ErrorResponse);
    assertTrue(((ErrorResponse)response.getEntity()).getDetail().equals("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testPatch_ErrorIfBothAttributesAndExcludedAttributesExist() throws Exception {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    PatchRequest patchRequest = new PatchRequest();
    
    when(baseResourceImpl.patch(patchRequest, "1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.patch(patchRequest, "1", includedAttributeList, excludedAttributeList);
    
    // then
    assertTrue(response != null);
    assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    assertTrue(response.getEntity() instanceof ErrorResponse);
    assertTrue(((ErrorResponse)response.getEntity()).getDetail().equals("Cannot include both attributes and excluded attributes in a single request"));
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
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("123-456-7890").countryCode("+1").build();
    phoneNumber.setDisplay("123-456-7890");
    phoneNumber.setPrimary(true);
    phoneNumber.setType("home");

    phoneNumbers.add(phoneNumber);

    phoneNumber = new GlobalPhoneNumberBuilder().globalNumber("1-800-555-1234").build();
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
