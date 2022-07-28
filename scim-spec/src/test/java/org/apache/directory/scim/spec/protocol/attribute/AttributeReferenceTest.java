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

package org.apache.directory.scim.spec.protocol.attribute;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  
  @ParameterizedTest
  @MethodSource("getAttributeReferences")
  public void testAttributeParsing(String attributeReferenceString, String expectedUrn, String expectedAttriubte) {
    AttributeReference attributeReference = new AttributeReference(attributeReferenceString);
    
    assertEquals(expectedUrn, attributeReference.getUrn());
    assertEquals(expectedAttriubte, attributeReference.getFullAttributeName());
    
  }
  
  @SuppressWarnings("unused")
  private static String[][] getAttributeReferences() {
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
