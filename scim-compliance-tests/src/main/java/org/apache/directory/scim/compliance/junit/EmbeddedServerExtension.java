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

package org.apache.directory.scim.compliance.junit;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

public class EmbeddedServerExtension implements BeforeEachCallback {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedServerExtension.class);

  private static ScimTestServer server;
  private static URI uri;

  static {
    // Start a single instance of the ScimTestServer
    // this instance is shared for all tests using this extension
    setupServer();
  }

  private static void setupServer() {
    ServiceLoader<ScimTestServer> serviceLoader = ServiceLoader.load(ScimTestServer.class);
    serviceLoader.findFirst()
      .ifPresentOrElse(testServer -> {
        try {
          server = testServer;
          uri = server.start(randomPort());

          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
              try {
                server.shutdown();
              } catch (Exception e) {
                logger.warn("Failed to shutdown test server", e);
              }
            }
          }));

        } catch (Exception e) {
          throw new TestInstantiationException("Failed to start test server: "+ testServer, e);
        }
      }, () -> logger.info("Could not find implementation of ScimTestServer via ServiceLoader, assuming server is started using different technique"));
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    if (uri != null) {
      final List<Object> testInstances = context.getRequiredTestInstances().getAllInstances();
      testInstances.forEach(test -> {
          Field[] fields = FieldUtils.getFieldsWithAnnotation(test.getClass(), ScimServerUri.class);
          Arrays.stream(fields).forEach(field -> {
            try {
              field.setAccessible(true);
              FieldUtils.writeField(field, test, uri);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("Failed to assign value to field annotated with '@ScimServerUri'", e);
            }
          });
        }
      );
    }
  }

  private static int randomPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Failed to find a free server port", e);
    }
  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ScimServerUri {
  }

  public interface ScimTestServer {
    URI start(int port) throws Exception;
    void shutdown() throws Exception;
  }
}
