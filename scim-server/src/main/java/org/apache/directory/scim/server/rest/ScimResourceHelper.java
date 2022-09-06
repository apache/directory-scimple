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

package org.apache.directory.scim.server.rest;

import java.util.HashSet;
import java.util.Set;

import org.apache.directory.scim.server.exception.FilterParseExceptionMapper;

/**
 * Provides the SCIM defined set of end-points and resources without declaring a
 * JAX-RS application. Additional end-points and extensions can be added by the
 * implementing class.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
public final class ScimResourceHelper {

  private ScimResourceHelper() {
    // Make this a utility class
  }

  /**
   * Provides a set of JAX-RS annotated classes for the basic SCIM protocol
   * functionality.
   * 
   * @return the JAX-RS annotated classes.
   */
  public static Set<Class<?>> getScimClassesToLoad() {

    // Required scim classes.
    return Set.of(
      BulkResourceImpl.class,
      GroupResourceImpl.class,
      ResourceTypesResourceImpl.class,
      SchemaResourceImpl.class,
      SearchResourceImpl.class,
      SelfResourceImpl.class,
      ServiceProviderConfigResourceImpl.class,
      UserResourceImpl.class,
      FilterParseExceptionMapper.class,
      WebApplicationExceptionMapper.class,

    // handle MediaType of application/scim+json
    ScimJacksonXmlBindJsonProvider.class);
  }
}
