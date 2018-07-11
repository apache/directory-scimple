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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class LintTest {
  
  Lint lint;
  
  @Before
  public void setUp() {
    lint = new Lint();
  }

  @Test
  @Ignore // TODO - Change the file names to match those provided by the
          //        scim-spec-schema module and figure out why some schemas
          //        don't have meta attributes.
  @Parameters({
    "schemas/user-schema.json",
    "schemas/group-schema.json",
    "schemas/resource-type-schema.json",
    "schemas/schema-schema.json",
    "schemas/service-provider-configuration-schema.json",
    "schemas/enterprise-user-schema.json"
  })
  public void testConvertWithSchemas(String schemaFileName) throws IOException {
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

  @Test
  @Parameters({
    "examples/enterprise-user-example.json",
    "examples/full-user-example.json",
    "examples/group-example.json",
    "examples/minimal-user-example.json",
    "examples/resource-type-group-example.json",
    "examples/resource-type-user-example.json"
  })
  public void testConvertWithExamples(String exampleFileName) throws IOException {
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
