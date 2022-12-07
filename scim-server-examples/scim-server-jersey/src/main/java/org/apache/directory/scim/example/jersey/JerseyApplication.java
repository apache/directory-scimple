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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

import java.net.URI;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import org.apache.directory.scim.server.rest.ScimpleFeature;
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
    return Set.of(ScimpleFeature.class);
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

      SeContainer container = SeContainerInitializer.newInstance()
        .addPackages(true, JerseyApplication.class)
        .initialize();

      JerseyApplication app = new JerseyApplication();
      SeBootstrap.start(app, SeBootstrap.Configuration.builder().port(8080).build())
        .thenAccept(instance -> instance.stopOnShutdown(stopResult -> container.close()));
      URI uri = UriBuilder.fromUri("http://localhost/").port(8080).build();

      System.out.printf("Application started: %s\nStop the application using CTRL+C%n", uri.toString());

      // block and wait shut down signal, like CTRL+C
      Thread.currentThread().join();

    } catch (InterruptedException ex) {
      LOG.error("Service Interrupted", ex);
    }
  }
}
