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

package org.apache.directory.scim.client.rest;

import jakarta.ws.rs.core.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.filter.FilterBuilder;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScimUserClientTest extends ClientTestSupport {

  @Test
  public void notFound(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(scimResponse()
      .setResponseCode(404));

    assertThat(client.getById("invalid-id")).isNotPresent();

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/v2/Users/invalid-id");
  }

  @Test
  public void basic200(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(scimResponse()
      .setBody("{\"id\": \"valid-id\"}")
      .setResponseCode(200));

    assertThat(client.getById("valid-id")).isPresent().get().hasFieldOrPropertyWithValue("id", "valid-id");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/v2/Users/valid-id");

  }

  @Test
  public void client500(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(new MockResponse()
      .setResponseCode(500));

    assertThrows(ScimException.class, () -> client.getById("id"));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/v2/Users/id");
  }

  @Test
  public void queryAll(MockWebServer server, ScimUserClient client) throws Exception {
    server.enqueue(scimResponse(Map.of(
      "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
      "totalResults", 2,
      "Resources", List.of(
        Map.of("id", "2819c223-7f76-453a-919d-413861904646",
          "userName", "bjensen"),
        Map.of("id", "c75ad752-64ae-4823-840d-ffa80929976c",
          "userName", "jsmith")
      )
    )));

    ScimUser bjensen = new ScimUser();
    bjensen.setUserName("bjensen");
    bjensen.setId("2819c223-7f76-453a-919d-413861904646");

    ScimUser jsmith = new ScimUser();
    jsmith.setUserName("jsmith");
    jsmith.setId("c75ad752-64ae-4823-840d-ffa80929976c");

    ListResponse<ScimUser> users = client.query(null, null, null, null, null, null, null);
    assertThat(users.getItemsPerPage()).isNull();
    assertThat(users.getStartIndex()).isNull();
    assertThat(users.getTotalResults()).isEqualTo(2);
    assertThat(users.getResources()).containsExactly(bjensen, jsmith);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/v2/Users");
  }

  @Test
  public void create(MockWebServer server, ScimUserClient client) throws Exception {
    server.enqueue(scimResponse(Map.of(
      "id", "created-id", "userName", "testUser")));

    ScimUser testUser = new ScimUser();
    testUser.setUserName("testUser");

    ScimUser expectedUser = new ScimUser();
    expectedUser.setUserName("testUser");
    expectedUser.setId("created-id");

    assertThat(client.create(testUser)).isEqualTo(expectedUser);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/v2/Users");
  }

  @Test
  public void findByFilter(MockWebServer server, ScimUserClient client) throws Exception {
    server.enqueue(scimResponse(Map.of(
      "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
      "totalResults", 1,
      "Resources", List.of(
        Map.of("id", "2819c223-7f76-453a-919d-413861904646",
          "userName", "bjensen")
      )
    )));

    ScimUser bjensen = new ScimUser();
    bjensen.setUserName("bjensen");
    bjensen.setId("2819c223-7f76-453a-919d-413861904646");

    SearchRequest searchRequest = new SearchRequest()
      .setFilter(FilterBuilder.create().equalTo("userName", "bjensen").build());

    ListResponse<ScimUser> users = client.find(searchRequest);
    assertThat(users.getItemsPerPage()).isNull();
    assertThat(users.getStartIndex()).isNull();
    assertThat(users.getTotalResults()).isEqualTo(1);
    assertThat(users.getResources()).containsExactly(bjensen);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/v2/Users/.search");
  }

  @Test
  public void update(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(scimResponse(Map.of(
      "id", "updated-id", "userName", "testUser")));

    ScimUser testUser = new ScimUser();
    testUser.setUserName("testUser");
    testUser.setId("updated-id");

    ScimUser expectedUser = new ScimUser();
    expectedUser.setUserName("testUser");
    expectedUser.setId("updated-id");

    ScimUser resultUser = client.update("updated-id", testUser);
    assertThat(resultUser).isEqualTo(expectedUser);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("PUT");
    assertThat(request.getPath()).isEqualTo("/v2/Users/updated-id");
  }

  @Test
  public void update500(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(new MockResponse()
      .setResponseCode(500));

    ScimUser testUser = new ScimUser();
    testUser.setUserName("testUser");
    testUser.setId("500-id");

    ScimException exception = assertThrows(ScimException.class, () -> client.update("500-id", testUser));
    assertThat(exception.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void patch(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(scimResponse(Map.of(
      "id", "patched-id", "userName", "testUser")));

    PatchRequest patchRequest = new PatchRequest().add(
      new PatchOperation()
        .setOperation(PatchOperation.Type.REMOVE)
        .setPath(new PatchOperationPath("name.honorificPrefix"))
    );

    ScimUser expectedUser = new ScimUser();
    expectedUser.setUserName("testUser");
    expectedUser.setId("patched-id");

    ScimUser resultUser = client.patch("patched-id", patchRequest);
    assertThat(resultUser).isEqualTo(expectedUser);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("PATCH");
    assertThat(request.getPath()).isEqualTo("/v2/Users/patched-id");
  }

  @Test
  public void delete(MockWebServer server, ScimUserClient client) throws Exception {
    server.enqueue(new MockResponse()
      .setResponseCode(204));

    client.delete("delete-id");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("DELETE");
    assertThat(request.getPath()).isEqualTo("/v2/Users/delete-id");
  }

  @Test
  public void delete500(MockWebServer server, ScimUserClient client) throws Exception {

    server.enqueue(new MockResponse()
      .setResponseCode(500));

    ScimException exception = assertThrows(ScimException.class, () -> client.delete("500-id"));
    assertThat(exception.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
  }
}
