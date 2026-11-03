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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central configuration for the SCIM-to-LDAP bridge, loaded from {@code scim-ldap.yml} on the
 * classpath. Holds LDAP connection settings (host, port, bind DN, TLS) and SCIM-to-LDAP
 * attribute mappings for users and groups.
 *
 * <p>LDAP connection settings can be overridden at runtime via system properties such as
 * {@code ldap.host}, {@code ldap.port}, {@code ldap.bind.dn}, {@code ldap.bind.password},
 * {@code ldap.base.dn.users}, {@code ldap.base.dn.groups}, and {@code ldap.use.tls}.
 * System properties take precedence over YAML values, which take precedence over built-in defaults.
 */
@ApplicationScoped
public class ScimLdapConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ScimLdapConfig.class);
  private static final String CONFIG_FILE = "scim-ldap.yml";

  private String host = "localhost";
  private int port = 389;
  private String bindDn = "cn=admin,dc=example,dc=com";
  private String bindPassword = "secret";
  private String userBaseDn = "ou=users,dc=example,dc=com";
  private String groupBaseDn = "ou=groups,dc=example,dc=com";
  private boolean useTls = false;
  private boolean embedded = false;

  private List<String> userObjectClasses;
  private String userRdnAttribute;
  private Map<String, String> userAttributes;

  private List<String> groupObjectClasses;
  private String groupRdnAttribute;
  private Map<String, String> groupAttributes;

  protected ScimLdapConfig() {}

  /**
   * Creates a configuration with explicit LDAP connection settings, primarily for
   * programmatic or test use where loading from {@code scim-ldap.yml} is not desired.
   *
   * @param host         the LDAP server hostname
   * @param port         the LDAP server port
   * @param bindDn       the DN used to bind (authenticate) to the LDAP server
   * @param bindPassword the password for the bind DN
   * @param userBaseDn   the base DN under which user entries are stored
   * @param groupBaseDn  the base DN under which group entries are stored
   * @param useTls       {@code true} to enable TLS for LDAP connections
   */
  public ScimLdapConfig(String host, int port, String bindDn, String bindPassword,
                         String userBaseDn, String groupBaseDn, boolean useTls) {
    this.host = host;
    this.port = port;
    this.bindDn = bindDn;
    this.bindPassword = bindPassword;
    this.userBaseDn = userBaseDn;
    this.groupBaseDn = groupBaseDn;
    this.useTls = useTls;
  }

  @SuppressWarnings("unchecked")
  @PostConstruct
  protected void init() {
    Map<String, Object> config = loadYaml();

    // LDAP connection settings (YAML values, then system property overrides)
    Map<String, Object> ldap = (Map<String, Object>) config.getOrDefault("ldap", Collections.emptyMap());
    host = resolveString("ldap.host", ldap, "host", host);
    bindDn = resolveString("ldap.bind.dn", ldap, "bindDn", bindDn);
    bindPassword = resolveString("ldap.bind.password", ldap, "bindPassword", bindPassword);
    userBaseDn = resolveString("ldap.base.dn.users", ldap, "userBaseDn", userBaseDn);
    groupBaseDn = resolveString("ldap.base.dn.groups", ldap, "groupBaseDn", groupBaseDn);
    useTls = Boolean.parseBoolean(resolveString("ldap.use.tls", ldap, "useTls", String.valueOf(useTls)));
    embedded = Boolean.parseBoolean(resolveString("ldap.embedded", ldap, "embedded", String.valueOf(embedded)));

    String portStr = resolveString("ldap.port", ldap, "port", String.valueOf(port));
    try {
      port = Integer.parseInt(portStr.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Invalid ldap.port value: '" + portStr + "'. Must be a valid integer.", e);
    }

    // User mapping
    Map<String, Object> user = (Map<String, Object>) config.getOrDefault("user", Collections.emptyMap());
    userObjectClasses = (List<String>) user.getOrDefault("objectClasses",
      List.of("inetOrgPerson", "organizationalPerson", "person", "top"));
    userRdnAttribute = (String) user.getOrDefault("rdnAttribute", "uid");
    userAttributes = new LinkedHashMap<>((Map<String, String>) user.getOrDefault("attributes", defaultUserAttributes()));

    // Group mapping
    Map<String, Object> group = (Map<String, Object>) config.getOrDefault("group", Collections.emptyMap());
    groupObjectClasses = (List<String>) group.getOrDefault("objectClasses",
      List.of("groupOfNames", "top"));
    groupRdnAttribute = (String) group.getOrDefault("rdnAttribute", "cn");
    groupAttributes = new LinkedHashMap<>((Map<String, String>) group.getOrDefault("attributes", defaultGroupAttributes()));

    LOG.debug("LDAP configuration: host={}, port={}, bindDn={}, userBaseDn={}, groupBaseDn={}, useTls={}, embedded={}",
      host, port, bindDn, userBaseDn, groupBaseDn, useTls, embedded);

    if (!useTls && bindPassword != null && !bindPassword.isEmpty()) {
      LOG.warn("TLS is disabled — bind credentials will be sent in plaintext. Set ldap.use-tls=true for production.");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> loadYaml() {
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (is == null) {
        LOG.warn("No {} found on classpath, using defaults", CONFIG_FILE);
        return Collections.emptyMap();
      }
      ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
      Map<String, Object> config = yaml.readValue(is, Map.class);
      LOG.info("Loaded configuration from {}", CONFIG_FILE);
      return config;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
    }
  }

  private static String resolveString(String sysPropKey, Map<String, Object> yamlSection,
                                       String yamlKey, String defaultValue) {
    String yamlValue = yamlSection.containsKey(yamlKey)
      ? String.valueOf(yamlSection.get(yamlKey))
      : defaultValue;
    return System.getProperty(sysPropKey, yamlValue);
  }

  private static Map<String, String> defaultUserAttributes() {
    Map<String, String> attrs = new LinkedHashMap<>();
    attrs.put("userName", "uid");
    attrs.put("name.givenName", "givenName");
    attrs.put("name.familyName", "sn");
    attrs.put("name.formatted", "cn");
    attrs.put("displayName", "displayName");
    attrs.put("emails.value", "mail");
    attrs.put("phoneNumbers.value", "telephoneNumber");
    attrs.put("addresses.streetAddress", "street");
    attrs.put("addresses.locality", "l");
    attrs.put("addresses.postalCode", "postalCode");
    attrs.put("title", "title");
    attrs.put("userType", "employeeType");
    attrs.put("password", "userPassword");
    return attrs;
  }

  private static Map<String, String> defaultGroupAttributes() {
    Map<String, String> attrs = new LinkedHashMap<>();
    attrs.put("displayName", "cn");
    attrs.put("members.value", "member");
    return attrs;
  }

  /** Returns the LDAP server hostname. */
  public String getHost() { return host; }

  /** Returns the LDAP server port. */
  public int getPort() { return port; }

  /** Returns the DN used to bind to the LDAP server. */
  public String getBindDn() { return bindDn; }

  /** Returns the password for the bind DN. */
  public String getBindPassword() { return bindPassword; }

  /** Returns the base DN under which user entries are stored. */
  public String getUserBaseDn() { return userBaseDn; }

  /** Returns the base DN under which group entries are stored. */
  public String getGroupBaseDn() { return groupBaseDn; }

  /** Returns {@code true} if TLS is enabled for LDAP connections. */
  public boolean isUseTls() { return useTls; }

  /** Returns {@code true} if an embedded ApacheDS server should be started. */
  public boolean isEmbedded() { return embedded; }

  /** Returns the LDAP object classes used when creating user entries. */
  public List<String> getUserObjectClasses() { return userObjectClasses; }

  /** Returns the RDN attribute name for user entries (e.g. {@code uid}). */
  public String getUserRdnAttribute() { return userRdnAttribute; }

  /** Returns the SCIM-to-LDAP attribute mapping for users, keyed by SCIM attribute path. */
  public Map<String, String> getUserAttributes() { return userAttributes; }

  /** Returns the LDAP object classes used when creating group entries. */
  public List<String> getGroupObjectClasses() { return groupObjectClasses; }

  /** Returns the RDN attribute name for group entries (e.g. {@code cn}). */
  public String getGroupRdnAttribute() { return groupRdnAttribute; }

  /** Returns the SCIM-to-LDAP attribute mapping for groups, keyed by SCIM attribute path. */
  public Map<String, String> getGroupAttributes() { return groupAttributes; }
}
