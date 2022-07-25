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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.spec.protocol.Constants;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.ListResponse;
import org.apache.directory.scim.spec.protocol.data.PatchRequest;
import org.apache.directory.scim.spec.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.SortOrder;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScimUserClientTest {

  private static final String BASE_URL = "https://scim.example.com/test";

  @Test
  public void findTest() throws Exception {

    ListResponse<ScimUser> mockListResponse = mock(ListResponse.class);
    MockClient mockClient = new MockClient(BASE_URL, "POST", ok(mockListResponse));
    SearchRequest searchRequest = new SearchRequest();

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ListResponse<ScimUser> listResponse = userClient.find(searchRequest);

    assertThat(listResponse).isSameAs(mockListResponse);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/.search");
    assertThat(mockClient.requestEntity()).isSameAs(searchRequest);
  }

  @Test
  public void queryTest() throws Exception {

    ListResponse<ScimUser> mockListResponse = mock(ListResponse.class);
    MockClient mockClient = new MockClient(BASE_URL, "GET", ok(mockListResponse));
    AttributeReferenceListWrapper attributes = mock(AttributeReferenceListWrapper.class);
    AttributeReferenceListWrapper excludedAttributes = mock(AttributeReferenceListWrapper.class);
    Filter filter = mock(Filter.class);
    AttributeReference sortBy = mock(AttributeReference.class);

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ListResponse<ScimUser> listResponse = userClient.query(attributes, excludedAttributes, filter, sortBy, SortOrder.ASCENDING, 5, 42);

    Map<String, Object> expectedQueryParams = new HashMap<>();
    expectedQueryParams.put("attributes", null);
    expectedQueryParams.put("count", 42);
    expectedQueryParams.put("excludedAttributes", null);
    expectedQueryParams.put("filter", filter);
    expectedQueryParams.put("sortBy", sortBy);
    expectedQueryParams.put("sortOrder", "ASCENDING");
    expectedQueryParams.put("startIndex", 5);

    assertThat(listResponse).isSameAs(mockListResponse);
    assertThat(mockClient.requestPath()).isEqualTo("/Users");
    assertThat(mockClient.queryParams()).isEqualTo(expectedQueryParams);
  }

  @Test
  public void getById_found() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "GET", ok(entity));
    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    Optional<ScimUser> optionalResponse = userClient.getById("test-id");

    assertThat(optionalResponse).isNotNull();
    assertThat(optionalResponse.get()).isSameAs(entity);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/test-id");
  }

  @Test
  public void getById_notFound() throws Exception {
    MockClient mockClient = new MockClient(BASE_URL, "GET", notFound());

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    Optional<ScimUser> optionalResponse = userClient.getById("test-id-not-found");

    assertThat(optionalResponse).isEmpty();
    assertThat(mockClient.requestPath()).isEqualTo("/Users/test-id-not-found");
  }

  @Test
  public void getById_serverError() throws Exception {
    MockClient mockClient = new MockClient(BASE_URL, "GET", error(Response.Status.INTERNAL_SERVER_ERROR));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimException exception = expect(ScimException.class, () -> userClient.getById("test-id-error"));

    assertThat(exception.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/test-id-error");
  }

  @Test
  public void create_success() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    ScimUser user = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "POST", ok(entity));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimUser response = userClient.create(user);

    assertThat(response).isSameAs(entity);
    assertThat(mockClient.requestEntity()).isSameAs(user);
    assertThat(mockClient.requestPath()).isEqualTo("/Users");
  }

  @Test
  public void create_fail() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "POST", error(Response.Status.INTERNAL_SERVER_ERROR));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimException exception = expect(ScimException.class, () -> userClient.create(entity));

    assertThat(exception.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    assertThat(mockClient.requestEntity()).isSameAs(entity);
    assertThat(mockClient.requestPath()).isEqualTo("/Users");
  }

  @Test
  public void create_withRestCallFail() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "POST", error(Response.Status.INTERNAL_SERVER_ERROR));
    ErrorResponse error = new ErrorResponse(Response.Status.CONFLICT, "expected test exception in create_withRestCall");
    RestCall restCall = (invocation) -> { throw new RestException(409, error); };

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL, restCall);
    ScimException exception = expect(ScimException.class, () -> userClient.create(entity));

    assertThat(exception.getStatus()).isEqualTo(Response.Status.CONFLICT);
    assertThat(mockClient.requestEntity()).isSameAs(entity);
    assertThat(mockClient.requestPath()).isEqualTo("/Users");
  }

  @Test
  public void update_success() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    ScimUser user = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "PUT", ok(entity));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimUser response = userClient.update("update-test", user);

    assertThat(response).isSameAs(entity);
    assertThat(mockClient.requestEntity()).isSameAs(user);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/update-test");
  }

  @Test
  public void patch_success() throws Exception {
    PatchRequest patch = mock(PatchRequest.class);
    ScimUser entity = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "PATCH", ok(entity));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimUser response = userClient.patch("update-test", patch);

    assertThat(response).isSameAs(entity);
    assertThat(mockClient.requestEntity()).isSameAs(patch);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/update-test");
  }

  @Test
  public void delete_success() throws Exception {
    ScimUser entity = mock(ScimUser.class);
    MockClient mockClient = new MockClient(BASE_URL, "DELETE", ok(entity));

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    userClient.delete("test-delete");

    assertThat(mockClient.requestPath()).isEqualTo("/Users/test-delete");
  }

  @Test
  public void delete_error() throws Exception {
    MockClient mockClient = new MockClient(BASE_URL, "DELETE", notFound());

    ScimUserClient userClient = new ScimUserClient(mockClient.client, BASE_URL);
    ScimException exception = expect(ScimException.class, () -> userClient.delete("test-delete-404"));

    assertThat(exception.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    assertThat(mockClient.requestPath()).isEqualTo("/Users/test-delete-404");
  }

  static Response ok(Object entity) {
    Response response = mock(Response.class);
    when(response.readEntity(any(GenericType.class))).thenReturn(entity); // list response
    when(response.readEntity(any(Class.class))).thenReturn(entity); // single entity
    when(response.getStatusInfo()).thenReturn(Response.Status.OK);
    when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
    return response;
  }

  static Response notFound() {
    return error(Response.Status.NOT_FOUND);
  }

  static Response error(Response.Status status) {
    Response response = mock(Response.class);
    when(response.getStatusInfo()).thenReturn(status);
    when(response.getStatus()).thenReturn(status.getStatusCode());
    return response;
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

  <T extends Exception> T expect(Class<T> exceptionType, ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      if (e.getClass().equals(exceptionType)) {
        return (T) e;
      }
      throw new RuntimeException("Expected block to throw exception of type: " + exceptionType + " but was: " + e.getClass(), e);
    }
    throw new RuntimeException("Expected block to throw exception of type " + exceptionType);
  }

  static class MockClient {

    Client client = mock(Client.class);
    WebTarget webTarget = mock(WebTarget.class);
    Invocation.Builder builder = mock(Invocation.Builder.class);
    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    Invocation invocation = mock(Invocation.class);
    ArgumentCaptor<String> pathCapture = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> queryKeysCapture = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object> queryValuesCapture = ArgumentCaptor.forClass(Object.class);

    MockClient(String baseUrl, String method, Response response) {
      when(client.target(baseUrl)).thenReturn(webTarget);
      when(webTarget.path(pathCapture.capture())).thenReturn(webTarget);
      when(webTarget.request(Constants.SCIM_CONTENT_TYPE)).thenReturn(builder);
      when(webTarget.queryParam(queryKeysCapture.capture(), queryValuesCapture.capture())).thenReturn(webTarget);
      when(invocation.invoke()).thenReturn(response);

      switch(method){
        case "POST":
          when(builder.buildPost(entityCaptor.capture())).thenReturn(invocation);
          break;
        case "PUT":
          when(builder.buildPut(entityCaptor.capture())).thenReturn(invocation);
          break;
        case "PATCH":
          when(builder.build(eq("PATCH"), entityCaptor.capture())).thenReturn(invocation);
          break;
        case "GET":
          when(builder.buildGet()).thenReturn(invocation);
          break;
        case "DELETE":
          when(builder.buildDelete()).thenReturn(invocation);
          break;
        default:
          throw new IllegalStateException("Unsupported method type '" + method + "', an update to the `MockClient` is needed");
      }
    }

    String requestPath() {
      return String.join("/", pathCapture.getAllValues());
    }

    Map<String, Object> queryParams() {
      List<String> keys = queryKeysCapture.getAllValues();
      List<Object> values = queryValuesCapture.getAllValues();

      Map<String, Object> params = new LinkedHashMap<>(keys.size());
      IntStream
        .range(0, keys.size())
        .forEach(i -> params.put(keys.get(i), values.get(i)));

      return params;
    }

    Object requestEntity() {
      return entityCaptor.getValue().getEntity();
    }
  }
}
