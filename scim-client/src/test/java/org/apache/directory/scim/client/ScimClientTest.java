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

package org.apache.directory.scim.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import org.apache.directory.scim.client.rest.ResourceTypesClient;
import org.apache.directory.scim.client.rest.ScimGroupClient;
import org.apache.directory.scim.client.rest.ScimSelfClient;
import org.apache.directory.scim.client.rest.ScimUserClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScimClientTest {

  private static final String BASE_URL = "https://scim.example.com/test";

  @Test
  public void userClient() {
    Client client = mock(Client.class);
    ScimClient scimClient = new ScimClient(client, BASE_URL);
    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(BASE_URL)).thenReturn(webTarget);
    WebTarget usersTarget = mock(WebTarget.class);
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    when(webTarget.path(pathCapture.capture())).thenReturn(usersTarget);

    ScimUserClient userClient = scimClient.userClient();

    assertThat(userClient).extracting("client").isSameAs(client);
    assertThat(userClient).extracting("target").isSameAs(usersTarget);
    assertThat(pathCapture.getValue()).isEqualTo("/Users");
  }

  @Test
  public void groupClient() {
    Client client = mock(Client.class);
    ScimClient scimClient = new ScimClient(client, BASE_URL);
    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(BASE_URL)).thenReturn(webTarget);
    WebTarget usersTarget = mock(WebTarget.class);
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    when(webTarget.path(pathCapture.capture())).thenReturn(usersTarget);

    ScimGroupClient groupClient = scimClient.groupClient();

    assertThat(groupClient).extracting("client").isSameAs(client);
    assertThat(groupClient).extracting("target").isSameAs(usersTarget);
    assertThat(pathCapture.getValue()).isEqualTo("/Groups");
  }

  @Test
  public void typesClient() {
    Client client = mock(Client.class);
    ScimClient scimClient = new ScimClient(client, BASE_URL);
    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(BASE_URL)).thenReturn(webTarget);
    WebTarget usersTarget = mock(WebTarget.class);
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    when(webTarget.path(pathCapture.capture())).thenReturn(usersTarget);

    ResourceTypesClient typesClient = scimClient.resourceTypesClient();

    assertThat(typesClient).extracting("client").isSameAs(client);
    assertThat(typesClient).extracting("target").isSameAs(usersTarget);
    assertThat(pathCapture.getValue()).isEqualTo("ResourceTypes");
  }

  @Test
  public void selfClient() {
    Client client = mock(Client.class);
    ScimClient scimClient = new ScimClient(client, BASE_URL);
    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(BASE_URL)).thenReturn(webTarget);
    WebTarget usersTarget = mock(WebTarget.class);
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    when(webTarget.path(pathCapture.capture())).thenReturn(usersTarget);

    ScimSelfClient selfClient = scimClient.selfClient();

    assertThat(selfClient).extracting("client").isSameAs(client);
    assertThat(selfClient).extracting("target").isSameAs(usersTarget);
    assertThat(pathCapture.getValue()).isEqualTo("Me");
  }
}
