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

package org.apache.directory.scim.example.memory.containers;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class OpenLibertyContainer extends GenericContainer<OpenLibertyContainer> implements WebAppContainer<OpenLibertyContainer> {

  public OpenLibertyContainer(String baseImage, Path warFile, Duration startupTimeout) {
    super(
      new ImageFromDockerfile("open-liberty:testcontainer")

        .withFileFromTransferable("target/app.war",
          MountableFile.forHostPath(warFile.toAbsolutePath(), 0444))
        .withFileFromTransferable("liberty/config/server.xml",
          Transferable.of(serverXml(8080, "/config/apps/example.war"), 0444))

        .withDockerfileFromBuilder(builder ->
          builder
            .from(baseImage)
            .user("root")

            .copy("liberty/config/server.xml", "/config/server.xml")

            .copy("target/app.war", "/config/apps/example.war")
            .run("chown default:root /config/apps/example.war /config/server.xml")

            .user("default")
            .run("configure.sh")
            .build()));

    this.withExposedPorts(8080)
      .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("container")))
      .withStartupTimeout(Duration.ofSeconds(60))
      .waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1)
        .withStartupTimeout(startupTimeout));
  }

  static String serverXml(int port, String warPath) {
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <server description="example">
      
        <featureManager>
          <platform>jakartaee-11.0</platform>
          <feature>restfulWS-3.1</feature>
        </featureManager>
      
        <variable name="http.port" defaultValue="%d" />
        <!-- tag::contextRoot[] -->
        <variable name="context.root" defaultValue="/" />
        <!-- end::contextRoot[] -->
      
        <httpEndpoint id="defaultHttpEndpoint" host="*"
                      httpPort="${http.port}" />
      
        <!-- Automatically expand WAR files and EAR files -->
        <applicationManager autoExpand="true"/>
        <applicationMonitor dropinsEnabled="true" />
      
        <!-- Configures the application on a specified context root -->
        <webApplication contextRoot="${context.root}"
                        location="%s"/>
      </server>
      """.formatted(port, warPath);
  }
}
