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

package org.apache.directory.scim.compliance.server.configuration;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Allows the connection information to be specified via environment variables
 * or JVM properties so that tests can be executed against different
 * environments (dev, test, acc, sb, prod).
 */
@Data
@Accessors(chain = true)
public class Configuration {

  private static final String DEFAULT_BASE_URL = "http://localhost:8080/v2";

  private static final String PROPERTY_BASE_URL = "scimtest.baseurl";
  private static final String PROPERTY_PROXY_HOST = "scimtest.proxy.host";
  private static final String PROPERTY_PROXY_PORT = "scimtest.proxy.port";
  private static final String PROPERTY_AUTH_HEADER = "scimtest.auth.header";

  private String baseUrl;

  private String proxyHost;

  private Integer proxyPort;

  private String authHeaderValue;

  private boolean debug;

  private Map<String, String> resourcesSupported = new HashMap<>();

  public static Configuration fromEnvironment() {

    // allow for setting property via Arquillian or other
    String serverPort = System.getProperty("server.http.port");
    String defaultBaseUrl = DEFAULT_BASE_URL;
    if (serverPort != null) {
      defaultBaseUrl = "http://localhost:" + serverPort + "/v2";
    }

    Configuration configuration = new Configuration()
        .setBaseUrl(getValue(PROPERTY_BASE_URL, defaultBaseUrl))
        .setAuthHeaderValue(getValue(PROPERTY_AUTH_HEADER))
        .setProxyHost(getValue(PROPERTY_PROXY_HOST));

    String rawPort = getValue(PROPERTY_PROXY_PORT);
    if (StringUtils.isNotEmpty(rawPort)) {
      configuration.setProxyPort(Integer.parseInt(rawPort));
    }

    return configuration;
  }

  private static String getValue(String sysPropName) {
    return getValue(sysPropName, null);
  }

  private static String getValue(String sysPropName, String defaultValue) {

    // try sys props first
    String value = System.getProperty(sysPropName);

    // then environment variables
    if (StringUtils.isEmpty(value)) {
      value = System.getenv(toEnvVar(sysPropName));
    }

    // then the default value
    if (StringUtils.isEmpty(value)) {
      value = defaultValue;
    }

    return value;
  }

  private static String toEnvVar(String sysPropName) {
    return sysPropName.replaceAll("\\.", "_").toUpperCase(Locale.ENGLISH);
  }
}
