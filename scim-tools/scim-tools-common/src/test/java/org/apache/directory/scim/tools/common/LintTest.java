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

package org.apache.directory.scim.tools.common;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LintTest {

  @ParameterizedTest
  @Disabled // TODO - Change the file names to match those provided by the
          //        scim-spec-schema module and figure out why some schemas
          //        don't have meta attributes.
  @ValueSource(strings = {
    "schemas/user-schema.json",
    "schemas/group-schema.json",
    "schemas/resource-type-schema.json",
    "schemas/schema-schema.json",
    "schemas/service-provider-configuration-schema.json",
    "schemas/enterprise-user-schema.json"
  })
  public void testConvertWithSchemas(String schemaFileName) throws IOException {
    Lint lint = new Lint();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(schemaFileName);

    try {
      JsonNode jsonNode = lint.convert(inputStream);
      assertTrue(jsonNode.isObject());
      assertFalse(lint.hasSchemas(jsonNode));
      assertTrue(lint.isSchema(jsonNode));
    } catch (JsonProcessingException e) {
      fail();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "examples/enterprise-user-example.json",
    "examples/full-user-example.json",
    "examples/group-example.json",
    "examples/minimal-user-example.json",
    "examples/resource-type-group-example.json",
    "examples/resource-type-user-example.json"
  })
  public void testConvertWithExamples(String exampleFileName) throws IOException {
    Lint lint = new Lint();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(exampleFileName);

    try {
      JsonNode jsonNode = lint.convert(inputStream);
      assertTrue(jsonNode.isObject());
      assertTrue(lint.hasSchemas(jsonNode));
    } catch (JsonProcessingException e) {
      fail();
    }
  }

}
