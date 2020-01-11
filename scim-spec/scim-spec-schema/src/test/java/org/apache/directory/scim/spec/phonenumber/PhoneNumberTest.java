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

package org.apache.directory.scim.spec.phonenumber;

import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhoneNumberTest {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberTest.class);
  
  @SuppressWarnings("unused")
  private static String[] getAllValidPhones() {
    return new String[] { 
      "tel:7042;phone-context=example.com",//local number
      "tel:863-1234;phone-context=+1-914-555",//local number
      "tel:235-1707;ext=4567;phone-context=+1-814-555",//local with ext and context      
      "tel:235-1707;isub=example.sub.com;phone-context=+1-814-555",//local with isub and context      
      "tel:235-1707;ext=4567;phone-context=+1-814-555;par2=ghnkl23",//local with ext, context and additional param
      "tel:235-1707;isub=example.sub.com;phone-context=+1-814-555;par2=ghnkl23",//local with isub, context and additional param
      
      "tel:+44-20-1234-5678",//global with visualSeparator -
      "tel:+44.20.1234.5678",//global with visualSeparator .
      "tel:+44.20.1234.5678;ext=4567",//global with ext      
      "tel:+44.20.1234.5678;isub=example.sub.com",//global with isub      
      "tel:+44.20.1234.5678;ext=4567;par2=ghnkl23",//global with ext and additional param
      "tel:+44.20.1234.5678;isub=example.sub.com;par2=ghnkl23",//global with isub and additional param
      
      "tel:+1-201-555-0123",//US global format with visualSeparator -
      "tel:+1.201.555.0123",//US global format with visualSeparator .
      "tel:+1(201)555.0123",//US global format with visualSeparator . and ()
      "tel:+1(201)555-0123",//US global format with visualSeparator - and ()
      "tel:+1-201-555-0123;ext=1234",//US global format with extension
      "tel:+880-23-6666-7410",//Bangladesh
      "tel:+886-912-345678",//Taiwan mobile
      "tel:+674-556-7815",//Nauru, Oman
      "tel:+676-27-987",//Tonga
      "tel:+683-5791",//Niue & Tokelau
      "tel:+686-22910",//Kiribati
      "tel:+32-2-555-12-12", //Belguim big city
      "tel:+32-71-123-456", //Belguim small city
      "tel:+32-478-12-34-56", //Belguim mobile
      "tel:+30-21-2-228-4931", //Greece
      "tel:+352-79-0000", //Luxemburg
      "tel:+352-2679-0000",
      "tel:+352-4-000-00",
      "tel:+970-08-240-7851",//Palestine
      "tel:+970-059-240-7851",//Palestine mobile
      "tel:+998-7071-123456789",//Uzbekistan
      "tel:+998-7071-12345678",
      "tel:+998-7071-1234567",
      "tel:+998-74-123456789",
      "tel:+998-751-12345678",
      "tel:+998-62-1234567",
      "tel:+996-312-123456",//Kyrgyzstan
      "tel:+359-2-912-4501",//Bulgaria
      "tel:+359-37-9873-571",//Bulgaria mobile
      "tel:+358-9-333-444",//Finland
      "tel:+358-045-123-45", //Finland mobile
      "tel:+358-050-123-45-6", //Finland mobile
      "tel:+358-045-123-45-67", //Finland mobile
      "tel:+357-22-123456",//Cyprus
      "tel:+357-9-987456",//Cyprus mobile
      "tel:+44-28-9034-0812", //Northern Ireland
      "tel:+44-7333-187-891", //Nothern Ireland mobile
      "tel:+351-12-241-6789", //Portugal
      "tel:+351-90-288-6789", //Portugal mobile
      "tel:+506-5710-9874", //Costa Rica
      "tel:+66-2-2134567", //Thailand
      "tel:+66-44-2134567", //Thailand
      "tel:+66-080-6345678", //Thailand mobile
      "tel:+673-215-9642", //Brunei
      "tel:+386-1-019-45-12",//Slovenia
      "tel:+386-1-030-99-35",//Slovenia mobile
      "tel:+7-499-123-78-56", //Russia
      "tel:+52-744-235-4410",//Mexico
      "tel:+52-55-235-4410",//Mexico
      "tel:+855-23-430715", //Cambodia
      "tel:+855-23-4307159", //Cambodia
      "tel:+855-76-234-5678", //Cambodia mobile
      "tel:+86-123-4567-8901", //China mobile
      "tel:+86-852-123-4567", //China landline
      "tel:+86-85-1234-5678", //China landline
      "tel:+852-145-6789-0123", //China mobile
      "tel:+852-852-123-4567", //China landline
      "tel:+852-85-1234-5678", //China landline
      "tel:+886-198-6541-2579", //China mobile
      "tel:+886-852-123-4567", //China landline
      "tel:+886-85-1234-5678", //China landline
      "tel:+977-10-512-345", //Napal
      "tel:+373-24-91-13-20", //Moldova
      "tel:+33-03-71-13-20-43", //France
      "tel:+43-(08)-9345-6765",//Austrailia +43
      "tel:+49(05236)-5217775",//Germany +49
      "tel:+49(032998)-651224",
      "tel:+49(065)-51140357",
      "tel:+81-004-477-3632",//Japan
      "tel:+81-044-021-3258",
      "tel:+81-005-920-5122",
      "tel:+44-(026)-6987-1101",//UK
      "tel:+44-055-6956-7230",
      "tel:+44-(0117)-204-2623",
      "tel:+44-07624-958791",
      "tel:+1-(495)-172-7974",//Canada
      "tel:+1-376-597-9524"
    };
  }
  
  @SuppressWarnings("unused")
  private static String[] getAllInvalidPhones() {
    return new String[] {
      "",//missing prefix and numbers
      "tel:",//missing numbers
      "201-555-0123",//missing prefix
      "tel:201 555 0123",//not allowed spaces
      "tel:201-555-0123",//no phone-context
      "tel:814-235-1707;ext=4567", //no phone-context for local
      "tel:235-1707;ext=4567;ext=1234;phone-context:+1=814-555", //two ext params
      "tel:235-1707;phone-context:+1=814-555;ext=4567", //ext in wrong order
      "tel:235-1707;ext=4567;isub=example.phone.com;phone-context:+1=814-555",//has both ext and isub; allowed only one
      "tel:1707;isub=sub.example.com",//no phone-context
      "tel:1707;ext=1234;isub=sub.example.com",//local with ext and isub, no phone-context
      "tel:865-8773;ext=#44;phone-context:+1-814-555",//local with symbol in ext param
      "tel:(814) 235-1707;ext=4567",//spaces not allowed
      
      "+1-201-555-0123", //no prefix
      "tel:+1-814-235-1707;ext=4567;ext=1234", //two ext params
      "tel:+1-814-235-1707;ext=4567;isub=example.phone.com", //has both ext and isub; allowed only one
      
      "+44.20.1234.5678",//no prefix
      "tel:+44-20-1234-5678;phone-context=+44",//phone-context not allowed on global number
      "tel:+44-20-1234-5678;ext=#44", //global with symbol in ext param
      "tel:+358-4x-123-4", //Finland mobile (wikipedia says that this is ok???)

    };
  }
	
	@ParameterizedTest
	@MethodSource("getAllValidPhones")
	public void test_parser_with_valid_phone_numbers(String phoneUri) throws Exception {
	  LOGGER.info("valid phones (" + phoneUri + ") start");
		PhoneNumber phoneNumber = new PhoneNumber();
		phoneNumber.setValue(phoneUri);
	}
	
	@ParameterizedTest
  @MethodSource("getAllInvalidPhones")
  public void test_parser_with_invalid_phone_numbers(String phoneUri) throws PhoneNumberParseException {
	  LOGGER.info("invalid phones (" + phoneUri + ") start");
    PhoneNumber phoneNumber = new PhoneNumber();

    Assertions.assertThrows(PhoneNumberParseException.class, () -> phoneNumber.setValue(phoneUri));
  }

}
