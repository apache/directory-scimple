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

package org.apache.directory.scim.server.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.server.ScimConfiguration;
import org.apache.directory.scim.server.exception.InvalidRepositoryException;
import org.apache.directory.scim.server.exception.UnableToRetrieveExtensionsResourceException;
import org.apache.directory.scim.server.schema.SchemaRegistry;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schemas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@ApplicationScoped
public class RepositoryRegistry implements ScimConfiguration {

  private SchemaRegistry schemaRegistry;

  private ScimExtensionRegistry scimExtensionRegistry;

  // Weld needs the '? extends' or the repositories will not be found, some CDI
  // implementations work fine with just <ScimResources>
  private Instance<Repository<? extends ScimResource>> scimRepositoryInstances;

  private Map<Class<? extends ScimResource>, Repository<? extends ScimResource>> repositoryMap = new HashMap<>();

  @Inject
  public RepositoryRegistry(SchemaRegistry schemaRegistry, ScimExtensionRegistry scimExtensionRegistry, Instance<Repository<? extends ScimResource>> scimRepositoryInstances) {
    this.schemaRegistry = schemaRegistry;
    this.scimExtensionRegistry = scimExtensionRegistry;
    this.scimRepositoryInstances = scimRepositoryInstances;
  }

  RepositoryRegistry() {}

  @Override
  @SuppressWarnings("unchecked")
  public void configure() {
    scimRepositoryInstances.stream()
      .map(repository -> (Repository<ScimResource>) repository)
      .forEach(repository -> {
      try {
        registerRepository(repository.getResourceClass(), repository);
      } catch (InvalidRepositoryException | JsonProcessingException | UnableToRetrieveExtensionsResourceException e) {
        throw new ScimResourceInvalidException("Failed to register repository " + repository.getClass() + " for ScimResource type " + repository.getResourceClass(), e);
      }
    });
  }

  public synchronized <T extends ScimResource> void registerRepository(Class<T> clazz, Repository<T> repository) throws InvalidRepositoryException, JsonProcessingException, UnableToRetrieveExtensionsResourceException {

    ResourceType resourceType = generateResourceType(clazz, repository);

    log.info("Calling addSchema on the base class: {}", clazz);
    schemaRegistry.addSchema(Schemas.schemaFor(clazz));
    // NOTE generateResourceType() ensures ScimResourceType exists
    ScimResourceType scimResourceType = clazz.getAnnotation(ScimResourceType.class);
    String schemaUrn = scimResourceType.schema();
    String endpoint = scimResourceType.endpoint();
    schemaRegistry.addScimResourceSchemaUrn(schemaUrn, clazz);
    schemaRegistry.addScimResourceEndPoint(endpoint, clazz);

    List<Class<? extends ScimExtension>> extensionList = repository.getExtensionList();

    if (extensionList != null) {
      for (Class<? extends ScimExtension> scimExtension : extensionList) {
        log.info("Registering a extension of type: " + scimExtension);
        scimExtensionRegistry.registerExtension(clazz, scimExtension);
        
        log.info("Calling addSchema on an extension: " + scimExtension);
        schemaRegistry.addSchema(Schemas.schemaForExtension(scimExtension));
      }
    }

    schemaRegistry.addResourceType(resourceType);
    repositoryMap.put(clazz, repository);
  }

  @SuppressWarnings("unchecked")
  public <T extends ScimResource> Repository<T> getRepository(Class<T> clazz) {
    return (Repository<T>) repositoryMap.get(clazz);
  }

  private ResourceType generateResourceType(Class<? extends ScimResource> base, Repository<? extends ScimResource> repository) throws InvalidRepositoryException, UnableToRetrieveExtensionsResourceException {

    ScimResourceType scimResourceType = base.getAnnotation(ScimResourceType.class);

    if (scimResourceType == null) {
      throw new InvalidRepositoryException("Missing annotation: ScimResourceType must be at the top of scim resource classes");
    }

    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    List<Class<? extends ScimExtension>> extensionList = repository.getExtensionList();

    if (extensionList != null) {

      List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();

      for (Class<? extends ScimExtension> se : extensionList) {

        ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

        if (extensionType == null) {
          throw new InvalidRepositoryException("Missing annotation: ScimExtensionType must be at the top of scim extension classes");
        }

        ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
        ext.setRequired(extensionType.required());
        ext.setSchemaUrn(extensionType.id());
        extensionSchemaList.add(ext);
      }

      resourceType.setSchemaExtensions(extensionSchemaList);
    }

    return resourceType;
  }
}
