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

package org.apache.directory.scim.ldap.ldap;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Starts an embedded Apache Directory Server for development and demonstration purposes.
 *
 * <p>This is a plain helper class (not a CDI bean) instantiated by {@link LdapConnectionManager}
 * when {@link ScimLdapConfig#isEmbedded()} is {@code true}. It creates an in-memory LDAP
 * directory with the base OUs needed for SCIM user and group operations, and seeds it with
 * sample users and a group so the server returns useful data immediately.</p>
 *
 * <p>The embedded server always listens on a random available port on {@code 127.0.0.1}.
 * After calling {@link #start(ScimLdapConfig)}, use {@link #getHost()} and
 * {@link #getPort()} to obtain the connection details.</p>
 *
 * @see ScimLdapConfig#isEmbedded()
 */
public class EmbeddedLdapServer {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedLdapServer.class);
  private DirectoryService directoryService;
  private LdapServer ldapServer;
  private String host;
  private int port;

  /**
   * Starts the embedded Apache DS instance and seeds the base directory structure.
   *
   * <p>Creates an in-memory directory with a partition rooted at the base DN derived from
   * {@link ScimLdapConfig#getUserBaseDn()}, then seeds it with organizational units, sample
   * users, and a group. The server binds to {@code 127.0.0.1} on a randomly selected
   * available port.</p>
   *
   * @param config the SCIM-LDAP configuration providing base DNs for users and groups
   * @throws Exception if the directory service or LDAP server fails to start
   */
  public void start(ScimLdapConfig config) throws Exception {
    // Initialize directory service
    DefaultDirectoryServiceFactory factory = new DefaultDirectoryServiceFactory();
    factory.init("scimple-embedded");
    directoryService = factory.getDirectoryService();

    // Derive the base DN from the user base DN (e.g. ou=users,dc=example,dc=com -> dc=example,dc=com)
    String userBaseDn = config.getUserBaseDn();
    String baseDn = userBaseDn.substring(userBaseDn.indexOf(',') + 1).trim();

    // Add partition for the base DN
    AvlPartition partition = new AvlPartition(directoryService.getSchemaManager());
    partition.setId("scimple");
    partition.setSuffixDn(new Dn(baseDn));
    directoryService.addPartition(partition);

    // Always bind to an ephemeral port — the config port is for external LDAP connections
    try (ServerSocketChannel ch = ServerSocketChannel.open()) {
      ch.bind(new InetSocketAddress(0));
      port = ch.socket().getLocalPort();
    }
    host = InetAddress.getLoopbackAddress().getHostAddress();

    // Start LDAP protocol server
    ldapServer = new LdapServer();
    ldapServer.setDirectoryService(directoryService);
    ldapServer.setTransports(new TcpTransport(host, port));
    ldapServer.start();

    // Seed base directory structure and sample data
    seedBaseEntries(config);
    seedSampleData(config);

    LOG.info("Embedded ApacheDS started on {}:{}", host, port);
  }

  /**
   * Seeds the base directory structure: domain entry, user OU, and group OU.
   *
   * <p>Subclasses may override to customize the base directory structure. The default
   * implementation creates the domain entry and organizational units derived from
   * {@link ScimLdapConfig#getUserBaseDn()} and {@link ScimLdapConfig#getGroupBaseDn()}.</p>
   *
   * @param config the SCIM-LDAP configuration providing base DNs
   * @throws Exception if entries cannot be created
   */
  protected void seedBaseEntries(ScimLdapConfig config)
    throws Exception {
    SchemaManager schemaManager = directoryService.getSchemaManager();
    String userBaseDn = config.getUserBaseDn();
    String baseDn = userBaseDn.substring(userBaseDn.indexOf(',') + 1).trim();
    var session = directoryService.getAdminSession();

    // Extract the domain component for the base entry (e.g. dc=example from dc=example,dc=com)
    String dcValue = baseDn.split(",")[0].split("=")[1];

    // Create base domain entry
    session.add(new DefaultEntry(schemaManager,
      baseDn,
      "objectClass: domain",
      "dc: " + dcValue));

    // Create user OU
    String userOu = userBaseDn.split(",")[0].split("=")[1];
    session.add(new DefaultEntry(schemaManager,
      userBaseDn,
      "objectClass: organizationalUnit",
      "ou: " + userOu));

    // Create group OU
    String groupBaseDn = config.getGroupBaseDn();
    String groupOu = groupBaseDn.split(",")[0].split("=")[1];
    session.add(new DefaultEntry(schemaManager,
      groupBaseDn,
      "objectClass: organizationalUnit",
      "ou: " + groupOu));

    LOG.debug("Seeded base entries: {}, {}, {}", baseDn, userBaseDn, groupBaseDn);
  }

  /**
   * Seeds sample data into the directory for demonstration purposes.
   *
   * <p>Subclasses may override to provide custom seed data (e.g. test-specific entries)
   * or to no-op if no sample data is desired.</p>
   *
   * @param config the SCIM-LDAP configuration providing base DNs
   * @throws Exception if entries cannot be created
   */
  protected void seedSampleData(ScimLdapConfig config)
    throws Exception {
    SchemaManager schemaManager = directoryService.getSchemaManager();
    var session = directoryService.getAdminSession();
    String userBaseDn = config.getUserBaseDn();
    String groupBaseDn = config.getGroupBaseDn();

    // Sample users
    session.add(new DefaultEntry(schemaManager,
      "uid=bjensen," + userBaseDn,
      "objectClass: inetOrgPerson",
      "objectClass: organizationalPerson",
      "objectClass: person",
      "objectClass: top",
      "uid: bjensen",
      "cn: Barbara Jensen",
      "sn: Jensen",
      "givenName: Barbara",
      "displayName: Barbara Jensen",
      "mail: bjensen@example.com",
      "title: Vice President"));

    session.add(new DefaultEntry(schemaManager,
      "uid=jsmith," + userBaseDn,
      "objectClass: inetOrgPerson",
      "objectClass: organizationalPerson",
      "objectClass: person",
      "objectClass: top",
      "uid: jsmith",
      "cn: John Smith",
      "sn: Smith",
      "givenName: John",
      "displayName: John Smith",
      "mail: jsmith@example.com",
      "title: Engineer"));

    session.add(new DefaultEntry(schemaManager,
      "uid=awhite," + userBaseDn,
      "objectClass: inetOrgPerson",
      "objectClass: organizationalPerson",
      "objectClass: person",
      "objectClass: top",
      "uid: awhite",
      "cn: Alice White",
      "sn: White",
      "givenName: Alice",
      "displayName: Alice White",
      "mail: awhite@example.com",
      "title: Manager"));

    // Sample group with all users as members
    session.add(new DefaultEntry(schemaManager,
      "cn=Engineering," + groupBaseDn,
      "objectClass: groupOfNames",
      "objectClass: top",
      "cn: Engineering",
      "member: uid=bjensen," + userBaseDn,
      "member: uid=jsmith," + userBaseDn,
      "member: uid=awhite," + userBaseDn));

    LOG.debug("Seeded sample users and groups");
  }

  /**
   * Stops the embedded LDAP server and directory service.
   *
   * <p>Stops components in reverse order: LDAP protocol server first, then the
   * underlying directory service. Safe to call even if the server was never started.</p>
   */
  public void stop() {
    if (ldapServer != null) {
      ldapServer.stop();
    }
    if (directoryService != null) {
      try {
        directoryService.shutdown();
      } catch (Exception e) {
        LOG.warn("Failed to shut down embedded directory service", e);
      }
    }
    LOG.info("Embedded ApacheDS stopped");
  }

  /**
   * Returns the underlying directory service, for subclasses that need to register custom
   * schema attributes or add entries outside the standard seed methods.
   *
   * @return the directory service, or {@code null} if the server has not been started
   */
  protected DirectoryService getDirectoryService() { return directoryService; }

  /** Returns the host the embedded server is listening on. */
  public String getHost() { return host; }

  /** Returns the port the embedded server is listening on. */
  public int getPort() { return port; }
}
