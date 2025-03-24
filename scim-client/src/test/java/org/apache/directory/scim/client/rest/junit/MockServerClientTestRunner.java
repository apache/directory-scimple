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

package org.apache.directory.scim.client.rest.junit;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.directory.scim.client.rest.ScimUserClient;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.extension.*;

import java.util.List;

public class MockServerClientTestRunner implements ParameterResolver, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback {

  private final List<Class<?>> supportedClasses = List.of(MockWebServer.class, ScimUserClient.class, Client.class);

  private final static Weld WELD = new Weld();
  static {
    // Use single instance of Weld for all tests using this runner
    WELD.initialize();
    Runtime.getRuntime().addShutdownHook(new Thread(WELD::shutdown));
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return supportedClasses.contains(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {

    Class<?> paramType = parameterContext.getParameter().getType();
    if (ScimUserClient.class.equals(paramType)) {

      MockWebServer server = fromStore(MockWebServer.class, context);
      Client client = fromStore(Client.class, context);

      ScimUserClient scimUserClient = new ScimUserClient(client, server.url("/v2").toString());
      getStore(context).put(ScimUserClient.class, scimUserClient);
      return scimUserClient;
    }

    return fromStore(paramType, context);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {

    MockWebServer server = new MockWebServer();
    getStore(context).put(MockWebServer.class, server);

    Client client = ClientBuilder.newBuilder()
      .withConfig( // the default Jersey client does not support PATCH requests
        new ClientConfig().connectorProvider(new JdkConnectorProvider()))
      .build();
    getStore(context).put(Client.class, client);
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {

    MockWebServer server = fromStore(MockWebServer.class, context);
    if (server != null) {
      server.start();
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    MockWebServer server = fromStore(MockWebServer.class, context);
    if (server != null) {
      server.shutdown();
    }

    Client client = fromStore(Client.class, context);
    if (client != null) {
      client.close();
    }

    ScimUserClient userClient = fromStore(ScimUserClient.class, context);
    if (userClient != null) {
      userClient.close();
    }
  }

  private ExtensionContext.Store getStore(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.create(this));
  }

  private <T> T fromStore(Class<T> type, ExtensionContext context) {
    return getStore(context).get(type, type);
  }
}
