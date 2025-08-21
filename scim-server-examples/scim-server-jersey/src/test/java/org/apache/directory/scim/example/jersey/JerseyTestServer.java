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

import javax.ws.rs.core.UriBuilder;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.environment.se.Weld;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class JerseyTestServer implements EmbeddedServerExtension.ScimTestServer {

  private HttpServer server;

  private HttpHandler container;

  private Weld weld;

  @Override
  public URI start(int port) throws Exception {
    // scan packages
    weld = new Weld();
    weld.scanClasspathEntries();
    weld.initialize();
    final ResourceConfig config = new ResourceConfig();
    JerseyApplication app = new JerseyApplication();

    GrizzlyHttpContainerProvider provider = new GrizzlyHttpContainerProvider();
    container = provider.createContainer(HttpHandler.class, app);

    // There are multiple JAX-RS implementations on the classpath, Jersey for the server and RestEasy for testing
    // explicitly use Jersey so the test implementation is not use to start the server
    server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:" + port +"/"), config);
    server.getServerConfiguration().addHttpHandler(container);
    server.start();

    return UriBuilder.fromUri("http://localhost/").port(port).build();
  }

  @Override
  public void shutdown() throws Exception {
    if (server != null) {
      server.shutdownNow();
    }
    if (container != null) {
      container.destroy();
    }
    weld.shutdown();
  }
}
