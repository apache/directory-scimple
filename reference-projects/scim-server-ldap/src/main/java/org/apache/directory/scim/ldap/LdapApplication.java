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

package org.apache.directory.scim.ldap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.scim.ldap.ldap.LdapConnectionManager;
import org.apache.directory.scim.ldap.ldap.LdapDao;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.ldap.mapping.AttributeMapper;
import org.apache.directory.scim.ldap.mapping.FilterTranslator;
import org.apache.directory.scim.ldap.service.LdapGroupRepository;
import org.apache.directory.scim.ldap.service.LdapUserRepository;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

import static org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.oauthBearer;

import java.net.URI;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import org.apache.directory.scim.server.rest.ScimResourceHelper;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * JAX-RS {@link Application} that bootstraps a standalone SCIM server backed by LDAP.
 *
 * <p>This application uses Jersey 3 as the JAX-RS runtime, Weld SE for CDI dependency injection,
 * and {@link SeBootstrap} to start an embedded HTTP server. It registers all SCIMple JAX-RS
 * resource and feature classes and produces a {@link ServerConfiguration} bean for the SCIM
 * service provider.</p>
 *
 * @see ScimResourceHelper#scimpleFeatureAndResourceClasses()
 * @see ServerConfiguration
 */
// @ApplicationPath("v2")
// Embedded Jersey + Grizzly ignores the ApplicationPath annotation
// https://github.com/eclipse-ee4j/jersey/issues/3222
@ApplicationScoped
public class LdapApplication extends Application {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LdapApplication.class);

  /**
   * Returns the set of JAX-RS resource and feature classes required by the SCIMple server.
   *
   * <p>Delegates to {@link ScimResourceHelper#scimpleFeatureAndResourceClasses()} to register
   * all SCIM endpoint resources, exception mappers, and JAX-RS features.</p>
   *
   * @return a set of JAX-RS classes to register with the runtime
   */
  @Override
  public Set<Class<?>> getClasses() {
    return ScimResourceHelper.scimpleFeatureAndResourceClasses();
  }

  /**
   * CDI producer method that provides the {@link ServerConfiguration} for the SCIM service.
   *
   * <p>Configures the server with an identifier and documentation URI pointing to the
   * Apache Directory SCIMple project.</p>
   *
   * @return the SCIM server configuration bean
   */
  @Produces
  ServerConfiguration serverConfiguration() {
    // TODO: SCIMple should auto-detect filter support from the Repository implementation
    // rather than requiring manual configuration in ServerConfiguration. This would improve
    // the developer experience across all SCIMple-based servers.
    return new ServerConfiguration()
      .setId("scimple-ldap-example")
      .setDocumentationUri("https://github.com/apache/directory-scimple")
      .setSupportsFilter(true)
      // This does not enforce authentication. Use oauthBearer() or httpBasic() as appropriate.
      .addAuthenticationSchema(oauthBearer());
  }

  /**
   * Starts the standalone SCIM server.
   *
   * <p>The server port defaults to 8080 and can be overridden via the {@code server.port}
   * system property (e.g., {@code -Dserver.port=9090}).</p>
   *
   * <p>Initializes a Weld SE CDI container, starts a JAX-RS {@link SeBootstrap} instance,
   * and blocks the main thread until interrupted (e.g., via {@code CTRL+C}). JUL logging
   * is bridged to SLF4J before startup.</p>
   *
   * <p>Usage: {@code java -jar scim-server-ldap.jar}</p>
   *
   * @param args command-line arguments (currently unused)
   */
  public static void main(String[] args) {

    // configure JUL logging
    SLF4JBridgeHandler.install();

    try {

      // Register CDI beans explicitly instead of using addPackages() — Spring Boot's nested
      // JAR classloader uses URLs that Weld SE's package scanner cannot enumerate.
      SeContainer container = SeContainerInitializer.newInstance()
        .addBeanClasses(
          LdapApplication.class,
          ScimLdapConfig.class,
          LdapConnectionManager.class,
          LdapDao.class,
          AttributeMapper.class,
          FilterTranslator.class,
          LdapUserRepository.class,
          LdapGroupRepository.class
        )
        .initialize();

      int port = Integer.parseInt(System.getProperty("server.port", "8080"));
      LdapApplication app = new LdapApplication();
      SeBootstrap.start(app, SeBootstrap.Configuration.builder().port(port).build())
        .thenAccept(instance -> instance.stopOnShutdown(stopResult -> container.close()));
      URI uri = UriBuilder.fromUri("http://localhost/").port(port).build();

      System.out.printf("Application started: %s%nStop the application using CTRL+C%n", uri.toString());

      // block and wait shut down signal, like CTRL+C
      Thread.currentThread().join();

    } catch (InterruptedException ex) {
      LOG.error("Service Interrupted", ex);
    }
  }
}
