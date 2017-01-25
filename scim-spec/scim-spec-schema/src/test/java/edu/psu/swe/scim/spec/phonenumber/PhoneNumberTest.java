package edu.psu.swe.scim.spec.phonenumber;

import org.junit.Test;

import edu.psu.swe.scim.spec.resources.PhoneNumber;

public class PhoneNumberTest {

	@Test
	public void test_parser() throws Exception {
		String phoneUri = "tel:+1-201-555-0123";
		PhoneNumber phoneNumber = new PhoneNumber();
		phoneNumber.setValue(phoneUri);
		/*"tel:7042;phone-context=example.com";
	      "tel:863-1234;phone-context=+1-914-555"
	      'tel:+44 20 1234 5678', //UK International format
	        'tel:011 44 20 1234 5678', //Dialing from US to UK: note the + is replaced with the NANPA-standard international dialing prefix, 011, but this will vary by country
	        'tel:(0)20 1234 4567', //UK National format
	        'tel:020 1234 5678', //Dialing locally within the UK    
	        'tel:(02) 1234 5678', //
	        'tel:02 1234 5678', //
	        'tel:0411 123 123', // (but I've never seen 04 1112 3456)
	        'tel:131 123', //
	        'tel:13 1123', //
	        'tel:131 123', //
	        'tel:1 300 123 123', //
	        'tel:1300 123 123', //
	        'tel:02-1234-5678', //
	        'tel:1300-234-234', //
	        'tel:+44 (0)78 1234 1234', //
	        'tel:+44-78-1234-1234', //
	        'tel:+44-(0)78-1234-1234', //
	        'tel:0011 44 78 1234 1234', // (0011 is the standard international dialling code)
	        'tel:(44) 078 1234 1234' // (not common)
	        '814-235-1707',//US local dialing
	        'tel:814-235-1707',//US local dialing
	        'tel:814.235.1707',//US local dialing
	        'tel:814 235 1707',//US local dialing
	        'tel:+1-814-235-1707',
	        'tel:814-235-1707;ext=#4567',
	        'tel:814-235-1707;ext=#4567;ext=#1234',
	        'tel:1707;isub=sub.example.com',
	        'tel:1707;ext=#1234;isub=sub.example.com',
	        'tel:1707;phone-context=example.com',
	        'tel:235-1707;ext=#4567;phone-context=+1-814-555',      
	        'tel:235-1707;ext=#4567;phone-context=+1-814-555;par2=ghnkl23',
	        'tel:+44-20-1234-5678',
	        'tel:+44-20-1234-5678;phone-context=+44',
	        'tel:+44.20.1234.5678',
	        '+44.20.1234.5678',
	        'tel:(814) 235-1707;ext=#4567'
		    */
	}

}
