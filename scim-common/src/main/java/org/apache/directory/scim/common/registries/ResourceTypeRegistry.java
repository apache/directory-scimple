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

package org.apache.directory.scim.common.registries;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.scim.spec.schema.ResourceType;

/**
 * Provides a registry for ResourceTypes, whether defined in the schema
 * specification or from implementors new ResourceTypes. In most applications,
 * this class should be instantiated as a singleton at application start.
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
public class ResourceTypeRegistry {

  Map<String, ResourceType> registry;

  public ResourceTypeRegistry() {
    registry = new ConcurrentHashMap<>();
    // TODO - default ResourceTypes from scim-spec-schema
  }

  /**
   * Adds a ResourceType to the registry.
   * 
   * @param resourceType
   *          the ResourceType to register.
   */
  public void add(ResourceType resourceType) {
    String key = resourceType.getId();
    if (key == null) {
      key = resourceType.getName();
    }
    registry.put(key, resourceType);
  }

  /**
   * Adds a Set of ResourceType objects to the registry.
   * 
   * @param resourceTypeSet
   *          the ResourceType objects to register.
   */
  public void addAll(Set<ResourceType> resourceTypeSet) {
    resourceTypeSet.forEach(r -> add(r));
  }

}
