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

package org.apache.directory.scim.spec.extension;

import java.util.HashMap;
import java.util.Map;

import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.exception.InvalidExtensionException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScimExtensionRegistry {
    
  private static final ScimExtensionRegistry INSTANCE = new ScimExtensionRegistry();
  
  private Map<Class<? extends ScimResource>, Map<String, Class<? extends ScimExtension>>> registry = new HashMap<>();
  
  private ScimExtensionRegistry() {}
  
  public Class<? extends ScimExtension> getExtensionClass(Class<? extends ScimResource> resourceClass, String urn) {
    Class<? extends ScimExtension> extensionClass = null;
    if(registry.containsKey(resourceClass)) {
      Map<String, Class<? extends ScimExtension>> resourceMap = registry.get(resourceClass);
      if(resourceMap.containsKey(urn)) {
        extensionClass = resourceMap.get(urn);
      }
    }
    return extensionClass;
  }
  
  public static ScimExtensionRegistry getInstance() {
    return INSTANCE;
  }
  
  public void registerExtension(Class<? extends ScimResource> resourceClass, Class<? extends ScimExtension> extensionClass) {
    ScimExtensionType[] se = extensionClass.getAnnotationsByType(ScimExtensionType.class);

    if (se.length != 1) {
      throw new InvalidExtensionException("Registered extensions must a single @ScimExtensionType annotation");
    }
    
    String urn = se[0].id();
    
    log.debug("Registering extension for URN: '{}' associated resource class: '{}' and extension class: '{}'",
      urn, resourceClass.getSimpleName(), extensionClass.getSimpleName() );

    Map<String, Class<? extends ScimExtension>> resourceMap = registry.computeIfAbsent(resourceClass, k -> new HashMap<>());

    if(!resourceMap.containsKey(urn)) {
      resourceMap.put(urn, extensionClass);
    }
  }

}
