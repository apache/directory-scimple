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

package org.apache.directory.scim.server.it;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.apache.directory.scim.server.it.testapp.App;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.internal.RuntimeDelegateImpl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class JerseyTestServer implements EmbeddedServerExtension.ScimTestServer {

  private SeBootstrap.Instance server;

  private SeContainer container;

  @Override
  public URI start(int port) throws Exception {
    container = SeContainerInitializer.newInstance()
      .addPackages(true, App.class.getPackage())
      .initialize();

    // There are multiple JAX-RS implementations on the classpath, Jersey for the server and RestEasy for testing
    // explicitly use Jersey so the test implementation is not use to start the server
     server = new RuntimeDelegateImpl().bootstrap(new App(), JerseySeBootstrapConfiguration.builder().port(port).build())
      .toCompletableFuture().get(1, TimeUnit.MINUTES);

    // shut down CDI container on stop
    server.stopOnShutdown(stopResult -> container.close());

    return UriBuilder.fromUri("http://localhost/").port(port).build();
  }

  @Override
  public void shutdown() throws Exception {
    if (server != null) {
      server.stop().toCompletableFuture()
        .get(10, TimeUnit.SECONDS);
    }

    if (container != null) {
      container.close();
    }
  }
}
