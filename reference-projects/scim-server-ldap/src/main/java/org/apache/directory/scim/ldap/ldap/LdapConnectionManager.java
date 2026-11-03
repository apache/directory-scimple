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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a pool of authenticated LDAP connections using the Apache Directory LDAP API.
 *
 * <p>This CDI bean is application-scoped: a single {@link LdapConnectionPool} is created at
 * startup from {@link ScimLdapConfig} and shared across all request threads. When
 * {@link ScimLdapConfig#isEmbedded()} is {@code true}, an {@link EmbeddedLdapServer} is
 * started automatically before creating the connection pool.</p>
 *
 * <p>Callers obtain a connection with {@link #getConnection()} and must return it via
 * {@link #releaseConnection(LdapConnection)} in a {@code finally} block.
 */
@ApplicationScoped
public class LdapConnectionManager {

  private static final Logger LOG = LoggerFactory.getLogger(LdapConnectionManager.class);

  private LdapConnectionPool pool;
  private EmbeddedLdapServer embeddedServer;

  @Inject
  ScimLdapConfig properties;

  protected LdapConnectionManager() {}

  @PostConstruct
  void init() {
    String host = properties.getHost();
    int port = properties.getPort();
    String bindDn = properties.getBindDn();

    // Start embedded ApacheDS if configured
    if (properties.isEmbedded()) {
      try {
        embeddedServer = new EmbeddedLdapServer();
        embeddedServer.start(properties);
        host = embeddedServer.getHost();
        port = embeddedServer.getPort();
        bindDn = "uid=admin,ou=system";
      } catch (Exception e) {
        throw new IllegalStateException("Failed to start embedded LDAP server", e);
      }
    }

    try {
      LdapConnectionConfig config = new LdapConnectionConfig();
      config.setLdapHost(host);
      config.setLdapPort(port);
      config.setName(bindDn);
      config.setCredentials(properties.isEmbedded() ? "secret" : properties.getBindPassword());
      // When TLS is enabled, the connection uses the JVM's default trust store (javax.net.ssl.trustStore).
      // Custom trust store configuration is not yet supported — configure via JVM system properties if needed.
      if (properties.isUseTls()) {
        config.setUseTls(true);
      }

      DefaultPoolableLdapConnectionFactory factory =
        new DefaultPoolableLdapConnectionFactory(config);
      pool = new LdapConnectionPool(factory);
      LOG.info("LDAP connection pool initialized for {}:{} as {}", host, port, bindDn);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Failed to initialize LDAP connection pool for " + host + ":" + port + " as " + bindDn, e);
    }
  }

  /**
   * Borrows an authenticated {@link LdapConnection} from the pool.
   *
   * <p>The caller is responsible for returning the connection via
   * {@link #releaseConnection(LdapConnection)} when finished.
   *
   * @return a pooled LDAP connection ready for use
   * @throws LdapException if a connection cannot be obtained from the pool
   */
  public LdapConnection getConnection() throws LdapException {
    return pool.getConnection();
  }

  /**
   * Returns a previously borrowed {@link LdapConnection} to the pool.
   *
   * <p>If the connection is {@code null}, this method is a no-op. Any exception during
   * release is logged at WARN level and swallowed so that it does not mask the original
   * operation's result.
   *
   * @param connection the connection to return, or {@code null}
   */
  public void releaseConnection(LdapConnection connection) {
    if (connection != null) {
      try {
        pool.releaseConnection(connection);
      } catch (LdapException e) {
        LOG.warn("Failed to release LDAP connection", e);
      }
    }
  }

  @PreDestroy
  void close() {
    if (pool != null) {
      try {
        pool.close();
      } catch (Exception e) {
        LOG.warn("Failed to close LDAP connection pool", e);
      }
    }
    if (embeddedServer != null) {
      embeddedServer.stop();
    }
  }
}
