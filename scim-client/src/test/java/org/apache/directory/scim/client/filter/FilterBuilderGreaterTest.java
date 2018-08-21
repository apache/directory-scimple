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

package org.apache.directory.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.search.Filter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class FilterBuilderGreaterTest {

  static final Integer[] INT_EXAMPLES = { -1, -10, -111, 1, 12, 123, 1234, 12345, 123456 };
  static final Long[] LONG_EXAMPLES = { -1L, -10L, -111L, 3L, 33L, 333L, 3333L, 33333L, 333333L };
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
  public void testGreaterThanT_Int(Integer arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThan("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @ParameterizedTest
  @MethodSource("getLongExamples")
  public void getLongExamples(Long arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThan("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @ParameterizedTest
  @MethodSource("getFloatExamples")
  public void testGreaterThanT_Float(Float arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThan("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @ParameterizedTest
  @MethodSource("getDoubleExamples")
  public void testGreaterThanT_Double(Double arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThan("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  public void testGreaterThanDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().greaterThan("dog.dob", new Date()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testGreaterThanLocalDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().greaterThan("dog.dob", LocalDate.now()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testGreaterThanLocalDateTime() throws UnsupportedEncodingException, FilterParseException  {
    String encoded = FilterClient.builder().greaterThan("dog.dob", LocalDateTime.now()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @ParameterizedTest
  @MethodSource("getIntExamples")
  public void testGreaterThanOrEqualsT_Int(Integer arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @ParameterizedTest
  @MethodSource("getLongExamples")
  public void testGreaterThanOrEqualsT_Long(Long arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @ParameterizedTest
  @MethodSource("getFloatExamples")
  public void testGreaterThanOrEqualsT_Float(Float arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @ParameterizedTest
  @MethodSource("getDoubleExamples")
  public void testGreaterThanOrEqualsT_Double(Double arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.weight", arg).toString();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  public void testGreaterThanOrEqualsDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.dob", new Date()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testGreaterThanOrEqualsLocalDate()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.dob", LocalDate.now()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testGreaterThanOrEqualsLocalDateTime() throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().greaterThanOrEquals("dog.dob", LocalDateTime.now()).toString();
    Filter filter = new Filter(decode(encoded));
  }

  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }
}
