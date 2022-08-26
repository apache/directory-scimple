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

package org.apache.directory.scim.server.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.directory.scim.server.schema.SchemaRegistry;
import org.apache.directory.scim.spec.resources.ScimResource;

public class ScimResourceDeserializer extends JsonDeserializer<ScimResource> {
  private final SchemaRegistry schemaRegistry;
  private final ObjectMapper objectMapper;

  public ScimResourceDeserializer(SchemaRegistry schemaRegistry, ObjectMapper objectMapper) {
    this.schemaRegistry = schemaRegistry;
    this.objectMapper = objectMapper;
  }

  @Override
  public ScimResource deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    ScimResource scimResource;
    JsonLocation location = jsonParser.getCurrentLocation();
    TreeNode node = jsonParser.getCodec().readTree(jsonParser);
    ArrayNode schemas = (ArrayNode) node.get("schemas");
    Class<? extends ScimResource> scimResourceClass = null;

    for (JsonNode schemaUrnNode : schemas) {
      String schemaUrn = schemaUrnNode.textValue();
      scimResourceClass = schemaRegistry.findScimResourceClass(schemaUrn);

      if (scimResourceClass != null) {
        break;
      }
    }
    if (scimResourceClass == null) {
      throw new JsonParseException("Could not find a valid schema in: " + schemas + ", valid schemas are: " + schemaRegistry.getAllSchemaUrns(), location);
    }
    scimResource = objectMapper.readValue(node.toString(), scimResourceClass);

    return scimResource;
  }
}
