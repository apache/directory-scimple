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

package org.apache.directory.scim.core.schema;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.schema.Schemas;

@Slf4j
@ApplicationScoped
public class SchemaRegistry {

  private Map<String, Schema> schemaMap = new HashMap<>();
  
  private Map<String, Class<? extends ScimResource>> schemaUrnToScimResourceClass = new HashMap<>();

  private Map<String, Class<? extends ScimResource>> endpointToScimResourceClass = new HashMap<>();

  private Map<String, ResourceType> resourceTypeMap = new HashMap<>();

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

  private void addSchema(Schema schema) {
    log.debug("Adding schema " + schema.getId() + " into the registry");
    schemaMap.put(schema.getId(), schema);
  }

  public <T extends ScimResource> void addSchema(Class<T> clazz, ResourceType resourceType, List<Class<? extends ScimExtension>> extensionList) {

    ScimResourceType scimResourceType = clazz.getAnnotation(ScimResourceType.class);
    if (scimResourceType == null) {
      throw new ScimResourceInvalidException("Missing annotation: ScimResource must be annotated with @ScimResourceType.");
    }

    String schemaUrn = scimResourceType.schema();
    String endpoint = scimResourceType.endpoint();

    addSchema(Schemas.schemaFor(clazz));
    addScimResourceSchemaUrn(schemaUrn, clazz);
    addScimResourceEndPoint(endpoint, clazz);
    addResourceType(resourceType);

    if (extensionList != null) {
      for (Class<? extends ScimExtension> scimExtension : extensionList) {
        log.debug("Calling addSchema on an extension: " + scimExtension);
        addSchema(Schemas.schemaForExtension(scimExtension));
      }
    }
  }

  private <T extends ScimResource> void addScimResourceSchemaUrn(String schemaUrn, Class<T> scimResourceClass) {
    schemaUrnToScimResourceClass.put(schemaUrn, scimResourceClass);
  }

  private <T extends ScimResource> void addScimResourceEndPoint(String endpoint, Class<T> scimResourceClass) {
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

  private void addResourceType(ResourceType resourceType) {
    resourceTypeMap.put(resourceType.getName(), resourceType);
  }
}
