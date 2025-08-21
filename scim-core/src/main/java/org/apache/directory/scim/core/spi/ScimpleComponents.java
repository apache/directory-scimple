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

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.util.stream.Collectors;

@Dependent
public class ScimpleComponents {

  public ScimpleComponents() {
    // CDI
    // See https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#what_classes_are_beans
  }

  @Produces
  @ApplicationScoped
  public SchemaRegistry schemaRegistry() {
    return new SchemaRegistry();
  }

  @Produces
  @ApplicationScoped
  public RepositoryRegistry repositoryRegistry(SchemaRegistry schemaRegistry, Instance<Repository<? extends ScimResource>> repositoryInstances) {
    RepositoryRegistry registry = new RepositoryRegistry(schemaRegistry);
    registry.registerRepositories(repositoryInstances.stream().collect(Collectors.toList()));
    return registry;
  }

  /*
   * Eagerly initialize the RepositoryRegistry bean on startup.
   */
  public void startup(@Observes ContainerInitialized startup, RepositoryRegistry repositoryRegistry) {
    repositoryRegistry.toString(); // call toString() to resolve real object from proxy
  }
}
