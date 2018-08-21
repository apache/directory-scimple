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

package org.apache.directory.scim.spec.resources;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhoneNumberBuilderTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberBuilderTest.class);
  private static final String SUBSCRIBER_NUMBER = "subscriberNumber";
  private static final String COUNTRY_CODE = "countryCode";
  private static final String AREA_CODE = "areaCode";
  private static final String EXTENSION = "extension";
  private static final String SUBADDRESS = "subAddress";
  private static final String GLOBAL_NUMBER = "globalNumber";
  private static final String DOMAIN_NAME = "domainName";
  private static final String PARAMS_NAME_VALUE = "params names and values";
  private static final String FAILURE_MESSAGE = "IllegalArgumentException should have been thrown";
  private static final String FAILED_TO_PARSE = "failed to parse";

  @SuppressWarnings("unused")
  private static String[] getInvalidSubscriberNumbers() {
    return new String[] { 
       null,
       "",
       "A",
       "b",
       "#1234",
       "*1234",
       "@1234",
       "23 1234",
       "123 1234",
       "1234 1234",
       "123 12345",
       "123 123 456",
       "+1-888-888-8888"
    };
  }
  
  @SuppressWarnings("unused")
  private static String[] getValidSubscriberNumbers() {
    return new String[] { 
       "1",
       "1234",
       "12345",
       "123-1234",
       "123.1234",
       "(123)-1234",
       "(123).1234",
       "23-1234",
       "(23)-1234",
       "(23).1234",
       "1234-1234",
       "1234.1234",
       "(1234)-1234",
       "(1234).1234",
       "123-123456",
       "123.123456",
       "(123)-123456",
       "(123).123456",
       "(123)-123-456",
       "(123).123.456",
       "(22).33.44.55",
    };
  }

  @ParameterizedTest
	@MethodSource("getInvalidSubscriberNumbers")
  public void test_invalid_subscriberNumber_for_LocalPhoneNumberBuilder(String invalidSubscriberNumber) throws Exception {

    LOGGER.info("invalid subscriber number '" + invalidSubscriberNumber + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber(invalidSubscriberNumber).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
    
    String temp = invalidSubscriberNumber != null ? (" " + invalidSubscriberNumber + " ") : null; 
    LOGGER.info("invalid subscriber number '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber(temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
    
  }
  
  @Test
  public void test_invalid_padded_subscriberNumber_for_LocalPhoneNumberBuilder() throws Exception {
    //parameterized value coming into test method has spaces stripped from beginning and end; need to test that spaces are not allowed at all
    try {
      new LocalPhoneNumberBuilder().subscriberNumber(" 23 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
  }
  
  @ParameterizedTest
  @MethodSource("getValidSubscriberNumbers")
  public void test_valid_subscriberNumber_for_LocalPhoneNumberBuilder(String validSubscriberNumber) throws Exception {

    LOGGER.info("valid subscriber number '" + validSubscriberNumber + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber(validSubscriberNumber).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assertTrue(ex.getMessage().contains(COUNTRY_CODE), "Exception should have been for country code.");
    }
  }
  
  @SuppressWarnings("unused")
    private static String[] getInvalidCountryCodes() {
      return new String[] { 
         null,
         "",
         "A",
         "b",
         "b0",
         "b01",
         "b012",
         "bcd",
         "bcde",
         "+0bc",
         "0ef",
         "+",
         "0",
         "+0",
         "02",
         "+03",
         "056",
         "+078",
         "1234",
         "+1234",
         "@1234"
      };
    }
    
    @SuppressWarnings("unused")
    private static String[] getValidCountryCodes() {
      return new String[] { 
         "+1",
         "1",
         "+5",
         "6",
         "+40",
         "30",
         "+995",
         "995"
      };
    }
    
    @ParameterizedTest
    @MethodSource("getInvalidCountryCodes")
    public void test_invalid_countryCode_for_LocalPhoneNumberBuilder(String invalidCountryCode) throws Exception {

      LOGGER.info("invalid country code '" + invalidCountryCode + "' start");
      try {
        new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode(invalidCountryCode).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        LOGGER.info(ex.getMessage());
        assert (ex.getMessage().contains(COUNTRY_CODE));
      }
      
      String temp = invalidCountryCode != null ? (" " + invalidCountryCode + " ") : null; 
      LOGGER.info("invalid country code '" + temp + "' start");
      try {
        new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode(temp).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        assert (ex.getMessage().contains(COUNTRY_CODE));
      }
    }
    
    @ParameterizedTest
    @MethodSource("getValidCountryCodes")
    public void test_valid_countryCode_for_LocalPhoneNumberBuilder(String validCountryCode) throws Exception {

      LOGGER.info("valid country code '" + validCountryCode + "' start");

      PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode(validCountryCode).build();
      assertNull(phoneNumber.getExtension(), "Extension should be null");
      assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
      assertEquals("123-4567", phoneNumber.getNumber());
      
      
      String countryCode = validCountryCode.startsWith("+") ? validCountryCode : ("+"+validCountryCode);
      assertEquals(countryCode, phoneNumber.getPhoneContext());
      
      assertEquals(("tel:123-4567;phone-context=" + countryCode), phoneNumber.getValue());
    }
    
    @SuppressWarnings("unused")
    private static String[] getInvalidAreaCodes() {
      return new String[] { 
         "",
         "A",
         "b",
         "b0",
         "b01",
         "b012",
         "bcd",
         "bcde",
         "+0bc",
         "0ef",
         "+",
         "@1234"
      };
    }
    
    @SuppressWarnings("unused")
    private static String[] getValidAreaCodes() {
      return new String[] { 
         "1",
         "30",
         "995",
         "9875"
      };
    }
    
    @ParameterizedTest
    @MethodSource("getInvalidAreaCodes")
    public void test_invalid_areaCode_for_LocalPhoneNumberBuilder(String invalidAreaCode) throws Exception {

    LOGGER.info("invalid area code '" + invalidAreaCode + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").areaCode(invalidAreaCode).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      LOGGER.info(ex.getMessage());
      assert (ex.getMessage().contains(AREA_CODE));
    }
    
    String temp = invalidAreaCode != null ? (" " + invalidAreaCode + " ") : null; 
    LOGGER.info("invalid area code '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").areaCode(temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(AREA_CODE));
    }
  }
  
  @Test
  public void test_invalid_padded_areaCode_for_LocalPhoneNumberBuilder() throws Exception {
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").areaCode(" 2 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      LOGGER.info("padded areaCode ->" + ex.getMessage());
      assert (ex.getMessage().contains(AREA_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").areaCode("  ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      LOGGER.info("padded areaCode ->" + ex.getMessage());
      assert (ex.getMessage().contains(AREA_CODE));
    }
  }
  
  @Test
  public void test_areaCode_can_be_null_for_LocalPhoneNumberBuilder() throws Exception {
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").build();
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertEquals("123-4567", phoneNumber.getNumber());
    assertEquals("+23", phoneNumber.getPhoneContext());
    assertEquals("tel:123-4567;phone-context=+23", phoneNumber.getValue());    
  } 
  
  @ParameterizedTest
  @MethodSource("getValidAreaCodes")
  public void test_valid_areaCode_for_LocalPhoneNumberBuilder(String validAreaCode) throws Exception {

    LOGGER.info("valid area code '" + validAreaCode + "' start");
    
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("123-4567").countryCode("23").areaCode(validAreaCode).build();
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertEquals("123-4567", phoneNumber.getNumber());
    assertEquals(("+23-" + validAreaCode), phoneNumber.getPhoneContext());
    assertEquals(("tel:123-4567;phone-context=+23-"+validAreaCode), phoneNumber.getValue());
  }
  
  @SuppressWarnings("unused")
  private static String[] getInvalidGlobalNumbers() {
    return new String[] { 
       null,
       "",
       "A",
       "b",
       "#1234",
       "*1234",
       "@1234",
       "23 1234",
       "123 1234",
       "1234 1234",
       "123 12345",
       "123 123 456",
       "+1 888 888 8888"
    };
  }
  
  @SuppressWarnings("unused")
  private static String[] getValidGlobalNumbers() {
    return new String[] { 
      "+44-20-1234-5678",//global with visualSeparator -
      "+44.20.1234.5678",//global with visualSeparator .
      "+1-201-555-0123",//US global format with visualSeparator -
      "+1.201.555.0123",//US global format with visualSeparator .
      "+1(201)555.0123",//US global format with visualSeparator . and ()
      "+1(201)555-0123",//US global format with visualSeparator - and ()
      "+880-23-6666-7410",//Bangladesh
      "+886-912-345678",//Taiwan mobile
      "+674-556-7815",//Nauru, Oman
      "+676-27-987",//Tonga
      "+683-5791",//Niue & Tokelau
      "+686-22910",//Kiribati
      "+32-2-555-12-12", //Belguim big city
      "+32-71-123-456", //Belguim small city
      "+32-478-12-34-56", //Belguim mobile
      "+30-21-2-228-4931", //Greece
      "+352-79-0000", //Luxemburg
      "+352-2679-0000",
      "+352-4-000-00",
      "+970-08-240-7851",//Palestine
      "+970-059-240-7851",//Palestine mobile
      "+998-7071-123456789",//Uzbekistan
      "+998-7071-12345678",
      "+998-7071-1234567",
      "+998-74-123456789",
      "+998-751-12345678",
      "+998-62-1234567",
      "+996-312-123456",//Kyrgyzstan
      "+359-2-912-4501",//Bulgaria
      "+359-37-9873-571",//Bulgaria mobile
      "+358-9-333-444",//Finland
      "+358-045-123-45", //Finland mobile
      "+358-050-123-45-6", //Finland mobile
      "+358-045-123-45-67", //Finland mobile
      "+357-22-123456",//Cyprus
      "+357-9-987456",//Cyprus mobile
      "+44-28-9034-0812", //Northern Ireland
      "+44-7333-187-891", //Nothern Ireland mobile
      "+351-12-241-6789", //Portugal
      "+351-90-288-6789", //Portugal mobile
      "+506-5710-9874", //Costa Rica
      "+66-2-2134567", //Thailand
      "+66-44-2134567", //Thailand
      "+66-080-6345678", //Thailand mobile
      "+673-215-9642", //Brunei
      "+386-1-019-45-12",//Slovenia
      "+386-1-030-99-35",//Slovenia mobile
      "+7-499-123-78-56", //Russia
      "+52-744-235-4410",//Mexico
      "+52-55-235-4410",//Mexico
      "+855-23-430715", //Cambodia
      "+855-23-4307159", //Cambodia
      "+855-76-234-5678", //Cambodia mobile
      "+86-123-4567-8901", //China mobile
      "+86-852-123-4567", //China landline
      "+86-85-1234-5678", //China landline
      "+852-145-6789-0123", //China mobile
      "+852-852-123-4567", //China landline
      "+852-85-1234-5678", //China landline
      "+886-198-6541-2579", //China mobile
      "+886-852-123-4567", //China landline
      "+886-85-1234-5678", //China landline
      "+977-10-512-345", //Napal
      "+373-24-91-13-20", //Moldova
      "+33-03-71-13-20-43", //France
      "+43-(08)-9345-6765",//Austrailia +43
      "+49(05236)-5217775",//Germany +49
      "+49(032998)-651224",
      "+49(065)-51140357",
      "+81-004-477-3632",//Japan
      "+81-044-021-3258",
      "+81-005-920-5122",
      "+44-(026)-6987-1101",//UK
      "+44-055-6956-7230",
      "+44-(0117)-204-2623",
      "+44-07624-958791",
      "+1-(495)-172-7974",//Canada
      "+1-376-597-9524"
    };
  }

  @ParameterizedTest
  @MethodSource("getInvalidGlobalNumbers")
  public void test_invalid_globalNumber_for_GlobalPhoneNumberBuilder(String invalidGlobalNumber) throws Exception {

    LOGGER.info("invalid global number '" + invalidGlobalNumber + "' start");
    try {
      new GlobalPhoneNumberBuilder().globalNumber(invalidGlobalNumber).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
    
    String temp = invalidGlobalNumber != null ? (" " + invalidGlobalNumber + " ") : null; 
    LOGGER.info("invalid global number '" + temp + "' start");
    try {
      new GlobalPhoneNumberBuilder().globalNumber(temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
    
  }
  
  @Test
  public void test_invalid_padded_gloablNumber_for_GlobalPhoneNumberBuilder() throws Exception {
    //parameterized value coming into test method has spaces stripped from beginning and end; need to test that spaces are not allowed at all
    try {
      new GlobalPhoneNumberBuilder().globalNumber(" 23 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
  }
  
  @ParameterizedTest
  @MethodSource("getValidGlobalNumbers")
  public void test_valid_globalNumber_for_GlobalPhoneNumberBuilder(String validGlobalNumber) throws Exception {

    LOGGER.info("valid global number '" + validGlobalNumber + "' start");
    
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder().globalNumber(validGlobalNumber).build();
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertNull(phoneNumber.getPhoneContext(), "PhoneContext should be null");
    
    assertEquals(validGlobalNumber, phoneNumber.getNumber());
    assertEquals(("tel:"+validGlobalNumber), phoneNumber.getValue());

  }
  
  @ParameterizedTest
  @MethodSource("getValidGlobalNumbers")
  public void test_valid_noPlusSymbol_globalNumber_for_GlobalPhoneNumberBuilder(String validGlobalNumber) throws Exception {
    String temp = validGlobalNumber.replace("+", "");
    LOGGER.info("valid global number '" + temp + "' start");
    
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder().globalNumber(temp).build();
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertNull(phoneNumber.getPhoneContext(), "PhoneContext should be null");

    assertEquals("+" + temp, phoneNumber.getNumber());
    assertEquals(("tel:+" + temp), phoneNumber.getValue());
  }
  
  @SuppressWarnings("unused")
  private static String[] getInvalidDomainNames() {
    return new String[] { 
       "#1",
       "*1",
       "@1",
       "example com",
       "hhh@example.com",
       "EXAMPLE COM",
       "HHH@EXAMPLE.COM",
       "eXAmpLE Com2",
       "hhH@ExaMPlE.CoM2"
    };
  }
  
  @SuppressWarnings("unused")
  private static String[] getValidDomainNames() {
    return new String[] { 
      "google.com",
      "2xkcd-ex.com",
      "Ab0c1d2e3f4g5H6i7-J8K9l0m.org",
      "Ab0c1d2e3f4g5H6i7-J8K9l0m"
    };
  }
  
  @ParameterizedTest
  @MethodSource("getInvalidDomainNames")
  public void test_invalid_domainName_for_LocalPhoneNumberBuilder(String invalidDomainName) throws Exception {

    LOGGER.info("invalid domain name '" + invalidDomainName + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName(invalidDomainName).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME));
    }
    
    String temp = invalidDomainName != null ? (" " + invalidDomainName + " ") : null; 
    LOGGER.info("invalid domain name '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName(temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME));
    }
    
  }
  
  @Test
  public void test_invalid_padded_domainName_for_LocalPhoneNumberBuilder() throws Exception {
    //parameterized value coming into test method has spaces stripped from beginning and end; need to test that spaces are not allowed at all
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName(" 23 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME));
    }
  }
  
  @Test
  public void test_no_domainName_coutryCode_or_areaCode_for_LocalPhoneNumberBuilder() throws Exception {
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName(null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName("").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName("  ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("222-1707").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("222-1707").countryCode("").areaCode("").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
    
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("222-1707").countryCode("  ").areaCode("  ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(DOMAIN_NAME) && ex.getMessage().contains(COUNTRY_CODE));
    }
  }
  
  @ParameterizedTest
  @MethodSource("getValidDomainNames")
  public void test_valid_domainName_for_LocalPhoneNumberBuilder(String validDomainName) throws Exception {

    LOGGER.info("valid domain name '" + validDomainName + "' start");
    
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName(validDomainName).build();
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
   
    assertEquals("1707", phoneNumber.getNumber());
    assertEquals(validDomainName, phoneNumber.getPhoneContext());
    assertEquals(("tel:1707;phone-context="+validDomainName), phoneNumber.getValue());
  }
  
  @Test
  public void test_extension_subAddress_conflict_for_GlobalPhoneNumberBuilder() throws PhoneNumberParseException {
    GlobalPhoneNumberBuilder builder = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555");
    builder.build(); //should be valid builder at this point
    
    builder.extension("1234");
    builder.subAddress("example.a.com");
    
    try {
      builder.build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(EXTENSION) && ex.getMessage().contains(SUBADDRESS));
    }
  }
  
  @Test
  public void test_extension_subAddress_conflict_for_LocalPhoneNumberBuilder() throws PhoneNumberParseException {
    LocalPhoneNumberBuilder builder = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888");
    builder.build(); //should be valid builder at this point
    
    builder.extension("1234");
    builder.subAddress("example.a.com");
    
    try {
      builder.build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(EXTENSION) && ex.getMessage().contains(SUBADDRESS));
    }
  }

  @Test
  public void test_extension_for_GlobalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                              .extension("1234")
                              .build();
    
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertNull(phoneNumber.getPhoneContext(), "PhoneContext should be null");

    assertEquals("+1-888-888-5555", phoneNumber.getNumber());
    assertEquals("1234", phoneNumber.getExtension());
    assertEquals(("tel:+1-888-888-5555;ext=1234"), phoneNumber.getValue());
  }
  
  @Test
  public void test_extension_for_LocalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .extension("1234")
                              .build();
    
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");

    assertEquals("888-5555", phoneNumber.getNumber());
    assertEquals("+1-888", phoneNumber.getPhoneContext());
    assertEquals("1234", phoneNumber.getExtension());
    assertEquals(("tel:888-5555;ext=1234;phone-context=+1-888"), phoneNumber.getValue());
  }
  
  @Test
  public void test_subAddress_for_GlobalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                              .subAddress("example.a.com")
                              .build();
    
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    assertNull(phoneNumber.getPhoneContext(), "PhoneContext should be null");

    assertEquals("+1-888-888-5555", phoneNumber.getNumber());
    assertEquals("example.a.com", phoneNumber.getSubAddress());
    assertEquals(("tel:+1-888-888-5555;isub=example.a.com"), phoneNumber.getValue());
  }
  
  @Test
  public void test_subAddress_for_LocalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .subAddress("example.a.com")
                              .build();
    
    assertNull(phoneNumber.getExtension(), "Extension should be null");

    assertEquals("888-5555", phoneNumber.getNumber());
    assertEquals("+1-888", phoneNumber.getPhoneContext());
    assertEquals("example.a.com", phoneNumber.getSubAddress());
    assertEquals(("tel:888-5555;isub=example.a.com;phone-context=+1-888"), phoneNumber.getValue());
  }
  
  @Test
  public void test_adding_params_for_GlobalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
        .extension("1234")
        .param("example", "gh234")
        .param("milhouse", "simpson")
        .build();
    
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertNull(phoneNumber.getPhoneContext(), "PhoneContext should be null");

    assertEquals("+1-888-888-5555", phoneNumber.getNumber());
    assertEquals("1234", phoneNumber.getExtension());
    assertEquals(("tel:+1-888-888-5555;ext=1234;milhouse=simpson;example=gh234"), phoneNumber.getValue());
  }
  
  @Test
  public void test_adding_params_for_LocalPhoneNumberBuilder() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
        .subAddress("example.a.com")
        .param("example", "gh234")
        .param("milhouse", "simpson")
        .build();
    
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    
    assertEquals("888-5555", phoneNumber.getNumber());
    assertEquals("+1-888", phoneNumber.getPhoneContext());
    assertEquals("example.a.com", phoneNumber.getSubAddress());
    assertEquals(("tel:888-5555;isub=example.a.com;phone-context=+1-888;milhouse=simpson;example=gh234"), phoneNumber.getValue());
  }
  
  @Test
  public void test_adding_invalid_param_to_GlobalPhoneNumberBuilder() throws PhoneNumberParseException {
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param("example_", "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (PhoneNumberParseException ex) {
      assert (ex.getMessage().contains(FAILED_TO_PARSE));
      assert (ex.getMessage().contains("'_'"));
    }
    
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param("example", "gh234^").build();
      fail(FAILURE_MESSAGE);
    } catch (PhoneNumberParseException ex) {
      assert (ex.getMessage().contains(FAILED_TO_PARSE));
      assert (ex.getMessage().contains("'^'"));
    }
    
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param(null, "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param("", "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param("a", null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").param("a", "").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
  }  
  
  @Test
  public void test_adding_invalid_param_to_LocalPhoneNumberBuilder() throws PhoneNumberParseException {
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param("example*", "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (PhoneNumberParseException ex) {
      assert (ex.getMessage().contains(FAILED_TO_PARSE));
      assert (ex.getMessage().contains("'*'"));
    }
    
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param("example", "gh234\\").build();
      fail(FAILURE_MESSAGE);
    } catch (PhoneNumberParseException ex) {
      assert (ex.getMessage().contains(FAILED_TO_PARSE));
      assert (ex.getMessage().contains("'\\'"));
    }
    
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param(null, "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param("", "gh234").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param("a", null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
    
    try{ 
      new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("814").param("a", "").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(PARAMS_NAME_VALUE));
    }
  }

  @Test
  public void test_valid_subAddress() throws PhoneNumberParseException {
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName("example.a.com")
                              .subAddress("%20azAZ09?@=,+$&/:_!~.-()")
                              .build();
    
    assertNull(phoneNumber.getExtension(), "Extension should be null");
    
    assertEquals("1707", phoneNumber.getNumber());
    assertEquals("example.a.com", phoneNumber.getPhoneContext());
    assertEquals("%20azAZ09?@=,+$&/:_!~.-()", phoneNumber.getSubAddress());
    assertEquals(("tel:1707;isub=%20azAZ09?@=,+$&/:_!~.-();phone-context=example.a.com"), phoneNumber.getValue());
  }
  
  @Test
  public void test_invalid_subAddress() {
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1707").domainName("example.a.com").subAddress("azAZ09^example.(com)").build();
      fail(FAILURE_MESSAGE);
    } catch (PhoneNumberParseException ex) {
      assert (ex.getMessage().contains(FAILED_TO_PARSE));
    }
  }
  
  @SuppressWarnings("unused")
  private static String[] getInvalidExtensions() {
    return new String[] { 
       "",
       "A",
       "b",
       "#1234",
       "*1234",
       "@1234",
       "23 1234",
       "123 1234",
       "1234 1234",
       "123 12345",
       "123 123 456",
       "+1-888-888-8888"
    };
  }
  
  @SuppressWarnings("unused")
  private static String[] getValidExtensions() {
    return new String[] { 
       "1",
       "1234",
       "12345",
       "123-1234",
       "123.1234",
       "(123)-1234",
       "(123).1234",
       "23-1234",
       "(23)-1234",
       "(23).1234",
       "1234-1234",
       "1234.1234",
       "(1234)-1234",
       "(1234).1234",
       "123-123456",
       "123.123456",
       "(123)-123456",
       "(123).123456",
       "(123)-123-456",
       "(123).123.456",
       "(22).33.44.55",
    };
  }
  
  @ParameterizedTest
  @MethodSource("getValidExtensions")
  public void test_valid_extension(String validExtension) throws PhoneNumberParseException {
    LOGGER.info("valid extension '" + validExtension + "' start");
    
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder().subscriberNumber("1234-5678").countryCode("+44").areaCode("20")
                              .extension(validExtension)
                              .build();
    
    assertEquals(validExtension, phoneNumber.getExtension());
    assertNull(phoneNumber.getSubAddress(), "SubAddress should be null");
    assertEquals("1234-5678", phoneNumber.getNumber());
    assertEquals("+44-20", phoneNumber.getPhoneContext());
    
    assertEquals(("tel:1234-5678;ext=" + validExtension + ";phone-context=+44-20"), phoneNumber.getValue());
  }
  
  @ParameterizedTest
  @MethodSource("getInvalidExtensions")
  public void test_invalid_extension(String invalidExtension) throws PhoneNumberParseException {
    LOGGER.info("invalid extension " + invalidExtension);
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1234-5678").countryCode("+44").areaCode("20").extension(invalidExtension).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(EXTENSION));
    }
    
    String temp = invalidExtension != null ? (" " + invalidExtension + " ") : null; 
    LOGGER.info("invalid extension '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder().subscriberNumber("1234-5678").countryCode("+44").areaCode("20").extension(invalidExtension).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(EXTENSION));
    }
    
  }
  
  @Test
  public void test_paramsToLowerCase() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                     .param("ABCDE", "FGHIJKLM")
                     .param("NOPQR", "STUVWXYZ.-_")
                     .build();
    
    HashMap<String, String> lowerP = ph.paramsToLowerCase();
    
    assertEquals(2, lowerP.size());
    assertEquals("fghijklm", lowerP.get("abcde"));
    assertEquals("stuvwxyz.-_", lowerP.get("nopqr"));
  }
  
  @Test
  public void test_equalsIgnoreCaseAndOrderParams_not_equal() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                     .param("A", "B")
                     .param("C", "D_")
                     .build();
    
    PhoneNumber phOther = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
        .param("A", "B")
        .param("C", "E_")
        .build();
    
    assertFalse(ph.equalsIgnoreCaseAndOrderParams(phOther.getParams()));
  }
  
  @Test
  public void test_equalsIgnoreCaseAndOrderParams_equal() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                     .param("A", "B")
                     .param("C", "D_")
                     .build();
    
    PhoneNumber phOther = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
        .param("c", "D_")
        .param("A", "b")
        .build();
    
    assert(ph.equalsIgnoreCaseAndOrderParams(phOther.getParams()));
  }
  
  @Test
  public void test_equalsAndHashCode_globalNumber() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").build();
    assert(ph.isGlobalNumber());
    
    PhoneNumber phSame = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").build();
    assert(phSame.isGlobalNumber());

    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    assertFalse(localPh.isGlobalNumber());
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    assertFalse(localPhSame.isGlobalNumber());
    
    assertEquals(ph, phSame);
    assertEquals(localPh, localPhSame);
    
    assertNotEquals(ph, localPh);
  }
  
  @Test
  public void test_equalsAndHashCode_number() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555").build();
    
    PhoneNumber phSame = new GlobalPhoneNumberBuilder().globalNumber("+18888885555").build();
    
    PhoneNumber phDiff = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5556").build();
    
    assertEquals(ph, phSame);
    assertEquals(ph.hashCode(), phSame.hashCode());
    assertNotEquals(ph, phDiff);
    assertNotEquals(ph.hashCode(), phDiff.hashCode());
    
    
    
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("8885555").countryCode("+1").areaCode("888").build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-6666").countryCode("+1").areaCode("888").build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_extension() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                     .extension("12(34)")
                     .build();
    
    PhoneNumber phSame = new GlobalPhoneNumberBuilder().globalNumber("+18888885555")
                         .extension("1234") 
                         .build();
    
    PhoneNumber phDiff = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                         .extension("1235") 
                         .build();
    
    assertEquals(ph, phSame);
    assertEquals(ph.hashCode(), phSame.hashCode());
    assertNotEquals(ph, phDiff);
    assertNotEquals(ph.hashCode(), phDiff.hashCode());
    
    
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                          .extension("12(34)")
                          .build();
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("8885555").countryCode("+1").areaCode("888")
                              .extension("1234")
                              .build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .extension("1235")
                              .build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_subAddress() throws PhoneNumberParseException {
    PhoneNumber ph = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                     .subAddress("example.ZXC.com")
                     .build();
    
    PhoneNumber phSame = new GlobalPhoneNumberBuilder().globalNumber("+18888885555")
                         .subAddress("Example.zxc.com") 
                         .build();
    
    PhoneNumber phDiff = new GlobalPhoneNumberBuilder().globalNumber("+1-888-888-5555")
                         .subAddress("example.zxc.gov") 
                         .build();
    
    assertEquals(ph, phSame);
    assertEquals(ph.hashCode(), phSame.hashCode());
    assertNotEquals(ph, phDiff);
    assertNotEquals(ph.hashCode(), phDiff.hashCode());
    
    
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                          .subAddress("example.ZXC.com")
                          .build();
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("8885555").countryCode("+1").areaCode("888")
                              .subAddress("Example.zxc.com")
                              .build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .subAddress("example.zxc.gov")
                              .build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_phoneContext_as_domainName() throws PhoneNumberParseException {
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").domainName("example.ZXC.com").build();
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("8885555").domainName("Example.zxc.com").build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").domainName("example.zxc.gov").build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_phoneContext_as_digits() throws PhoneNumberParseException {
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();

    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("886").build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_params() throws PhoneNumberParseException {
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                          .param("A","c")
                          .param("B","d")
                          .build();

    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .param("a","C")
                              .param("b","D")
                              .build();
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888")
                              .param("A","e")
                              .param("B","d")
                              .build();
    
    PhoneNumber nullParamsPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
    
    assertNotEquals(localPh, nullParamsPh);
    assertNotEquals(localPh.hashCode(), nullParamsPh.hashCode());
    
    PhoneNumber globalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791")
                          .param("A","c")
                          .param("B","d")
                          .build();

    PhoneNumber globalPhSame = new GlobalPhoneNumberBuilder().globalNumber("+683-5791")
                               .param("a","C")
                               .param("b","D")
                               .build();

    PhoneNumber globalPhDiff = new GlobalPhoneNumberBuilder().globalNumber("+683-5791")
                               .param("A","e")
                               .param("f","D")
                                .build();

    PhoneNumber nullParamsGlobalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();

    assertEquals(globalPh, globalPhSame);
    assertEquals(globalPh.hashCode(), globalPhSame.hashCode());

    assertNotEquals(globalPh, globalPhDiff);
    assertNotEquals(globalPh.hashCode(), globalPhDiff.hashCode());

    assertNotEquals(globalPh, nullParamsGlobalPh);
    assertNotEquals(globalPh.hashCode(), nullParamsGlobalPh.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_phoneContext_primary_flag() throws PhoneNumberParseException {
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPh.setPrimary(Boolean.TRUE);
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPhSame.setPrimary(Boolean.TRUE);
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPhDiff.setPrimary(Boolean.FALSE);
    
    PhoneNumber nullPrimaryPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    nullPrimaryPh.setPrimary(null);
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
    
    assertNotEquals(localPh, nullPrimaryPh);
    assertNotEquals(localPh.hashCode(), nullPrimaryPh.hashCode());
    
    
    PhoneNumber globalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPh.setPrimary(true);
    
    PhoneNumber globalPhSame = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPhSame.setPrimary(true);
    
    PhoneNumber globalPhDiff = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPhDiff.setPrimary(false);
    
    PhoneNumber nullPrimaryGlobalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    nullPrimaryGlobalPh.setPrimary(null);
    
    assertEquals(globalPh, globalPhSame);
    assertEquals(globalPh.hashCode(), globalPhSame.hashCode());
    
    assertNotEquals(globalPh, globalPhDiff);
    assertNotEquals(globalPh.hashCode(), globalPhDiff.hashCode());
    
    assertNotEquals(globalPh, nullPrimaryGlobalPh);
    assertNotEquals(globalPh.hashCode(), nullPrimaryGlobalPh.hashCode());
  }
  
  @Test
  public void test_equalsAndHashCode_phoneContext_type() throws PhoneNumberParseException {
    PhoneNumber localPh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPh.setType("work");
    
    PhoneNumber localPhSame = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPhSame.setType("WORK");
    
    PhoneNumber localPhDiff = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    localPhDiff.setType("HOME");
    
    PhoneNumber nullTypePh = new LocalPhoneNumberBuilder().subscriberNumber("888-5555").countryCode("+1").areaCode("888").build();
    assertNull(nullTypePh.getType());
    
    assertEquals(localPh, localPhSame);
    assertEquals(localPh.hashCode(), localPhSame.hashCode());
    
    assertNotEquals(localPh, localPhDiff);
    assertNotEquals(localPh.hashCode(), localPhDiff.hashCode());
    
    assertNotEquals(localPh, nullTypePh);
    assertNotEquals(localPh.hashCode(), nullTypePh.hashCode());
    
    
    PhoneNumber globalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPh.setType("mobile");
    
    PhoneNumber globalPhSame = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPhSame.setType("MOBILE");
    
    PhoneNumber globalPhDiff = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    globalPhDiff.setType("fax");
    
    PhoneNumber nullTypeGlobalPh = new GlobalPhoneNumberBuilder().globalNumber("+683-5791").build();
    assertNull(nullTypeGlobalPh.getType());
    
    assertEquals(globalPh, globalPhSame);
    assertEquals(globalPh.hashCode(), globalPhSame.hashCode());
    
    assertNotEquals(globalPh, globalPhDiff);
    assertNotEquals(globalPh.hashCode(), globalPhDiff.hashCode());
    
    assertNotEquals(globalPh, nullTypeGlobalPh);
    assertNotEquals(globalPh.hashCode(), nullTypeGlobalPh.hashCode());
  }
  
  

}
