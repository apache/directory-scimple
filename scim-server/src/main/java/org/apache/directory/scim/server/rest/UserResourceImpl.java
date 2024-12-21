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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.protocol.UserResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.core.schema.SchemaRegistry;

/**
 * @author shawn
 *
 */
@Slf4j
@ApplicationScoped
public class UserResourceImpl extends BaseResourceTypeResourceImpl<ScimUser> implements UserResource {

  @Inject
  public UserResourceImpl(SchemaRegistry schemaRegistry, RepositoryRegistry repositoryRegistry) {
    super(schemaRegistry, repositoryRegistry, ScimUser.class);
  }

  public UserResourceImpl() {
    // CDI
    this(null, null);
  }
}
