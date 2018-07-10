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

/**
 * 
 */
package edu.psu.swe.scim.compliance.server.configuration;

import com.eclipsesource.restfuse.Destination;

/**
 * Allows the Restfuse Destination to be specified via environment variables
 * or JVM properties so that tests can be executed against different
 * environments (dev, test, acc, sb, prod).
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
public final class Configuration {

  public static final String DEFAULT_DESTINATION = "http://localhost:8080/scim/v2";
  public static final String DEFAULT_PROXY_HOST = "";
  public static final String DEFAULT_PROXY_PORT = "";

  public static final String ENV_DESTINATION = "RESTFUSE_DESTINATION";
  public static final String ENV_PROXY_HOST = "RESTFUSE_PROXY_HOST";
  public static final String ENV_PROXY_PORT = "RESTFUSE_PROXY_PORT";

  public static final String PROPERTY_DESTINATION = "restfuse.destination";
  public static final String PROPERTY_PROXY_HOST = "restfuse.proxy.host";
  public static final String PROPERTY_PROXY_PORT = "restfuse.proxy.port";

  static String destinationUrl;
  static String proxyHost;
  static Integer proxyPort;

  /*
  * Processes the environment variables and JVM properties statically so that
  * this processing only happens once per test run.
  */
  static {
    destinationUrl = getValue(DEFAULT_DESTINATION, ENV_DESTINATION, PROPERTY_DESTINATION);
    proxyHost = getValue(DEFAULT_PROXY_HOST, ENV_PROXY_HOST, PROPERTY_PROXY_HOST);
    String proxyPortString = getValue(DEFAULT_PROXY_PORT, ENV_PROXY_HOST, PROPERTY_PROXY_PORT);
    
    if (proxyPortString != null && !proxyPortString.isEmpty()) {
      proxyPort = Integer.parseInt(proxyPortString);
    }
  }

  private Configuration() {
    // Make this a utility class
  }

  /**
   * Creates a Restfuse Destination for the provided testObject using the URL
   * proxy host and proxy port gleaned from the environment variables and/or
   * JVM properties.
   * 
   * @param testObject the test
   * @return
   */
  public static Destination getDestination(Object testObject) {
    Destination destination = new Destination(testObject, destinationUrl);

    if (proxyHost != null && proxyPort != null) {
      destination = new Destination(testObject, destinationUrl, proxyHost, proxyPort);
    }

    return destination;
  }

  /*
   * Generic way to get a default string value, environment variable value or
   * property value in that order of priority.
   */
  private static String getValue(String defaultValue, String envName, String propertyName) {
    String value = System.getenv(envName);
    if (value == null) {
      value = defaultValue;
    }
    value = System.getProperty(propertyName, value);
    return value;
  }

}
