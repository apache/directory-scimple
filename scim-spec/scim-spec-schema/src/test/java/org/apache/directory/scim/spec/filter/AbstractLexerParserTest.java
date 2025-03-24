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

package org.apache.directory.scim.spec.filter;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author stevemoyer
 *
 */
public abstract class AbstractLexerParserTest {

  protected static final String NO_FILTER = "";

  protected static final String JOHN_FILTER_MIXED_CASE_1 = "userName Eq \"john\"";
  protected static final String JOHN_FILTER_MIXED_CASE_2 = "Username eq \"john\"";
  protected static final String JOHN_FILTER_MIXED_CASE_3 = "Username EQ \"john\"";
  protected static final String JOHN_FILTER_MIXED_CASE_4 = "USERNAME EQ \"john\"";

  protected static final String EXAMPLE_1 = "userName eq \"bjensen\"";
  protected static final String EXAMPLE_2 = "name.familyName co \"O'Malley\"";
  protected static final String EXAMPLE_3 = "userName sw \"J\"";
  protected static final String EXAMPLE_4 = "title pr";
  protected static final String EXAMPLE_5 = "meta.lastModified gt \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_6 = "meta.lastModified ge \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_7 = "meta.lastModified lt \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_8 = "meta.lastModified le \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_9 = "title pr and userType eq \"Employee\"";
  protected static final String EXAMPLE_10 = "title pr or userType eq \"Intern\"";
  protected static final String EXAMPLE_11 = "userType eq \"Employee\" and (emails co \"example.com\" or emails co \"example.org\")";

  protected static final String[] EXAMPLES = { EXAMPLE_1, EXAMPLE_2, EXAMPLE_3, EXAMPLE_4, EXAMPLE_5, EXAMPLE_6, EXAMPLE_7, EXAMPLE_8, EXAMPLE_9, EXAMPLE_10, EXAMPLE_11 };

  protected static final String EXTRA_1 = "(emails co \"example.com\" or emails co \"example.org\")";
  protected static final String EXTRA_2 = "(emails co \"example.com\" or emails co \"example.org\") and userType eq \"Employee\"";

  protected static final String[] EXTRAS = { EXTRA_1, EXTRA_2 };

  protected static final String GROUP_EXAMPLE_1 = "not(userType eq \"Employee\")";
  protected static final String GROUP_EXAMPLE_1_1 = "(userType eq \"Employee\")";
  protected static final String GROUP_EXAMPLE_1_2 = "NOT(userType eq \"Employee\")";
  protected static final String GROUP_EXAMPLE_1_3 = "NOT (userType eq \"Employee\")";
  protected static final String GROUP_EXAMPLE_2 = "userType eq \"Employee\" and not(emails co \"example.com\" or emails co \"example.org\")";
  protected static final String GROUP_EXAMPLE_2_1 = "userType eq \"Employee\" and (emails co \"example.com\" or emails co \"example.org\")";
  protected static final String GROUP_EXAMPLE_2_2 = "userType eq \"Employee\" and NOT(emails co \"example.com\" or emails co \"example.org\")";
  protected static final String GROUP_EXAMPLE_2_3 = "userType eq \"Employee\" and NOT (emails co \"example.com\" or emails co \"example.org\")";
  protected static final String GROUP_EXAMPLE_3 = "not(userType eq \"Employee\") and not(userType eq \"Intern\")";
  protected static final String GROUP_EXAMPLE_3_1 = "(userType eq \"Employee\") and (userType eq \"Intern\")";
  protected static final String GROUP_EXAMPLE_4 = "not(userType eq \"Employee\" or userType eq \"Intern\")";
  protected static final String GROUP_EXAMPLE_4_1 = "(userType eq \"Employee\" or userType eq \"Intern\")";

  protected static final String[] GROUPS = { GROUP_EXAMPLE_1, GROUP_EXAMPLE_1_1, GROUP_EXAMPLE_1_2, GROUP_EXAMPLE_1_3, GROUP_EXAMPLE_2, GROUP_EXAMPLE_2_1, GROUP_EXAMPLE_2_2, GROUP_EXAMPLE_2_3, GROUP_EXAMPLE_3, GROUP_EXAMPLE_3_1, GROUP_EXAMPLE_4, GROUP_EXAMPLE_4_1 };

  protected static final String VALUE_EXPRESSION_1 = "emails[type eq \"work\"]";
  protected static final String VALUE_EXPRESSION_2 = "emails[type eq \"work\" and value co \"@example.com\"]";
  protected static final String VALUE_EXPRESSION_2_1 = "emails[type eq \"work\" AND value co \"@example.com\"]";
  protected static final String VALUE_EXPRESSION_3 = "emails[type eq \"work\" and value co \"@example.com\"] or ims[type eq \"xmpp\" and value co \"@foo.com\"]";
  protected static final String VALUE_EXPRESSION_3_1 = "emails[type eq \"work\" AND value co \"@example.com\"] OR ims[type eq \"xmpp\" and value co \"@foo.com\"]";

  protected static final String[] VALUE_EXPRESSIONS = { VALUE_EXPRESSION_1, VALUE_EXPRESSION_2, VALUE_EXPRESSION_2_1, VALUE_EXPRESSION_3, VALUE_EXPRESSION_3_1};

  protected static final String EXTENSION_1 = "urn:example:params:scim:schemas:extension:ExampleExtension:example pr";
  protected static final String EXTENSION_2 = "urn:example:params:scim:schemas:extension:ExampleExtension:example.sub pr";
  protected static final String EXTENSION_3 = "urn:example:params:scim:schemas:extension:ExampleExtension:examples[value eq true or type sw \"example\"]";
  protected static final String EXTENSION_4 = "urn:example:params:scim:schemas:extension:ExampleExtension:example1 eq \"example1\" and urn:example:params:scim:schemas:extension:ExampleExtension:example2 eq \"example2\"";

  protected static final String[] EXTENSIONS = { EXTENSION_1, EXTENSION_2, EXTENSION_3, EXTENSION_4 };

  protected static final String[] EXAMPLES_AND_EXTRAS = ArrayUtils.addAll(EXAMPLES, EXTRAS);

  protected static final String[] GROUPS_AND_VALUE_EXPRESSIONS = ArrayUtils.addAll(GROUPS, VALUE_EXPRESSIONS);
  
  protected static final String[] ALL = ArrayUtils.addAll(ArrayUtils.addAll(EXAMPLES_AND_EXTRAS, GROUPS_AND_VALUE_EXPRESSIONS), EXTENSIONS);

  protected static final String[] MIXED_CASE = { JOHN_FILTER_MIXED_CASE_1, JOHN_FILTER_MIXED_CASE_2, JOHN_FILTER_MIXED_CASE_3, JOHN_FILTER_MIXED_CASE_4 };

  protected static final String[] INPUT_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS = { "streetAddress EQ \"111 Heritage Way\"", "streetAddress EQ \"111 Heritage Way\nSuite S\"", "streetAddress EQ \"111 Heritage Way\rSuite S\"" };

  protected static final String[][] EXPECTED_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS = { { "streetAddress", "EQ", "\"111 Heritage Way\"" }, { "streetAddress", "EQ", "\"111 Heritage Way\nSuite S\"" }, { "streetAddress", "EQ", "\"111 Heritage Way\rSuite S\"" } };

}
