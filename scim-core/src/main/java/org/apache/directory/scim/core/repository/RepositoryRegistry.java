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

import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.directory.scim.core.schema.SchemaRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryRegistry {
    /** A logger for this class */
    private static final Logger log = LoggerFactory.getLogger(RepositoryRegistry.class);
    
  private SchemaRegistry schemaRegistry;

  private Map<Class<? extends ScimResource>, Repository<? extends ScimResource>> repositoryMap = new HashMap<>();

  public RepositoryRegistry() {
    // CDI
  }

  public RepositoryRegistry(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public void registerRepositories(List<Repository<? extends ScimResource>> scimRepositories) {
    scimRepositories.stream()
      .map(repository -> (Repository<? extends ScimResource>) repository)
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

  public SchemaRegistry getSchemaRegistry() {
    return this.schemaRegistry;
  }

  public RepositoryRegistry setSchemaRegistry(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
    return this;
  }

  public Map<Class<? extends ScimResource>, Repository<? extends ScimResource>> getRepositoryMap() {
    return this.repositoryMap;
  }

  public RepositoryRegistry setRepositoryMap(Map<Class<? extends ScimResource>, Repository<? extends ScimResource>> repositoryMap) {
    this.repositoryMap = repositoryMap;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RepositoryRegistry other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$schemaRegistry = this.getSchemaRegistry();
    final Object other$schemaRegistry = other.getSchemaRegistry();
    if (this$schemaRegistry == null ? other$schemaRegistry != null : !this$schemaRegistry.equals(other$schemaRegistry))
      return false;
    final Object this$repositoryMap = this.getRepositoryMap();
    final Object other$repositoryMap = other.getRepositoryMap();
    if (this$repositoryMap == null ? other$repositoryMap != null : !this$repositoryMap.equals(other$repositoryMap))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RepositoryRegistry;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $schemaRegistry = this.getSchemaRegistry();
    result = result * PRIME + ($schemaRegistry == null ? 43 : $schemaRegistry.hashCode());
    final Object $repositoryMap = this.getRepositoryMap();
    result = result * PRIME + ($repositoryMap == null ? 43 : $repositoryMap.hashCode());
    return result;
  }

  public String toString() {
    return "RepositoryRegistry(schemaRegistry=" + this.getSchemaRegistry() + ", repositoryMap=" + this.getRepositoryMap() + ")";
  }
}
