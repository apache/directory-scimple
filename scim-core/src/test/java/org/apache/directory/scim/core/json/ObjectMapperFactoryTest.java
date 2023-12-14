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

package org.apache.directory.scim.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.test.stub.ExampleObjectExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ObjectMapperFactoryTest {

  @Test
  public void serialize() throws JsonProcessingException {
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    schemaRegistry.addSchema(ScimUser.class, List.of(ExampleObjectExtension.class));
    schemaRegistry.addExtension(ScimUser.class, ExampleObjectExtension.class);

    ScimResource resource = new ScimUser().setId("test1");
    ExampleObjectExtension extension = new ExampleObjectExtension().setValueDefault("test-value");
    resource.addExtension(extension);

    ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper(schemaRegistry);
    String json = objectMapper.writeValueAsString(resource);

    ScimResource actual = objectMapper.readValue(json, ScimResource.class);

    Assertions.assertThat(actual).isEqualTo(resource);
  }
}
