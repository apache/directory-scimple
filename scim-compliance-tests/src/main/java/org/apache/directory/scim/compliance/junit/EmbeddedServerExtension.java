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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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

public class EmbeddedServerExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

  private ScimTestServer server;
  private URI uri;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {

    ServiceLoader<ScimTestServer> serviceLoader = ServiceLoader.load(ScimTestServer.class);
    server = serviceLoader.findFirst().orElseThrow(() -> new RuntimeException("Failed to find implementation of ScimTestServer via ServiceLoader"));
    uri = server.start(randomPort());
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    final List<Object> testInstances = context.getRequiredTestInstances().getAllInstances();
    testInstances.forEach(test -> {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(test.getClass(), ScimServerUri.class);
        Arrays.stream(fields).forEach(field -> {
          try {
            field.setAccessible(true);
            FieldUtils.writeField(field, test, uri);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        });
      }
    );
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.shutdown();
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
