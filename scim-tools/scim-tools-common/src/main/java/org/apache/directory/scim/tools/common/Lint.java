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
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Validates that a SCIM resource conforms to the associated schema definition.
 * Note that it's possible to validate schemas since the associated schema
 * definition is provided.  It's also possible to validate the schema definition
 * against itself.  This class provides the code to provide linting SCIM
 * resources, but there will be CLI and web tools that 
 * @author smoyer1
 *
 */
public class Lint {
  
  ObjectMapper objectMapper = new ObjectMapper();

  JsonNode convert(InputStream inputStream) throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(inputStream);
  }
  
  boolean hasMeta(JsonNode jsonNode) {
    return jsonNode.has("meta");
  }
  
  boolean hasSchemas(JsonNode jsonNode) {
    return jsonNode.has("schemas");
  }
  
  boolean isSchema(JsonNode jsonNode) {
    boolean schema = false;
    JsonNode metaJsonNode = jsonNode.get("meta");
    if(metaJsonNode != null) {
      schema = "Schema".equals(metaJsonNode.get("resourceType").asText());
    }
    return schema;
  }
  
  public boolean lint(InputStream inputStream) throws JsonProcessingException, IOException {
    return lint(convert(inputStream));
  }
  
  boolean lint(JsonNode jsonNode) {
    boolean output = true;
    if(jsonNode.isArray()) {
      output = lintArray(jsonNode);
    } else if(jsonNode.isObject()) {
      output = lintObject(jsonNode);
    } else {
      // TODO - this is some sort of error
    }
    return output;
  }
  
  boolean lintArray(JsonNode arrayJsonNode) {
    boolean output = true;
    for(JsonNode jsonNode: arrayJsonNode) {
      if(jsonNode.isObject()) {
        output = output && lintObject(jsonNode);
      } else {
        // TODO - this is some sort of error
      }
    }
    return output;
  }
  
  boolean lintObject(JsonNode objectJsonNode) {
    boolean output = true;
    if(objectJsonNode.isObject()) {
      if(isSchema(objectJsonNode)) {
        output = lintSchema(objectJsonNode);
      } else {
        output = lintResource(objectJsonNode);
      }
    } else {
      // TODO - this is some sort of error
    }
    return output;
  }
  
  boolean lintResource(JsonNode resourceJsonNode) {
    return false;
  }
  
  boolean lintSchema(JsonNode schemaJsonNode) {
    return false;
  }

}
