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

package org.apache.directory.scim.core.repository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.core.schema.SchemaRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class RepositoryRegistry {

  private SchemaRegistry schemaRegistry;

  private Map<Class<? extends ScimResource>, Repository<? extends ScimResource>> repositoryMap = new HashMap<>();

  public RepositoryRegistry() {
    // CDI
  }

  public RepositoryRegistry(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public RepositoryRegistry(SchemaRegistry schemaRegistry, List<Repository<? extends ScimResource>> scimRepositories) {
    this.schemaRegistry = schemaRegistry;
    scimRepositories.stream()
      .map(repository -> (Repository<ScimResource>) repository)
      .forEach(repository -> {
      try {
        registerRepository(repository.getResourceClass(), repository);
      } catch (InvalidRepositoryException e) {
        throw new ScimResourceInvalidException("Failed to register repository " + repository.getClass() + " for ScimResource type " + repository.getResourceClass(), e);
      }
    });
  }

  public synchronized <T extends ScimResource> void registerRepository(Class<T> clazz, Repository<T> repository) throws InvalidRepositoryException {
    List<Class<? extends ScimExtension>> extensionList = repository.getExtensionList();

    log.debug("Calling addSchema on the base class: {}", clazz);
    schemaRegistry.addSchema(clazz, extensionList);
    repositoryMap.put(clazz, repository);
  }

  @SuppressWarnings("unchecked")
  public <T extends ScimResource> Repository<T> getRepository(Class<T> clazz) {
    return (Repository<T>) repositoryMap.get(clazz);
  }
}
