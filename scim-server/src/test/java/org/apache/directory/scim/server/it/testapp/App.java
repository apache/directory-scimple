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

package org.apache.directory.scim.server.it.testapp;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Application;
import org.apache.directory.scim.server.ScimConfiguration;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.rest.ScimResourceHelper;

import java.util.HashSet;
import java.util.Set;

import static org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.httpBasic;

public class App extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> clazzes = new HashSet<>(ScimResourceHelper.getScimClassesToLoad());
    clazzes.add(ServerConfigInitializer.class);
    return clazzes;
  }

  /**
   * A {@link ScimConfiguration} allow for eager initialization of beans, this class configures the {@link ServerConfiguration}.
   */
  public static class ServerConfigInitializer implements ScimConfiguration {

    @Inject
    private ServerConfiguration serverConfiguration;

    @Override
    public void configure() {

      // Set any unique configuration bits
      serverConfiguration
        .setId("scimple-server-its")
        .addAuthenticationSchema(httpBasic());
    }
  }
}
