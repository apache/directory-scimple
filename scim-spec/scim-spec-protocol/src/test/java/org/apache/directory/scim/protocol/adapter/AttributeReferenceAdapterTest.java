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

package org.apache.directory.scim.protocol.adapter;

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeReferenceAdapterTest {

  private static Map<String, AttributeReference> TEST_DATA = Map.of(
    "urn:test:testAttributeName.testSubAttributeName", new AttributeReference("urn:test", "testAttributeName", "testSubAttributeName"),
    "urn:test:testAttributeName", new AttributeReference("urn:test", "testAttributeName"),
    "testAttributeName", new AttributeReference("testAttributeName")
  );

  @ParameterizedTest
  @MethodSource("marshalTestArgs")
  public void marshal(AttributeReference input, String expected) throws Exception {
    assertThat(new AttributeReferenceAdapter().marshal(input)).isEqualTo(expected);
  }

  @Test
  public void marshalNull() throws Exception {
    String result = new AttributeReferenceAdapter().marshal(null);
    assertThat(result).isNull();
  }

  @ParameterizedTest
  @MethodSource("unmarshalTestArgs")
  public void unmarshal(String input, AttributeReference expected) throws Exception {
    assertThat(new AttributeReferenceAdapter().unmarshal(input)).isEqualTo(expected);
  }

  @Test
  public void unmarshalNull() throws Exception {
    AttributeReference result = new AttributeReferenceAdapter().unmarshal(null);
    assertThat(result).isNull();
  }

  private static Stream<Arguments> unmarshalTestArgs() {
    return TEST_DATA.entrySet().stream()
      .map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  private static Stream<Arguments> marshalTestArgs() {
    return TEST_DATA.entrySet().stream()
      .map(e -> Arguments.of(e.getValue(), e.getKey()));
  }
}
