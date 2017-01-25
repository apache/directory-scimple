package edu.psu.swe.scim.spec.phonenumber;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.resources.PhoneNumber;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class PhoneNumberTest {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberTest.class);
  
  @SuppressWarnings("unused")
  private String[] getAllValidPhones() {
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
      "tel:+1-201-555-0123;ext=1234"//US global format with extension
    };
  }
  
  @SuppressWarnings("unused")
  private String[] getAllInvalidPhones() {
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
      "tel:+44-20-1234-5678;ext=#44"//global with symbol in ext param
    };
  }
	
	@Test
	@Parameters(method = "getAllValidPhones")
	public void test_parser_with_valid_phone_numbers(String phoneUri) throws Exception {
	  LOGGER.info("valid phones (" + phoneUri + ") start");
		PhoneNumber phoneNumber = new PhoneNumber();
		phoneNumber.setValue(phoneUri);
	}
	
	@Test(expected = IllegalStateException.class)
  @Parameters(method = "getAllInvalidPhones")
  public void test_parser_with_invalid_phone_numbers(String phoneUri) {
	  LOGGER.info("invalid phones (" + phoneUri + ") start");
    PhoneNumber phoneNumber = new PhoneNumber();
    phoneNumber.setValue(phoneUri);
  }

}
