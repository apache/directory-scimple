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

import org.apache.directory.scim.server.rest.ScimResourceHelper;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.jboss.weld.environment.se.Weld;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

// @ApplicationPath("v2")
// Embedded Jersey + Jetty ignores the ApplicationPath annotation
// https://github.com/eclipse-ee4j/jersey/issues/3222
public class JerseyApplication extends Application {

  private static final String BASE_URI = "http://localhost:8080/";

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JerseyApplication.class);
  
  @Override
  public Set<Class<?>> getClasses() {
    return new HashSet<>(ScimResourceHelper.getScimClassesToLoad());
  }

  public static void main(String[] args) {

    // configure JUL logging
    SLF4JBridgeHandler.install();

    try {
      Weld weld = new Weld();
      // ensure Weld discovers the beans in this project
      weld.addPackages(true, JerseyApplication.class.getPackage());
      weld.initialize();

      ApplicationHandler applicationHandler = new ApplicationHandler(JerseyApplication.class);
      final Server server = JettyHttpContainerFactory.createServer(URI.create(BASE_URI), applicationHandler.getConfiguration());

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          System.out.println("Shutting down the application...");
          server.stop();
          weld.shutdown();
          System.out.println("Done, exit.");
        } catch (Exception e) {
          LOG.error("Failed to shutdown service", e);
        }
      }));

      System.out.printf("Application started: %s\nStop the application using CTRL+C%n", BASE_URI);

      // block and wait shut down signal, like CTRL+C
      Thread.currentThread().join();

    } catch (InterruptedException ex) {
      LOG.error("Service Interrupted", ex);
    }
  }
}
