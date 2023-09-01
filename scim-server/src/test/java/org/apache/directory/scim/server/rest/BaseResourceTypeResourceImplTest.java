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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.ws.rs.core.UriInfo;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.server.utility.ExampleObjectExtension;
import org.apache.directory.scim.server.utility.ExampleObjectExtension.ComplexObject;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.EnterpriseExtension.Manager;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
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
  Repository<ScimUser> repository;
  
  AttributeReferenceListWrapper includedAttributeList = new AttributeReferenceListWrapper("name.givenName, name.familyName");
  AttributeReferenceListWrapper excludedAttributeList = new AttributeReferenceListWrapper("emails, phoneNumbers");
  
  @Test
  public void testGetProviderInternal_ScimServerExceptionThrownWhenNoProvider() throws ScimException {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();
    
    // when
    assertThrows(ScimException.class, () -> baseResourceImpl.getRepositoryInternal());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGetById_ForbiddenIfNoFilter() throws ScimException, ResourceException {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap queryParams = mock(MultivaluedMap.class);
    baseResourceImpl.uriInfo = uriInfo;

    when(uriInfo.getQueryParameters()).thenReturn(queryParams);
    when(queryParams.getFirst("filter")).thenReturn("not null");
    when(baseResourceImpl.getById("1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    Response response = baseResourceImpl.getById("1", includedAttributeList, excludedAttributeList);
    
    // then
    assertNotNull(response);
    assertEquals(response.getStatus(), Status.FORBIDDEN.getStatusCode());
  }
  
  @Test
  public void testQuery_NullParametersValid() throws ScimException, ResourceException {
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
    assertNotNull(response);
    assertEquals(response.getStatus(), Status.OK.getStatusCode());
  }
  
  @Test
  public void testCreate_ErrorIfBothAttributesAndExcludedAttributesExist() throws ScimException, ResourceException, PhoneNumberParseException {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    ScimUser scimUser = getScimUser();
    
    when(baseResourceImpl.create(scimUser, includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    ScimException exception = assertThrows(ScimException.class, () -> baseResourceImpl.create(scimUser, includedAttributeList, excludedAttributeList));

    // then
    assertEquals(exception.getStatus(), Status.BAD_REQUEST);
    assertThat(exception.getError().getDetail(), is("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testFind_ErrorIfBothAttributesAndExcludedAttributesExist() throws ScimException, ResourceException {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(includedAttributeList.getAttributeReferences());
    searchRequest.setExcludedAttributes(excludedAttributeList.getAttributeReferences());

    when(baseResourceImpl.find(searchRequest)).thenCallRealMethod();
    
    // when
    ScimException exception = assertThrows(ScimException.class, () -> baseResourceImpl.find(searchRequest));

    // then
    assertEquals(exception.getStatus(), Status.BAD_REQUEST);
    assertThat(exception.getError().getDetail(), is("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testUpdate_ErrorIfBothAttributesAndExcludedAttributesExist() throws ScimException, ResourceException, PhoneNumberParseException {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    ScimUser scimUser = getScimUser();
    
    when(baseResourceImpl.update(scimUser, "1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    ScimException exception = assertThrows(ScimException.class, () -> baseResourceImpl.update(scimUser, "1", includedAttributeList, excludedAttributeList));

    // then
    assertEquals(exception.getStatus(), Status.BAD_REQUEST);
    assertThat(exception.getError().getDetail(), is("Cannot include both attributes and excluded attributes in a single request"));
  }
  
  @Test
  public void testPatch_ErrorIfBothAttributesAndExcludedAttributesExist() throws Exception {
    // given
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    
    PatchRequest patchRequest = new PatchRequest();
    
    when(baseResourceImpl.patch(patchRequest, "1", includedAttributeList, excludedAttributeList)).thenCallRealMethod();
    
    // when
    ScimException exception = assertThrows(ScimException.class, () -> baseResourceImpl.patch(patchRequest, "1", includedAttributeList, excludedAttributeList));

    // then
    assertEquals(exception.getStatus(), Status.BAD_REQUEST);
    assertThat(exception.getError().getDetail(), is("Cannot include both attributes and excluded attributes in a single request"));
  }

  @Test
  public void repositoryNotImplemented() throws ScimException {
    // given
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = Mockito.mock(BaseResourceTypeResourceImpl.class);
    when(baseResourceImpl.getRepository()).thenReturn(null);
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();

    // when
    ScimException exception = assertThrows(ScimException.class, baseResourceImpl::getRepositoryInternal);

    // then
    assertEquals(exception.getStatus(), Status.NOT_IMPLEMENTED);
    assertThat(exception.getError().getDetail(), is("Provider not defined"));
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
