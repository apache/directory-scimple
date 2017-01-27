package edu.psu.swe.scim.spec.phonenumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.resources.PhoneNumber;
import edu.psu.swe.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import edu.psu.swe.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class PhoneNumberBuilderTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberBuilderTest.class);
  private static final String SUBSCRIBER_NUMBER = "subscriberNumber";
  private static final String COUNTRY_CODE = "countryCode";
  private static final String AREA_CODE = "areaCode";
  private static final String EXTENSION = "extension";
  private static final String SUBADDRESS = "subAddress";
  private static final String GLOBAL_NUMBER = "globalNumber";
  private static final String PHONE_NUMBER = "number";
  private static final String PHONE_CONTEXT = "phoneContext";
  private static final String FAILURE_MESSAGE = "IllegalArgumentException should have been thrown";
  // TODO params

  @SuppressWarnings("unused")
  private String[] getInvalidSubscriberNumbers() {
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
  private String[] getValidSubscriberNumbers() {
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

  @Test
	@Parameters(method = "getInvalidSubscriberNumbers")
  public void test_invalid_subscriberNumber_for_LocalPhoneNumberBuilder(String invalidSubscriberNumber) throws Exception {

    LOGGER.info("invalid subscriber number '" + invalidSubscriberNumber + "' start");
    try {
      new LocalPhoneNumberBuilder(invalidSubscriberNumber, null, null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
    
    String temp = invalidSubscriberNumber != null ? (" " + invalidSubscriberNumber + " ") : null; 
    LOGGER.info("invalid subscriber number '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder(temp, null, null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
    
  }
  
  @Test
  public void test_invalid_padded_subscriberNumber_for_LocalPhoneNumberBuilder() throws Exception {
    //parameterized value coming into test method has spaces stripped from beginning and end; need to test that spaces are not allowed at all
    try {
      new LocalPhoneNumberBuilder(" 23 ", null, null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(SUBSCRIBER_NUMBER));
    }
  }
  
  @Test
  @Parameters(method = "getValidSubscriberNumbers")
  public void test_valid_subscriberNumber_for_LocalPhoneNumberBuilder(String validSubscriberNumber) throws Exception {

    LOGGER.info("valid subscriber number '" + validSubscriberNumber + "' start");
    try {
      new LocalPhoneNumberBuilder(validSubscriberNumber, null, null).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assertTrue("Exception should have been for country code.", ex.getMessage().contains(COUNTRY_CODE));
    }
  }
  
  @SuppressWarnings("unused")
    private String[] getInvalidCountryCodes() {
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
    private String[] getValidCountryCodes() {
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
    
    @Test
    @Parameters(method = "getInvalidCountryCodes")
    public void test_invalid_countryCode_for_LocalPhoneNumberBuilder(String invalidCountryCode) throws Exception {

      LOGGER.info("invalid country code '" + invalidCountryCode + "' start");
      try {
        new LocalPhoneNumberBuilder("123-4567", invalidCountryCode, null).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        LOGGER.info(ex.getMessage());
        assert (ex.getMessage().contains(COUNTRY_CODE));
      }
      
      String temp = invalidCountryCode != null ? (" " + invalidCountryCode + " ") : null; 
      LOGGER.info("invalid country code '" + temp + "' start");
      try {
        new LocalPhoneNumberBuilder("123-4567", temp, null).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        assert (ex.getMessage().contains(COUNTRY_CODE));
      }
    }
    
    @Test
    public void test_invalid_padded_countryCode_for_LocalPhoneNumberBuilder() throws Exception {
      try {
        new LocalPhoneNumberBuilder("123-4567", " 23 ", null).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        LOGGER.info(ex.getMessage());
        assert (ex.getMessage().contains(COUNTRY_CODE));
      }
    }
    
    @Test
    @Parameters(method = "getValidCountryCodes")
    public void test_valid_countryCode_for_LocalPhoneNumberBuilder(String validCountryCode) throws Exception {

      LOGGER.info("valid country code '" + validCountryCode + "' start");
      try {
        new LocalPhoneNumberBuilder("123-4567", validCountryCode, null).build();
        fail(FAILURE_MESSAGE);
      } catch (IllegalArgumentException ex) {
        assertTrue("Exception should have been for area code.", ex.getMessage().contains(AREA_CODE));
      }
    }
    
    @SuppressWarnings("unused")
    private String[] getInvalidAreaCodes() {
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
         "@1234"
      };
    }
    
    @SuppressWarnings("unused")
    private String[] getValidAreaCodes() {
      return new String[] { 
         "1",
         "30",
         "995",
         "9875"
      };
    }
    
    @Test
    @Parameters(method = "getInvalidAreaCodes")
    public void test_invalid_areaCode_for_LocalPhoneNumberBuilder(String invalidAreaCode) throws Exception {

    LOGGER.info("invalid area code '" + invalidAreaCode + "' start");
    try {
      new LocalPhoneNumberBuilder("123-4567", "23", invalidAreaCode).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      LOGGER.info(ex.getMessage());
      assert (ex.getMessage().contains(AREA_CODE));
    }
    
    String temp = invalidAreaCode != null ? (" " + invalidAreaCode + " ") : null; 
    LOGGER.info("invalid area code '" + temp + "' start");
    try {
      new LocalPhoneNumberBuilder("123-4567", "23", temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(AREA_CODE));
    }
  }
  
  @Test
  public void test_invalid_padded_areaCode_for_LocalPhoneNumberBuilder() throws Exception {
    try {
      new LocalPhoneNumberBuilder("123-4567", "23", " 2 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      LOGGER.info("padded areaCode ->" + ex.getMessage());
      assert (ex.getMessage().contains(AREA_CODE));
    }
  }
  
  @Test
  @Parameters(method = "getValidAreaCodes")
  public void test_valid_areaCode_for_LocalPhoneNumberBuilder(String validAreaCode) throws Exception {

    LOGGER.info("valid area code '" + validAreaCode + "' start");
    
    PhoneNumber phoneNumber = new LocalPhoneNumberBuilder("123-4567", "23", validAreaCode).build();
    assertNull("Extension should be null", phoneNumber.getExtension());
    assertNull("SubAddress should be null", phoneNumber.getSubAddress());
    //assertEquals("123-4567", phoneNumber.getNumber());
    //assertEquals(("+23-" + validAreaCode), phoneNumber.getPhoneContext());
    assertEquals(("tel:123-4567;phone-context=+23-"+validAreaCode), phoneNumber.getValue());
  }
  
  @SuppressWarnings("unused")
  private String[] getInvalidGlobalNumbers() {
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
  private String[] getValidGlobalNumbers() {
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

  @Test
  @Parameters(method = "getInvalidGlobalNumbers")
  public void test_invalid_globalNumber_for_GlobalPhoneNumberBuilder(String invalidGlobalNumber) throws Exception {

    LOGGER.info("invalid global number '" + invalidGlobalNumber + "' start");
    try {
      new GlobalPhoneNumberBuilder(invalidGlobalNumber).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
    
    String temp = invalidGlobalNumber != null ? (" " + invalidGlobalNumber + " ") : null; 
    LOGGER.info("invalid global number '" + temp + "' start");
    try {
      new GlobalPhoneNumberBuilder(temp).build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
    
  }
  
  @Test
  public void test_invalid_padded_gloablNumber_for_GlobalPhoneNumberBuilder() throws Exception {
    //parameterized value coming into test method has spaces stripped from beginning and end; need to test that spaces are not allowed at all
    try {
      new GlobalPhoneNumberBuilder(" 23 ").build();
      fail(FAILURE_MESSAGE);
    } catch (IllegalArgumentException ex) {
      assert (ex.getMessage().contains(GLOBAL_NUMBER));
    }
  }
  
  @Test
  @Parameters(method = "getValidGlobalNumbers")
  public void test_valid_globalNumber_for_GlobalPhoneNumberBuilder(String validGlobalNumber) throws Exception {

    LOGGER.info("valid global number '" + validGlobalNumber + "' start");
    
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder(validGlobalNumber).build();
    assertNull("Extension should be null", phoneNumber.getExtension());
    assertNull("SubAddress should be null", phoneNumber.getSubAddress());
    assertNull("PhoneContext should be null", phoneNumber.getPhoneContext());
    
    //assertEquals("123-4567", phoneNumber.getNumber());
    assertEquals(("tel:"+validGlobalNumber), phoneNumber.getValue());

  }
  
  @Test
  @Parameters(method = "getValidGlobalNumbers")
  public void test_valid_noPlusSymbol_globalNumber_for_GlobalPhoneNumberBuilder(String validGlobalNumber) throws Exception {
    String temp = validGlobalNumber.replace("+", "");
    LOGGER.info("valid global number '" + temp + "' start");
    
    PhoneNumber phoneNumber = new GlobalPhoneNumberBuilder(temp).build();
    assertNull("Extension should be null", phoneNumber.getExtension());
    assertNull("SubAddress should be null", phoneNumber.getSubAddress());
    assertNull("PhoneContext should be null", phoneNumber.getPhoneContext());

    //assertEquals("123-4567", phoneNumber.getNumber());
    assertEquals(("tel:+"+temp), phoneNumber.getValue());
  }


}
