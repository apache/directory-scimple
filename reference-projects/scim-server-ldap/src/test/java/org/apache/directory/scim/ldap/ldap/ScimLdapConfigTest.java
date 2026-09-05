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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Execution(ExecutionMode.SAME_THREAD)
class ScimLdapConfigTest {

  private static final List<String> SYSTEM_PROPERTY_KEYS = List.of(
    "ldap.host", "ldap.port", "ldap.bind.dn", "ldap.bind.password",
    "ldap.base.dn.users", "ldap.base.dn.groups", "ldap.use.tls"
  );

  @AfterEach
  void clearSystemProperties() {
    SYSTEM_PROPERTY_KEYS.forEach(System::clearProperty);
  }

  // Helper: create an instance via the protected no-arg constructor and call init()
  private static ScimLdapConfig createAndInit() throws Exception {
    Constructor<ScimLdapConfig> ctor = ScimLdapConfig.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    ScimLdapConfig config = ctor.newInstance();

    Method init = ScimLdapConfig.class.getDeclaredMethod("init");
    init.setAccessible(true);
    init.invoke(config);
    return config;
  }

  // =========================================================================
  // Group 1: Programmatic constructor
  // =========================================================================

  @Nested
  @DisplayName("Programmatic constructor")
  class ProgrammaticConstructorTest {

    private final ScimLdapConfig config = new ScimLdapConfig(
      "ldap.example.org", 636, "cn=admin,dc=test,dc=com", "p@ss",
      "ou=people,dc=test,dc=com", "ou=roles,dc=test,dc=com", true
    );

    @Test
    void hostIsSet() {
      assertThat(config.getHost()).isEqualTo("ldap.example.org");
    }

    @Test
    void portIsSet() {
      assertThat(config.getPort()).isEqualTo(636);
    }

    @Test
    void bindDnIsSet() {
      assertThat(config.getBindDn()).isEqualTo("cn=admin,dc=test,dc=com");
    }

    @Test
    void bindPasswordIsSet() {
      assertThat(config.getBindPassword()).isEqualTo("p@ss");
    }

    @Test
    void userBaseDnIsSet() {
      assertThat(config.getUserBaseDn()).isEqualTo("ou=people,dc=test,dc=com");
    }

    @Test
    void groupBaseDnIsSet() {
      assertThat(config.getGroupBaseDn()).isEqualTo("ou=roles,dc=test,dc=com");
    }

    @Test
    void useTlsIsSet() {
      assertThat(config.isUseTls()).isTrue();
    }
  }

  // =========================================================================
  // Group 2: init() loading from test classpath YAML
  // =========================================================================

  @Nested
  @DisplayName("init() with test classpath YAML")
  class InitFromYamlTest {

    @Test
    void loadsLdapConnectionSettings() throws Exception {
      ScimLdapConfig config = createAndInit();

      assertThat(config.getHost()).isEqualTo("127.0.0.1");
      assertThat(config.getPort()).isEqualTo(10389);
      assertThat(config.getBindDn()).isEqualTo("uid=admin,ou=system");
      assertThat(config.getBindPassword()).isEqualTo("secret");
      assertThat(config.getUserBaseDn()).isEqualTo("ou=users,dc=example,dc=com");
      assertThat(config.getGroupBaseDn()).isEqualTo("ou=groups,dc=example,dc=com");
      assertThat(config.isUseTls()).isFalse();
    }

    @Test
    void loadsUserMapping() throws Exception {
      ScimLdapConfig config = createAndInit();

      assertThat(config.getUserObjectClasses()).contains("extensibleObject", "inetOrgPerson", "top");
      assertThat(config.getUserRdnAttribute()).isEqualTo("uid");
      assertThat(config.getUserAttributes())
        .containsEntry("userName", "uid")
        .containsEntry("name.givenName", "givenName")
        .containsEntry("emails.value", "mail");
    }

    @Test
    void loadsGroupMapping() throws Exception {
      ScimLdapConfig config = createAndInit();

      assertThat(config.getGroupObjectClasses()).containsExactly("groupOfNames", "top");
      assertThat(config.getGroupRdnAttribute()).isEqualTo("cn");
      assertThat(config.getGroupAttributes())
        .containsEntry("displayName", "cn")
        .containsEntry("members.value", "member");
    }
  }

  // =========================================================================
  // Group 3: System property overrides
  // =========================================================================

  @Nested
  @DisplayName("System property overrides")
  class SystemPropertyOverrideTest {

    @Test
    void systemPropertyOverridesHost() throws Exception {
      System.setProperty("ldap.host", "override.example.com");
      ScimLdapConfig config = createAndInit();
      assertThat(config.getHost()).isEqualTo("override.example.com");
    }

    @Test
    void systemPropertyOverridesPort() throws Exception {
      System.setProperty("ldap.port", "1636");
      ScimLdapConfig config = createAndInit();
      assertThat(config.getPort()).isEqualTo(1636);
    }

    @Test
    void systemPropertyOverridesBindDn() throws Exception {
      System.setProperty("ldap.bind.dn", "cn=override,dc=test,dc=com");
      ScimLdapConfig config = createAndInit();
      assertThat(config.getBindDn()).isEqualTo("cn=override,dc=test,dc=com");
    }

    @Test
    void systemPropertyOverridesUseTls() throws Exception {
      System.setProperty("ldap.use.tls", "true");
      ScimLdapConfig config = createAndInit();
      assertThat(config.isUseTls()).isTrue();
    }

    @Test
    void invalidPortValueThrowsIllegalArgumentException() {
      System.setProperty("ldap.port", "not-a-number");
      assertThatThrownBy(ScimLdapConfigTest::createAndInit)
        .isInstanceOf(InvocationTargetException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);
    }
  }
}
