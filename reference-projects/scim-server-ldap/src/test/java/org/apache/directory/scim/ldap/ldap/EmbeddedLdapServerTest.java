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

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedLdapServer}.
 */
class EmbeddedLdapServerTest {

  @Test
  void startsAndSeedsBaseEntriesAndSampleData() throws Exception {
    ScimLdapConfig config = new ScimLdapConfig(
      "127.0.0.1", 389, "uid=admin,ou=system", "secret",
      "ou=users,dc=example,dc=com", "ou=groups,dc=example,dc=com", false);
    config.init();

    EmbeddedLdapServer server = new EmbeddedLdapServer();
    try {
      server.start(config);

      assertThat(server.getHost()).isEqualTo("127.0.0.1");
      assertThat(server.getPort()).isGreaterThan(0);

      // Connect and verify base entries and sample data exist
      try (LdapConnection conn = new LdapNetworkConnection(server.getHost(), server.getPort())) {
        conn.bind("uid=admin,ou=system", "secret");

        // Base entries
        assertThat(conn.exists("dc=example,dc=com")).isTrue();
        assertThat(conn.exists("ou=users,dc=example,dc=com")).isTrue();
        assertThat(conn.exists("ou=groups,dc=example,dc=com")).isTrue();

        // Sample users
        assertThat(conn.exists("uid=bjensen,ou=users,dc=example,dc=com")).isTrue();
        assertThat(conn.exists("uid=jsmith,ou=users,dc=example,dc=com")).isTrue();
        assertThat(conn.exists("uid=awhite,ou=users,dc=example,dc=com")).isTrue();

        // Sample group
        assertThat(conn.exists("cn=Engineering,ou=groups,dc=example,dc=com")).isTrue();

        conn.unBind();
      }
    } finally {
      server.stop();
    }
  }

  @Test
  void stopIsIdempotent() {
    EmbeddedLdapServer server = new EmbeddedLdapServer();
    // Calling stop without start should not throw
    server.stop();
    server.stop();
  }
}
