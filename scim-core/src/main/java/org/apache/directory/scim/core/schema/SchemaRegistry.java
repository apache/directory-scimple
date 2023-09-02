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

import java.io.Serializable;
import java.util.*;

import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.InvalidExtensionException;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.schema.Schemas;

@Slf4j
public class SchemaRegistry implements Serializable {

  private static final long serialVersionUID = 2644269305703474835L;
  private final Map<String, Schema> schemaMap = new HashMap<>();
  
  private final Map<String, Class<? extends ScimResource>> schemaUrnToScimResourceClass = new HashMap<>();

  private final Map<String, Class<? extends ScimResource>> endpointToScimResourceClass = new HashMap<>();

  private final Map<String, ResourceType> resourceTypeMap = new HashMap<>();

  private final Map<Class<? extends ScimResource>, Map<String, Class<? extends ScimExtension>>> resourceExtensionsMap = new HashMap<>();

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

  public <T extends ScimResource> void addSchema(Class<T> clazz, List<Class<? extends ScimExtension>> extensionList) {

    ScimResourceType scimResourceType = clazz.getAnnotation(ScimResourceType.class);
    if (scimResourceType == null) {
      throw new ScimResourceInvalidException("Missing annotation: ScimResource must be annotated with @ScimResourceType.");
    }

    ResourceType resourceType = generateResourceType(scimResourceType, extensionList);

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
        log.debug("Registering a extension of type: " + scimExtension);
        addExtension(clazz, scimExtension);
      }
    }
  }

  private <T extends ScimResource> void addScimResourceSchemaUrn(String schemaUrn, Class<T> scimResourceClass) {
    schemaUrnToScimResourceClass.put(schemaUrn, scimResourceClass);
  }

  private <T extends ScimResource> void addScimResourceEndPoint(String endpoint, Class<T> scimResourceClass) {
    endpointToScimResourceClass.put(endpoint, scimResourceClass);
  }

  public <T extends ScimResource> Class<T> getScimResourceClassFromEndpoint(String endpoint) {
    @SuppressWarnings("unchecked")
    Class<T> scimResourceClass = (Class<T>) endpointToScimResourceClass.get(endpoint);

    return scimResourceClass;
  }

  public <T extends ScimResource> Class<T> getScimResourceClass(String schemaUrn) {
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

  public Class<? extends ScimExtension> getExtensionClass(Class<? extends ScimResource> resourceClass, String urn) {
    Class<? extends ScimExtension> extensionClass = null;
    if(resourceExtensionsMap.containsKey(resourceClass)) {
      Map<String, Class<? extends ScimExtension>> resourceMap = resourceExtensionsMap.get(resourceClass);
      if(resourceMap.containsKey(urn)) {
        extensionClass = resourceMap.get(urn);
      }
    }
    return extensionClass;
  }

  public void addExtension(Class<? extends ScimResource> resourceClass, Class<? extends ScimExtension> extensionClass) {
    ScimExtensionType[] se = extensionClass.getAnnotationsByType(ScimExtensionType.class);

    if (se.length != 1) {
      throw new InvalidExtensionException("Registered extensions must a single @ScimExtensionType annotation");
    }

    String urn = se[0].id();

    log.debug("Registering extension for URN: '{}' associated resource class: '{}' and extension class: '{}'",
      urn, resourceClass.getSimpleName(), extensionClass.getSimpleName() );

    Map<String, Class<? extends ScimExtension>> resourceMap = resourceExtensionsMap.computeIfAbsent(resourceClass, k -> new HashMap<>());

    if(!resourceMap.containsKey(urn)) {
      resourceMap.put(urn, extensionClass);
    }
  }

  private ResourceType generateResourceType(ScimResourceType scimResourceType, List<Class<? extends ScimExtension>> extensionList) throws InvalidExtensionException {

    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    if (extensionList != null) {

      List<ResourceType.SchemaExtensionConfiguration> extensionSchemaList = new ArrayList<>();

      for (Class<? extends ScimExtension> se : extensionList) {

        ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

        if (extensionType == null) {
          throw new InvalidExtensionException("Missing annotation: @ScimExtensionType on ScimExtensionL " + se.getSimpleName());
        }

        ResourceType.SchemaExtensionConfiguration ext = new ResourceType.SchemaExtensionConfiguration();
        ext.setRequired(extensionType.required());
        ext.setSchemaUrn(extensionType.id());
        extensionSchemaList.add(ext);
      }

      resourceType.setSchemaExtensions(extensionSchemaList);
    }

    return resourceType;
  }
}
