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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import javax.ws.rs.core.UriBuilder;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.Application;
import org.apache.directory.scim.server.rest.ScimResourceHelper;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.oauthBearer;

// @ApplicationPath("v2")
// Embedded Jersey + Jetty ignores the ApplicationPath annotation
// https://github.com/eclipse-ee4j/jersey/issues/3222
@ApplicationScoped
public class JerseyApplication extends Application {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JerseyApplication.class);
  
  @Override
  public Set<Class<?>> getClasses() {
    return ScimResourceHelper.scimpleFeatureAndResourceClasses();
  }

  @Produces
  ServerConfiguration serverConfiguration() {
    return new ServerConfiguration()
      // Set any unique configuration bits
      .setId("scimple-jersey-example")
      .setDocumentationUri("https://github.com/apache/directory-scimple")
    // set the auth scheme too
     .addAuthenticationSchema(oauthBearer());
  }

  public static void main(String[] args) {

    // configure JUL logging
    SLF4JBridgeHandler.install();

    try {
      JerseyApplication app = new JerseyApplication();
      HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/")); // shut
      GrizzlyHttpContainerProvider provider = new GrizzlyHttpContainerProvider();
      HttpHandler container = provider.createContainer(HttpHandler.class, app);
      server.getServerConfiguration().addHttpHandler(container);
      URI uri = UriBuilder.fromUri("http://localhost/").port(8080).build();

      System.out.printf("Application started: %s%nStop the application using CTRL+C%n", uri.toString());

      // block and wait shut down signal, like CTRL+C
      Thread.currentThread().join();

    } catch (InterruptedException ex) {
      LOG.error("Service Interrupted", ex);
    }
  }
}
