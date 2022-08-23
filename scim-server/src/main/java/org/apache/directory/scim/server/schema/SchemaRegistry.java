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

package org.apache.directory.scim.server.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SchemaRegistry {

  private Map<String, Schema> schemaMap = new HashMap<>();
  
  private Map<String, Class<? extends ScimResource>> schemaUrnToScimResourceClass = new HashMap<>();

  private Map<String, Class<? extends ScimResource>> endpointToScimResourceClass = new HashMap<>();

  private Map<String, ResourceType> resourceTypeMap = new HashMap<>();

  private ObjectMapper objectMapper;

  public Schema getSchema(String urn) {
    return schemaMap.get(urn);
  }

  public Set<String> getAllSchemaUrns() {
    return Collections.unmodifiableSet(schemaMap.keySet());
  }

  public Collection<Schema> getAllSchemas() {
    return Collections.unmodifiableCollection(schemaMap.values());
  }
  
  public Schema getBaseSchemaOfResourceType(String resourceType) {
    ResourceType rt = resourceTypeMap.get(resourceType);
    if (rt == null) {
      return null;
    }
    
    String schemaUrn = rt.getSchemaUrn();
    return schemaMap.get(schemaUrn);
  }

  public void addSchema(Schema schema) throws JsonProcessingException {
    log.info("Adding schema " + schema.getId() + " into the registry");
    schemaMap.put(schema.getId(), schema);
  }

  public void addSchemaDoc(String schemaDoc) {
    // Unmarshall the JSON document to a Schema and its associated object graph.
    try {
      Schema schema = objectMapper.readValue(schemaDoc, Schema.class);
      schemaMap.put(schema.getId(), schema);
    } catch (Throwable t) {
      log.error("Unexpected Throwable was caught while unmarshalling JSON, schema will not be added: " + t.getLocalizedMessage());
    }
  }

  public <T extends ScimResource> void addScimResourceSchemaUrn(String schemaUrn, Class<T> scimResourceClass) {
    schemaUrnToScimResourceClass.put(schemaUrn, scimResourceClass);
  }

  public <T extends ScimResource> void addScimResourceEndPoint(String endpoint, Class<T> scimResourceClass) {
    endpointToScimResourceClass.put(endpoint, scimResourceClass);
  }

  public <T extends ScimResource> Class<T> findScimResourceClassFromEndpoint(String endpoint) {
    @SuppressWarnings("unchecked")
    Class<T> scimResourceClass = (Class<T>) endpointToScimResourceClass.get(endpoint);

    return scimResourceClass;
  }

  public <T extends ScimResource> Class<T> findScimResourceClass(String schemaUrn) {
    @SuppressWarnings("unchecked")
    Class<T> scimResourceClass = (Class<T>) schemaUrnToScimResourceClass.get(schemaUrn);

    return scimResourceClass;
  }

  public ResourceType getResourceType(String name) {
    return resourceTypeMap.get(name);
  }
  
  public Collection<ResourceType> getAllResourceTypes() {
    return Collections.unmodifiableCollection(resourceTypeMap.values());
  }
  
  public void addResourceType(ResourceType resourceType) {
    resourceTypeMap.put(resourceType.getName(), resourceType);
  }

}
