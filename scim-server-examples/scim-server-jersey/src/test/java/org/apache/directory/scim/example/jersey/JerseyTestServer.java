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

package org.apache.directory.scim.example.jersey;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class JerseyTestServer implements EmbeddedServerExtension.ScimTestServer {

  private SeContainer container;
  private SeBootstrap.Instance server;

  @Override
  public URI start(int port) throws Exception {

    // It doesn't look like Weld finds the beans in src/main/java, so enable implicit scanning
    // NOTE: this isn't an issue for the scim-server tests, but those beans are located in src/test/java
    container = SeContainerInitializer.newInstance()
      .addPackages(true, JerseyApplication.class)
      .initialize();

    JerseyApplication app = new JerseyApplication();
    server = SeBootstrap.start(app, SeBootstrap.Configuration.builder().port(port).build())
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
