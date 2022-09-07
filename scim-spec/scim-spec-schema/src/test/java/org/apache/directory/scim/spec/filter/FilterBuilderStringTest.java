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

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterBuilder;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FilterBuilderStringTest {

  @Test
  public void testEndsWith() throws FilterParseException {
    Filter filter = FilterBuilder.create().endsWith("address.streetAddress", "Way").build();
    Filter expected = new Filter("address.streetAddress EW \"Way\"");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testStartsWith()  throws FilterParseException {
    Filter filter = FilterBuilder.create().startsWith("address.streetAddress", "133").build();
    Filter expected = new Filter("address.streetAddress SW \"133\"");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testContains()  throws FilterParseException {
    Filter filter = FilterBuilder.create().contains("address.streetAddress", "MacDuff").build();
    Filter expected = new Filter("address.streetAddress CO \"MacDuff\"");
    assertThat(filter).isEqualTo(expected);
  }
}
