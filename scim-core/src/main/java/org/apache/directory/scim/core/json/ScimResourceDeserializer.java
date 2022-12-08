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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class ScimResourceDeserializer extends StdDeserializer<ScimResource> {
  private final SchemaRegistry schemaRegistry;

  public ScimResourceDeserializer(SchemaRegistry schemaRegistry) {
    super(ScimResource.class);
    this.schemaRegistry = schemaRegistry;
  }

  @Override
  public ScimResource deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonLocation location = jsonParser.getCurrentLocation();
    TreeNode node = jsonParser.getCodec().readTree(jsonParser);
    ArrayNode schemas = (ArrayNode) node.get("schemas");

    Class<? extends ScimResource> scimResourceClass = StreamSupport.stream(schemas.spliterator(), false)
      .map(JsonNode::textValue)
      .map(schemaRegistry::getScimResourceClass)
      .filter(Objects::nonNull)
      .findFirst()
      .orElseThrow(() -> new JsonParseException(jsonParser, "Could not find a valid schema in: " + schemas + ", valid schemas are: " + schemaRegistry.getAllSchemaUrns(), location));

    return jsonParser.getCodec().treeToValue(node, scimResourceClass);
  }
}
