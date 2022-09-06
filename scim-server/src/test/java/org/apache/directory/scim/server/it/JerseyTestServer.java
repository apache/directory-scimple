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

import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.environment.se.Weld;

import org.apache.directory.scim.server.it.testapp.App;

import java.net.URI;

public class JerseyTestServer implements EmbeddedServerExtension.ScimTestServer {

  private HttpServer server;
  private Weld weld;

  @Override
  public URI start(int port) {
    weld = new Weld();
    // ensure Weld discovers the beans in this project
    weld.addPackages(true, App.class.getPackage());
    weld.initialize();

    ResourceConfig rc = ResourceConfig.forApplication(new App());
    URI uri = UriBuilder.fromUri("http://localhost/").port(port).build();
    server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);

    return uri;
  }

  @Override
  public void shutdown() {
    if (server != null) {
      server.shutdown();
    }
    if (weld != null) {
      weld.shutdown();
    }
  }
}
