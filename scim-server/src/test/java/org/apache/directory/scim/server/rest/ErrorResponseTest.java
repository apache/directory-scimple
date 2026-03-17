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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.server.exception.GenericExceptionMapper;
import org.apache.directory.scim.server.exception.ResourceExceptionMapper;
import org.apache.directory.scim.server.exception.ScimExceptionMapper;
import org.apache.directory.scim.spec.exception.ConflictResourceException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void getById_resourceNotFound_errorResponseFormat() throws Exception {
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    @SuppressWarnings("unchecked")
    Repository<ScimUser> repository = mock(Repository.class);
    UriInfo uriInfo = mock(UriInfo.class);
    @SuppressWarnings("unchecked")
    MultivaluedMap<String, String> queryParams = mock(MultivaluedMap.class);
    baseResourceImpl.uriInfo = uriInfo;

    when(uriInfo.getQueryParameters()).thenReturn(queryParams);
    when(queryParams.getFirst("filter")).thenReturn(null);
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();
    when(baseResourceImpl.getRepository()).thenReturn(repository);
    when(repository.get(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);
    when(baseResourceImpl.getById("nonexistent", null, null)).thenCallRealMethod();

    ScimException exception = catchThrowableOfType(ScimException.class, () -> baseResourceImpl.getById("nonexistent", null, null));
    assertThat(exception).isNotNull();

    ScimExceptionMapper mapper = new ScimExceptionMapper();
    Response response = mapper.toResponse(exception);

    assertScimErrorResponse(response, 404, null, "Resource nonexistent not found");
  }

  @Test
  void create_conflict_errorResponseFormat() throws Exception {
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    @SuppressWarnings("unchecked")
    Repository<ScimUser> repository = mock(Repository.class);
    ScimUser user = new ScimUser();

    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();
    when(baseResourceImpl.getRepository()).thenReturn(repository);
    when(repository.create(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenThrow(new ConflictResourceException("email already exists"));
    when(baseResourceImpl.create(user, null, null)).thenCallRealMethod();

    ResourceException exception = catchThrowableOfType(ResourceException.class, () -> baseResourceImpl.create(user, null, null));
    assertThat(exception).isNotNull();

    ResourceExceptionMapper mapper = new ResourceExceptionMapper();
    Response response = mapper.toResponse(exception);

    assertScimErrorResponse(response, 409, ErrorMessageType.UNIQUENESS, "email already exists");
  }

  @Test
  void repositoryNotDefined_errorResponseFormat() throws ScimException {
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    when(baseResourceImpl.getRepository()).thenReturn(null);
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();

    ScimException exception = catchThrowableOfType(ScimException.class, baseResourceImpl::getRepositoryInternal);
    assertThat(exception).isNotNull();

    ScimExceptionMapper mapper = new ScimExceptionMapper();
    Response response = mapper.toResponse(exception);

    assertScimErrorResponse(response, 501, null, "Provider not defined");
  }

  @Test
  void create_bothAttributes_errorResponseFormat() throws Exception {
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    ScimUser user = new ScimUser();
    AttributeReferenceListWrapper included = new AttributeReferenceListWrapper("name.givenName");
    AttributeReferenceListWrapper excluded = new AttributeReferenceListWrapper("emails");

    when(baseResourceImpl.create(user, included, excluded)).thenCallRealMethod();

    ScimException exception = catchThrowableOfType(ScimException.class, () -> baseResourceImpl.create(user, included, excluded));
    assertThat(exception).isNotNull();

    ScimExceptionMapper mapper = new ScimExceptionMapper();
    Response response = mapper.toResponse(exception);

    assertScimErrorResponse(response, 400, null, "Cannot include both attributes and excluded attributes in a single request");
  }

  @Test
  void unexpectedException_errorResponseFormat() throws Exception {
    @SuppressWarnings("unchecked")
    BaseResourceTypeResourceImpl<ScimUser> baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    @SuppressWarnings("unchecked")
    Repository<ScimUser> repository = mock(Repository.class);
    ScimUser user = new ScimUser();

    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();
    when(baseResourceImpl.getRepository()).thenReturn(repository);
    when(repository.create(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenThrow(new RuntimeException("unexpected database error"));
    when(baseResourceImpl.create(user, null, null)).thenCallRealMethod();

    RuntimeException exception = catchThrowableOfType(RuntimeException.class, () -> baseResourceImpl.create(user, null, null));
    assertThat(exception).isNotNull();

    GenericExceptionMapper mapper = new GenericExceptionMapper();
    Response response = mapper.toResponse(exception);

    assertScimErrorResponse(response, 500, null, "unexpected database error");
  }

  @Test
  void errorResponse_schemaUriPresent() throws ScimException {
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    when(baseResourceImpl.getRepository()).thenReturn(null);
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();

    ScimException exception = catchThrowableOfType(ScimException.class, baseResourceImpl::getRepositoryInternal);
    assertThat(exception).isNotNull();

    try (Response response = new ScimExceptionMapper().toResponse(exception)) {
      ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
      assertThat(errorResponse.getSchemas()).contains("urn:ietf:params:scim:api:messages:2.0:Error");
    }
  }

  @Test
  void errorResponse_statusFieldMatchesHttpStatus() throws ScimException {
    @SuppressWarnings("rawtypes")
    BaseResourceTypeResourceImpl baseResourceImpl = mock(BaseResourceTypeResourceImpl.class);
    when(baseResourceImpl.getRepository()).thenReturn(null);
    when(baseResourceImpl.getRepositoryInternal()).thenCallRealMethod();

    ScimException exception = catchThrowableOfType(ScimException.class, baseResourceImpl::getRepositoryInternal);
    assertThat(exception).isNotNull();

    try (Response response = new ScimExceptionMapper().toResponse(exception)) {
      ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
      assertThat(response.getStatus()).isEqualTo(errorResponse.getStatus().getStatusCode());
    }
  }

  private void assertScimErrorResponse(Response response, int expectedStatus, ErrorMessageType expectedScimType, String expectedDetailSubstring) {
    assertThat(response.getStatus()).isEqualTo(expectedStatus);
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(Constants.SCIM_CONTENT_TYPE);

    Object entity = response.getEntity();
    assertThat(entity).isInstanceOf(ErrorResponse.class);

    ErrorResponse errorResponse = (ErrorResponse) entity;
    assertThat(errorResponse.getSchemas()).contains(ErrorResponse.SCHEMA_URI);
    assertThat(errorResponse.getStatus()).isEqualTo(Status.fromStatusCode(expectedStatus));
    assertThat(errorResponse.getScimType()).isEqualTo(expectedScimType);
    assertThat(errorResponse.getDetail()).isEqualTo(expectedDetailSubstring);
  }
}
