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

package org.apache.directory.scim.example.memory;

import org.apache.directory.scim.example.memory.containers.GlassfishContainer;
import org.apache.directory.scim.example.memory.containers.OpenLibertyContainer;
import org.apache.directory.scim.example.memory.containers.PayaraContainer;
import org.apache.directory.scim.example.memory.containers.WebAppContainer;
import org.apache.directory.scim.example.memory.containers.WildflyContainer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@Testcontainers
public class ContainerIT {

  static Stream<Arguments> containers() {

    // this project's war file to test
    Path warFile = Paths.get("target/scim-server-memory.war");

    Map<String, Function<ContainerConfiguration, WebAppContainer<?>>> containers = Map.of(
      "payara", config -> new PayaraContainer(config.imageName, warFile, config.timeout),
      "glassfish", config -> new GlassfishContainer(config.imageName, warFile, config.timeout),
      "wildfly", config -> new WildflyContainer(config.imageName, warFile, config.timeout),
      "open-liberty", config -> new OpenLibertyContainer(config.imageName, warFile, config.timeout)
    );

    // Apply config from container.properties and system properties
    Properties testConfig = getTestConfiguration();
    return containers.entrySet().stream()
      .map(e -> {
        ContainerConfiguration config = ContainerConfiguration.fromConfig(e.getKey(), testConfig);
        return Arguments.of(Named.of(e.getKey(), config), Named.of(config.imageName, e.getValue().apply(config)));
      });
  }

  @ParameterizedTest
  @MethodSource("containers")
  void webAppTest(ContainerConfiguration config, WebAppContainer<?> container) {

    Assumptions.assumeTrue(config.enabled,
      "This test has been disabled by configuring the property '" + config.name + ".enabled=false'");

    try (container) {
      container.start();

      given()
        .log().everything()
        .contentType("application/scim+json")
        .when()
        .get(container.getAppUrl() + "/v2/ServiceProviderConfig")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(200);
    }
  }

  static Properties getTestConfiguration() {
    // images are referenced from a properties file, see container.properties
    // system properties can override default configuration.
    Properties containerProps = new Properties(System.getProperties());
    try {
      containerProps.load(ContainerIT.class.getResourceAsStream("/container.properties"));
    } catch (IOException e) {
      throw new TestInstantiationException("Unable to load container.properties", e);
    }
    return containerProps;
  }

  record ContainerConfiguration(String name, String imageName, Duration timeout, boolean enabled) {
    static ContainerConfiguration fromConfig(String name, Properties properties) {
      boolean enabled = Boolean.parseBoolean(properties.getProperty(name + ".enabled", "true"));
      String imageName = properties.getProperty(name + ".image");
      Duration timeout = Duration.parse(properties.getProperty(name + ".timeout", "PT30S"));

      if (imageName == null) {
        throw new TestInstantiationException("Missing image for " + name + ", check the '" + name + ".image' property, in container.properties");
      }

      return new ContainerConfiguration(name, imageName, timeout, enabled);
    }
  }
}
