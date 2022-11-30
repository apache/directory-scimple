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

package org.apache.directory.scim.example.memory.rest;

import jakarta.enterprise.inject.Produces;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.apache.directory.scim.server.rest.ScimpleFeature;

import static org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.httpBasic;

@ApplicationPath("v2")
public class RestApplication extends Application {
  
  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(ScimpleFeature.class);
  }

  @Produces
  ServerConfiguration serverConfiguration() {
    return new ServerConfiguration()
      .setId("scimple-in-memory-example")
      .addAuthenticationSchema(httpBasic());

    // set the auth scheme too
    // .addAuthenticationSchema(oauthBearer());
  }
}
