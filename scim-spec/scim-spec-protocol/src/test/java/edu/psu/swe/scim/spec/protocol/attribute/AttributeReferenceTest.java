package edu.psu.swe.scim.spec.protocol.attribute;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AttributeReferenceTest {
  
  private static final String USER_NAME = "userName";
  private static final String NAME = "name";
  private static final String GIVEN_NAME = "givenName";
  private static final String NAME_GIVEN_NAME = NAME + "." + GIVEN_NAME;
  private static final String EMPLOYEE_NUMBER = "employeeNumber";
  private static final String CORE_USER_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";
  private static final String ENTERPRISE_USER_SCHEMA = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";
  
  
  protected static final String EXAMPLE_1 = USER_NAME;
  protected static final String EXAMPLE_2 = USER_NAME.toLowerCase();
  protected static final String EXAMPLE_3 = NAME_GIVEN_NAME;
  protected static final String EXAMPLE_4 = CORE_USER_SCHEMA + ":" + USER_NAME;
  protected static final String EXAMPLE_5 = CORE_USER_SCHEMA + ":" + USER_NAME.toLowerCase();
  protected static final String EXAMPLE_6 = CORE_USER_SCHEMA + ":" + NAME_GIVEN_NAME;
  protected static final String EXAMPLE_7 = ENTERPRISE_USER_SCHEMA + ":" + EMPLOYEE_NUMBER;
  
  @Test
  @Parameters(method = "getAttributeReferences")
  public void testAttributeParsing(String attributeReferenceString, String expectedUrn, String expectedAttriubte) {
    AttributeReference attributeReference = new AttributeReference(attributeReferenceString);
    
    Assert.assertEquals(expectedUrn, attributeReference.getUrn());
    Assert.assertEquals(expectedAttriubte, attributeReference.getFullAttributeName());
    
  }
  
  @SuppressWarnings("unused")
  private String[][] getAttributeReferences() {
    return new String[][] {
        {EXAMPLE_1, null, USER_NAME},
        {EXAMPLE_2, null, USER_NAME.toLowerCase()},
        {EXAMPLE_3, null, NAME_GIVEN_NAME},
        {EXAMPLE_4, CORE_USER_SCHEMA, USER_NAME},
        {EXAMPLE_5, CORE_USER_SCHEMA, USER_NAME.toLowerCase()},
        {EXAMPLE_6, CORE_USER_SCHEMA, NAME_GIVEN_NAME},
        {EXAMPLE_7, ENTERPRISE_USER_SCHEMA, EMPLOYEE_NUMBER}
    };
    
  }

}
