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

package org.apache.directory.scim.core.spi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.util.stream.Collectors;

@Dependent
public class ScimpleComponents {

  @Produces
  @ApplicationScoped
  public SchemaRegistry schemaRegistry() {
    return new SchemaRegistry();
  }

  @Produces
  @ApplicationScoped
  public RepositoryRegistry repositoryRegistry(SchemaRegistry schemaRegistry, Instance<Repository<? extends ScimResource>> repositoryInstances) {
    return new RepositoryRegistry(schemaRegistry, repositoryInstances.stream().collect(Collectors.toList()));
  }

  /*
   * Eagerly initialize the RepositoryRegistry bean on startup.
   */
  public void startup(@Observes Startup startup, RepositoryRegistry repositoryRegistry) {
    repositoryRegistry.toString(); // call toString() to resolve real object from proxy
  }
}
