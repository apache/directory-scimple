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

package org.apache.directory.scim.example.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ApplicationPath;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

import java.util.Set;

import jakarta.ws.rs.core.Application;
import org.apache.directory.scim.server.rest.ScimResourceHelper;

import static org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.oauthBearer;

@ApplicationPath("v2")
@ApplicationScoped
public class QuarkusApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return ScimResourceHelper.scimpleFeatureAndResourceClasses();
  }

  @Produces
  ServerConfiguration serverConfiguration() {
    return new ServerConfiguration()
      // Set any unique configuration bits
      .setId("scimple-quarkus-example")
      .setDocumentationUri("https://github.com/apache/directory-scimple")
      // set the auth scheme too
     .addAuthenticationSchema(oauthBearer());
  }

}
