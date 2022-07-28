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

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FilterBuilderLessThanTest {

  static final Integer[] INT_EXAMPLES = { 1, 12, 123, 1234, 12345, 123456 };
  static final Long[] LONG_EXAMPLES = { 3L, 33L, 333L, 3333L, 33333L, 333333L };
  static final Float [] FLOAT_EXAMPLES = {.14f, 3.14f, 2.1415f, 3.14E+10f, 333.14f};
  static final Double [] DOUBLE_EXAMPLES = {.14, 3.14, 2.1415, 3.14E+10, 333.14};
 
  static Integer[] getIntExamples() {
    return INT_EXAMPLES;
  }
  
  static Long[] getLongExamples() {
    return LONG_EXAMPLES;
  }
  
  static Float[] getFloatExamples() {
    return FLOAT_EXAMPLES;
  }
  
  static Double[] getDoubleExamples() {
    return DOUBLE_EXAMPLES;
  }
  
  @ParameterizedTest
  @MethodSource("getIntExamples")
  public void testLessThanT_Int(Integer arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThan("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LT " + arg);
    assertThat(filter).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("getLongExamples")
  public void testLessThanT_Long(Long arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThan("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LT " + arg);
    // values are parsed to integers, use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
  
  @ParameterizedTest
  @MethodSource("getFloatExamples")
  public void testLessThanT_Float(Float arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThan("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LT " + arg);
    // values are parsed to doubles, use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
  
  @ParameterizedTest
  @MethodSource("getDoubleExamples")
  public void testLessThanT_Double(Double arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThan("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LT " + arg);
    assertThat(filter).isEqualTo(expected);
  }
  
  
  @Test
  public void testLessThanDate() throws FilterParseException {
    Date now = new Date();
    Filter filter = FilterBuilder.create().lessThan("dog.dob", new Date()).build();
    Filter expected = new Filter("dog.dob LT \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").format(now) + "\""); // FIXME: format is missing TZ
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testLessThanLocalDate() throws FilterParseException {
    LocalDate now = LocalDate.now();
    Filter filter = FilterBuilder.create().lessThan("dog.dob", now).build();
    Filter expected = new Filter("dog.dob LT \"" + DateTimeFormatter.ISO_LOCAL_DATE.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testLessThanLocalDateTime() throws FilterParseException  {
    LocalDateTime now = LocalDateTime.now();
    Filter filter = FilterBuilder.create().lessThan("dog.dob", now).build();
    Filter expected = new Filter("dog.dob LT \"" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @ParameterizedTest
  @MethodSource("getIntExamples")
  public void testLessThanOrEqualsT_Int(Integer arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LE " + arg);
    assertThat(filter).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("getLongExamples")
  public void testLessThanOrEqualsT_Long(Long arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LE " + arg);
    // values are parsed to doubles, use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
  
  @ParameterizedTest
  @MethodSource("getFloatExamples")
  public void testLessThanOrEqualsT_Float(Float arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LE " + arg);
    // values are parsed to doubles, use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
  
  @ParameterizedTest
  @MethodSource("getDoubleExamples")
  public void testLessThanOrEqualsT_Double(Double arg) throws FilterParseException {
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.weight", arg).build();
    Filter expected = new Filter("dog.weight LE " + arg);
    // values are parsed to doubles, use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
  
  @Test
  public void testLessThanOrEqualsDate() throws FilterParseException {
    Date now = new Date();
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.dob", now).build();
    Filter expected = new Filter("dog.dob LE \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").format(now) + "\""); // FIXME: format is missing TZ
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testLessThanOrEqualsLocalDate()  throws FilterParseException {
    LocalDate now = LocalDate.now();
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.dob", now).build();
    Filter expected = new Filter("dog.dob LE \"" + DateTimeFormatter.ISO_LOCAL_DATE.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testLessThanOrEqualsLocalDateTime() throws FilterParseException {
    LocalDateTime now = LocalDateTime.now();
    Filter filter = FilterBuilder.create().lessThanOrEquals("dog.dob", now).build();
    Filter expected = new Filter("dog.dob LE \"" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }
}
