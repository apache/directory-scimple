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

package org.apache.directory.scim.spec.protocol.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.scim.spec.protocol.search.Filter;

public class FilterTest extends AbstractLexerParserTest {

  private static final Logger LOG = LoggerFactory.getLogger(FilterTest.class);

  @SuppressWarnings("unused")
  private static String[] getAllFilters() {
    return ALL;
  }

  @ParameterizedTest
  @MethodSource("getAllFilters")
  public void test(String filterText) throws Exception {
    LOG.info("Running Filter Parser test on input: " + filterText);
    Filter filter = new Filter(filterText);
    FilterExpression expression = filter.getExpression();
    LOG.info("Parsed String: " + expression.toFilter());
    Assertions.assertNotNull(expression);
  }
}
