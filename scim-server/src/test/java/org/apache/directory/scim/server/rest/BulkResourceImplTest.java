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

package org.apache.directory.scim.server.rest;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.BulkOperation;
import org.apache.directory.scim.protocol.data.BulkRequest;
import org.apache.directory.scim.protocol.data.BulkResponse;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.core.repository.ScimRequestContext;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BulkResourceImplTest {

  @Test
  public void bulkIdTemporaryIdentifiersTest() throws Exception {
    // Request copied from RFC 7644 section 3.7.2
    ScimUser alice = new ScimUser()
      .setUserName("Alice");

    ScimGroup tourGuides = new ScimGroup()
      .setDisplayName("Tour Guides")
      .setMembers(List.of(new GroupMembership()
        .setType(GroupMembership.Type.USER)
        .setValue("bulkId:qwerty")));

    BulkRequest bulkRequest = new BulkRequest()
      .setOperations(List.of(
        new BulkOperation()
          .setMethod(BulkOperation.Method.POST)
          .setPath("/Users")
          .setBulkId("qwerty")
          .setData(alice),
        new BulkOperation()
          .setMethod(BulkOperation.Method.POST)
          .setPath("/Groups")
          .setBulkId("ytrewq")
          .setData(tourGuides)));

    Instance<Repository<? extends ScimResource>> emptyInstance = mock(Instance.class);
    when(emptyInstance.stream()).thenReturn(Stream.empty());

    SchemaRegistry schemaRegistry = new SchemaRegistry();
    RepositoryRegistry repositoryRegistry = new RepositoryRegistry(schemaRegistry);

    Instance<Repository<ScimUser>> userRepositoryInstance = mock(Instance.class);
    Repository<ScimUser> userRepository = mock(Repository.class);
    ScimUser user = new ScimUser();
    user.setId("alice-id");
    when(userRepositoryInstance.get()).thenReturn(userRepository);
    repositoryRegistry.registerRepository(ScimUser.class, userRepository);
    when(userRepository.create(any(), any())).thenReturn(user);

    Instance<Repository<ScimGroup>> groupProviderInstance = mock(Instance.class);
    Repository<ScimGroup> groupRepository = mock(Repository.class);
    ScimGroup group = new ScimGroup();
    group.setId("tour-guides");
    when(groupProviderInstance.get()).thenReturn(groupRepository);
    repositoryRegistry.registerRepository(ScimGroup.class, groupRepository);
    when(groupRepository.getExtensionList()).thenReturn(Collections.emptyList());
    when(groupRepository.create(any(), any())).thenReturn(group);

    BulkResourceImpl impl = new BulkResourceImpl(schemaRegistry, repositoryRegistry);
    UriInfo uriInfo = mock(UriInfo.class);
    UriBuilder uriBuilder = mock(UriBuilder.class);
    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
    when(uriBuilder.path("/Users")).thenReturn(uriBuilder);
    when(uriBuilder.path("alice-id")).thenReturn(uriBuilder);
    when(uriBuilder.build())
      .thenReturn(URI.create("https://scim.example.com/Users/alice-id"))
      .thenReturn(URI.create("https://scim.example.com/Groups/tour-guides"));
    when(uriBuilder.path("/Groups")).thenReturn(uriBuilder);
    when(uriBuilder.path("tour-guides")).thenReturn(uriBuilder);

    Response response = impl.doBulk(bulkRequest, uriInfo);
    BulkResponse bulkResponse = (BulkResponse) response.getEntity();

    assertThat(bulkResponse.getErrorResponse()).isNull();
    assertThat(bulkResponse.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(bulkResponse.getSchemas()).containsOnly(BulkResponse.SCHEMA_URI);
    assertThat(bulkResponse.getOperations())
      .hasSize(2)
      .contains(new BulkOperation()
        .setMethod(BulkOperation.Method.POST)
        .setBulkId("qwerty")
        .setData(new ScimUser()
          .setId("alice-id"))
        .setLocation("https://scim.example.com/Users/alice-id")
        .setStatus(new BulkOperation.StatusWrapper(Response.Status.CREATED)))
      .contains(new BulkOperation()
        .setMethod(BulkOperation.Method.POST)
        .setBulkId("ytrewq")
        .setData(new ScimGroup()
          .setId("tour-guides"))
        .setLocation("https://scim.example.com/Groups/tour-guides")
        .setStatus(new BulkOperation.StatusWrapper(Response.Status.CREATED)));

    // Verify behavior
    InOrder inOrder = inOrder(userRepository, groupRepository);
    inOrder.verify(userRepository, atLeast(1)).getExtensionList();
    inOrder.verify(groupRepository, atLeast(1)).getExtensionList();

    // User was created before group due to calculated dependency
    inOrder.verify(userRepository).create(alice, new ScimRequestContext());
    inOrder.verify(groupRepository).create(tourGuides, new ScimRequestContext());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void bulkFailedTest() throws Exception {
    ScimUser alice = new ScimUser()
      .setUserName("Alice");
    ScimUser bob = new ScimUser()
      .setUserName("Bob");

    BulkRequest bulkRequest = new BulkRequest()
      .setFailOnErrors(1)
      .setOperations(List.of(
        new BulkOperation()
          .setMethod(BulkOperation.Method.POST)
          .setPath("/Users")
          .setBulkId("bulk-id-alice")
          .setData(alice),
        new BulkOperation()
          .setMethod(BulkOperation.Method.POST)
          .setPath("/Users")
          .setBulkId("bulk-id-bob")
          .setData(bob)
      ));
    Instance<Repository<? extends ScimResource>> emptyInstance = mock(Instance.class);
    when(emptyInstance.stream()).thenReturn(Stream.empty());

    SchemaRegistry schemaRegistry = new SchemaRegistry();
    RepositoryRegistry repositoryRegistry = new RepositoryRegistry(schemaRegistry);

    Instance<Repository<ScimUser>> userProviderInstance = mock(Instance.class);
    Repository<ScimUser> userRepository = mock(Repository.class);

    when(userProviderInstance.get()).thenReturn(userRepository);
    repositoryRegistry.registerRepository(ScimUser.class, userRepository);
    when(userProviderInstance.get()).thenReturn(userRepository);

    ScimUser userAlice = new ScimUser();
    userAlice.setId("alice-id");

    ScimUser userBob = new ScimUser();
    userBob.setId("bob-id");

    when(userRepository.create(any(), any()))
      .thenReturn(userAlice)
      .thenThrow(new UnableToCreateResourceException(Response.Status.BAD_REQUEST, "Expected Test Exception when bob is created"));


    BulkResourceImpl impl = new BulkResourceImpl(schemaRegistry, repositoryRegistry);
    UriInfo uriInfo = mock(UriInfo.class);
    UriBuilder uriBuilder = mock(UriBuilder.class);
    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
    when(uriBuilder.path("/Users")).thenReturn(uriBuilder);
    when(uriBuilder.path("alice-id")).thenReturn(uriBuilder);
    when(uriBuilder.path("bob-id")).thenReturn(uriBuilder);
    when(uriBuilder.build())
      .thenReturn(URI.create("https://scim.example.com/Users/alice-id"))
      .thenReturn(URI.create("https://scim.example.com/Users/bob-id"));

    Response response = impl.doBulk(bulkRequest, uriInfo);
    BulkResponse bulkResponse = (BulkResponse) response.getEntity();

    assertThat(bulkResponse.getErrorResponse()).isNull();
    assertThat(bulkResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST);
    assertThat(bulkResponse.getSchemas()).containsOnly(BulkResponse.SCHEMA_URI);
    assertThat(bulkResponse.getOperations())
      .hasSize(2)
      .contains(new BulkOperation()
        .setMethod(BulkOperation.Method.POST)
        .setBulkId("bulk-id-alice")
        .setData(new ScimUser()
          .setId("alice-id"))
        .setLocation("https://scim.example.com/Users/alice-id")
        .setStatus(new BulkOperation.StatusWrapper(Response.Status.CREATED)))
      .contains(new BulkOperation()
        .setMethod(BulkOperation.Method.POST)
        .setBulkId("bulk-id-bob")
        .setResponse(new ErrorResponse(Response.Status.BAD_REQUEST, "Expected Test Exception when bob is created"))
        .setStatus(new BulkOperation.StatusWrapper(Response.Status.BAD_REQUEST)));
  }

  // ---------------------------------------------------------------------------
  // bulkMaxOperations enforcement
  // ---------------------------------------------------------------------------

  @Test
  public void testBulkMaxOperationsExceeded() throws Exception {
    // limit=2, 3 ops -> 413; repository must not be called
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    RepositoryRegistry repositoryRegistry = new RepositoryRegistry(schemaRegistry);
    Repository<ScimUser> userRepository = mock(Repository.class);
    repositoryRegistry.registerRepository(ScimUser.class, userRepository);

    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxOperations(2);

    BulkRequest bulkRequest = new BulkRequest()
      .setOperations(List.of(
        new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("a")),
        new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("b")),
        new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("c"))
      ));

    BulkResourceImpl impl = new BulkResourceImpl(schemaRegistry, repositoryRegistry, config);
    UriInfo uriInfo = mock(UriInfo.class);

    Response response = impl.doBulk(bulkRequest, uriInfo);

    assertThat(response.getStatus()).isEqualTo(Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode());
    assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
    // RFC 7644 §3.7.4: 413 states the maximum operation count and uses scimType "tooMany".
    ErrorResponse error = (ErrorResponse) response.getEntity();
    assertThat(error.getScimType()).isEqualTo(ErrorMessageType.TOO_MANY);
    assertThat(error.getDetail()).contains("2");
    verify(userRepository, never()).create(any(), any());
    verify(userRepository, never()).update(any(), any(), any());
    verify(userRepository, never()).delete(any());
  }

  @Test
  public void testBulkMaxOperationsAtLimit() throws Exception {
    // limit=2, 2 ops -> success
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    RepositoryRegistry repositoryRegistry = new RepositoryRegistry(schemaRegistry);
    Repository<ScimUser> userRepository = mock(Repository.class);
    repositoryRegistry.registerRepository(ScimUser.class, userRepository);

    ScimUser createdA = new ScimUser();
    createdA.setId("id-a");
    ScimUser createdB = new ScimUser();
    createdB.setId("id-b");
    when(userRepository.create(any(), any()))
      .thenReturn(createdA)
      .thenReturn(createdB);

    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxOperations(2);

    BulkRequest bulkRequest = new BulkRequest()
      .setOperations(List.of(
        new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setBulkId("bulk-a").setData(new ScimUser().setUserName("a")),
        new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setBulkId("bulk-b").setData(new ScimUser().setUserName("b"))
      ));

    BulkResourceImpl impl = new BulkResourceImpl(schemaRegistry, repositoryRegistry, config);
    UriInfo uriInfo = mock(UriInfo.class);
    UriBuilder uriBuilder = mock(UriBuilder.class);
    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
    when(uriBuilder.path(any(String.class))).thenReturn(uriBuilder);
    when(uriBuilder.build())
      .thenReturn(URI.create("https://scim.example.com/Users/id-a"))
      .thenReturn(URI.create("https://scim.example.com/Users/id-b"));

    Response response = impl.doBulk(bulkRequest, uriInfo);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void testBulkMaxOperationsNonDefaultConfig() throws Exception {
    // limit=5, 6 ops -> 413
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    RepositoryRegistry repositoryRegistry = new RepositoryRegistry(schemaRegistry);
    Repository<ScimUser> userRepository = mock(Repository.class);
    repositoryRegistry.registerRepository(ScimUser.class, userRepository);

    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxOperations(5);

    List<BulkOperation> ops = List.of(
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u1")),
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u2")),
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u3")),
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u4")),
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u5")),
      new BulkOperation().setMethod(BulkOperation.Method.POST).setPath("/Users").setData(new ScimUser().setUserName("u6"))
    );

    BulkRequest bulkRequest = new BulkRequest().setOperations(ops);
    BulkResourceImpl impl = new BulkResourceImpl(schemaRegistry, repositoryRegistry, config);
    UriInfo uriInfo = mock(UriInfo.class);

    Response response = impl.doBulk(bulkRequest, uriInfo);

    assertThat(response.getStatus()).isEqualTo(Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode());
    ErrorResponse error = (ErrorResponse) response.getEntity();
    assertThat(error.getScimType()).isEqualTo(ErrorMessageType.TOO_MANY);
    assertThat(error.getDetail()).contains("5");
    verify(userRepository, never()).create(any(), any());
  }
}
