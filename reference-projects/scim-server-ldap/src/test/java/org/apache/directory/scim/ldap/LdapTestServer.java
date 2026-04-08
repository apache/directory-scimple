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

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.apache.directory.scim.ldap.ldap.EmbeddedLdapServer;
import org.apache.directory.scim.ldap.ldap.LdapConnectionManager;
import org.apache.directory.scim.ldap.ldap.LdapDao;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.ldap.mapping.AttributeMapper;
import org.apache.directory.scim.ldap.mapping.FilterTranslator;
import org.apache.directory.scim.ldap.service.LdapGroupRepository;
import org.apache.directory.scim.ldap.service.LdapUserRepository;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Compliance test harness that starts an embedded Apache DS LDAP server and a SCIM server
 * for running the SCIMple compliance test suite. Registered via {@code META-INF/services} SPI
 * as an {@link EmbeddedServerExtension.ScimTestServer} implementation.
 *
 * <p>Extends {@link EmbeddedLdapServer} and overrides {@link #seedSampleData(ScimLdapConfig)}
 * to register custom schema attributes and seed test-specific entries instead of the default
 * demo data.</p>
 */
public class LdapTestServer extends EmbeddedLdapServer
    implements EmbeddedServerExtension.ScimTestServer {

  private SeContainer container;
  private SeBootstrap.Instance server;

  /**
   * CDI {@link Alternative} that overrides {@link ScimLdapConfig} to provide the dynamic LDAP
   * port assigned to the embedded test server. All other configuration values are inherited
   * from the test {@code scim-ldap.yml} resource.
   */
  @Alternative
  @Priority(1)
  @ApplicationScoped
  public static class TestScimLdapConfig extends ScimLdapConfig {
    private static volatile int ldapPort;

    /**
     * Sets the LDAP port that {@link #getPort()} will return.
     *
     * @param port the port the embedded LDAP server is listening on
     */
    static void configure(int port) {
      ldapPort = port;
    }

    /**
     * Returns the dynamic LDAP port for the embedded test server.
     *
     * @return the port the embedded LDAP server is listening on
     */
    @Override
    public int getPort() { return ldapPort; }
  }

  /**
   * Seeds test-specific entries instead of the default sample data.
   *
   * <p>Registers custom schema attributes ({@code scimActive}, {@code scimPhoneTypes}) and
   * adds a test user and group used by the compliance test suite.</p>
   *
   * @param config the SCIM-LDAP configuration providing base DNs
   * @throws Exception if entries cannot be created
   */
  @Override
  protected void seedSampleData(ScimLdapConfig config) throws Exception {
    SchemaManager schemaManager = getDirectoryService().getSchemaManager();

    // Register custom attribute types used by AttributeMapper
    registerCustomAttribute(schemaManager, "1.3.6.1.4.1.99999.1.1", "scimActive");
    registerCustomAttribute(schemaManager, "1.3.6.1.4.1.99999.1.2", "scimPhoneTypes");

    var session = getDirectoryService().getAdminSession();
    String userBaseDn = config.getUserBaseDn();
    String groupBaseDn = config.getGroupBaseDn();

    // Add a default test user
    session.add(new DefaultEntry(schemaManager,
      "uid=testuser," + userBaseDn,
      "objectClass: inetOrgPerson",
      "objectClass: organizationalPerson",
      "objectClass: person",
      "objectClass: top",
      "uid: testuser",
      "cn: Test McTest",
      "sn: McTest",
      "givenName: Test",
      "displayName: Test McTest",
      "mail: test@example.com"));

    // Add a default test group
    session.add(new DefaultEntry(schemaManager,
      "cn=example-group," + groupBaseDn,
      "objectClass: groupOfNames",
      "objectClass: top",
      "cn: example-group",
      "member: uid=testuser," + userBaseDn));
  }

  /**
   * Starts an embedded Apache DS instance, seeds it with test data (users and groups),
   * then starts a CDI container and JAX-RS SCIM server on the given port.
   *
   * @param port the HTTP port the SCIM server should bind to
   * @return the base URI of the running SCIM server
   * @throws Exception if any server fails to start or test data cannot be loaded
   */
  @Override
  public URI start(int port) throws Exception {

    // Start embedded LDAP server with test-specific seed data
    ScimLdapConfig ldapConfig = new ScimLdapConfig(
      "127.0.0.1", 0, "uid=admin,ou=system", "secret",
      "ou=users,dc=example,dc=com", "ou=groups,dc=example,dc=com", false);
    super.start(ldapConfig);

    // Configure test LDAP properties via CDI alternative (no system properties)
    TestScimLdapConfig.configure(getPort());

    // Start CDI container and SCIM server
    // Register CDI beans explicitly instead of using addPackages() — Spring Boot's nested
    // JAR classloader uses URLs that Weld SE's package scanner cannot enumerate.
    container = SeContainerInitializer.newInstance()
      .addBeanClasses(
        LdapApplication.class,
        ScimLdapConfig.class,
        LdapConnectionManager.class,
        LdapDao.class,
        AttributeMapper.class,
        FilterTranslator.class,
        LdapUserRepository.class,
        LdapGroupRepository.class,
        TestScimLdapConfig.class
      )
      .initialize();

    LdapApplication app = new LdapApplication();
    server = SeBootstrap.start(app, SeBootstrap.Configuration.builder().port(port).build())
      .toCompletableFuture().get(1, TimeUnit.MINUTES);

    server.stopOnShutdown(stopResult -> container.close());

    return UriBuilder.fromUri("http://localhost/").port(port).build();
  }

  /**
   * Stops the SCIM server, CDI container, and embedded LDAP server, in that order.
   * Each component is stopped only if it was previously started.
   *
   * @throws Exception if any server fails to stop cleanly
   */
  @Override
  public void shutdown() throws Exception {
    if (server != null) {
      server.stop().toCompletableFuture()
        .get(10, TimeUnit.SECONDS);
    }

    if (container != null) {
      container.close();
    }

    super.stop();
  }

  private static void registerCustomAttribute(SchemaManager schemaManager, String oid, String name)
    throws Exception {
    AttributeType attrType = new AttributeType(oid);
    attrType.setNames(name);
    attrType.setSyntaxOid("1.3.6.1.4.1.1466.115.121.1.15"); // Directory String
    attrType.setEqualityOid("2.5.13.2"); // caseIgnoreMatch
    attrType.setSingleValued(true);
    schemaManager.add(attrType);
  }
}
