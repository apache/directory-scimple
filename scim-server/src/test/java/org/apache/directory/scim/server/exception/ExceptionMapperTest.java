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

package org.apache.directory.scim.server.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.exception.MutabilityException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.UnsupportedFilterException;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ExceptionMapperTest {

  @Test
  void scimExceptionMapper_preservesErrorResponse() {
    ScimExceptionMapper mapper = new ScimExceptionMapper();
    ScimException exception = new ScimException(Status.BAD_REQUEST, "test detail");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 400, null, "test detail");
  }

  @Test
  void scimExceptionMapper_preservesScimType() {
    ScimExceptionMapper mapper = new ScimExceptionMapper();
    ErrorResponse error = new ErrorResponse(Status.BAD_REQUEST, "syntax error");
    error.setScimType(ErrorMessageType.INVALID_SYNTAX);
    ScimException exception = new ScimException(error, Status.BAD_REQUEST);

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 400, ErrorMessageType.INVALID_SYNTAX, "syntax error");
  }

  @Test
  void resourceExceptionMapper_notFound() {
    ResourceExceptionMapper mapper = new ResourceExceptionMapper();
    ResourceException exception = new ResourceException(404, "not found");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 404, null, "not found");
  }

  @Test
  void resourceExceptionMapper_conflict_setsUniqueness() {
    ResourceExceptionMapper mapper = new ResourceExceptionMapper();
    ResourceException exception = new ResourceException(409, "email exists");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 409, ErrorMessageType.UNIQUENESS, "email exists");
  }

  @Test
  void resourceExceptionMapper_conflict_nullMessage_usesDefault() {
    ResourceExceptionMapper mapper = new ResourceExceptionMapper();
    ResourceException exception = new ResourceException(409, null);

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 409, ErrorMessageType.UNIQUENESS, ErrorMessageType.UNIQUENESS.getDetail());
  }

  @Test
  void resourceExceptionMapper_otherStatus_noScimType() {
    ResourceExceptionMapper mapper = new ResourceExceptionMapper();
    ResourceException exception = new ResourceException(403, "forbidden");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 403, null, "forbidden");
  }

  @Test
  void filterParseExceptionMapper_returnsInvalidFilter() {
    FilterParseExceptionMapper mapper = new FilterParseExceptionMapper();
    FilterParseException exception = new FilterParseException("bad filter");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 400, ErrorMessageType.INVALID_FILTER, "bad filter");
  }

  @Test
  void unsupportedFilterExceptionMapper_returnsInvalidFilter() {
    UnsupportedFilterExceptionMapper mapper = new UnsupportedFilterExceptionMapper();
    UnsupportedFilterException exception = new UnsupportedFilterException("unsupported");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 400, ErrorMessageType.INVALID_FILTER, ErrorMessageType.INVALID_FILTER.getDetail());
  }

  @Test
  void mutabilityExceptionMapper_shouldHandleMutabilityException() {
    MutabilityExceptionMapper mapper = new MutabilityExceptionMapper();
    MutabilityException exception = new MutabilityException("cannot modify immutable attribute");
    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 400, ErrorMessageType.MUTABILITY, "cannot modify immutable attribute");
  }

  @Test
  void webApplicationExceptionMapper_preservesStatus() {
    WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
    WebApplicationException exception = new WebApplicationException("not allowed", Status.METHOD_NOT_ALLOWED);

    try (Response response = mapper.toResponse(exception)) {

      assertThat(response.getStatus()).isEqualTo(405);
      assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(Constants.SCIM_CONTENT_TYPE);

      ErrorResponse entity = (ErrorResponse) response.getEntity();
      assertThat(entity.getSchemas()).contains(ErrorResponse.SCHEMA_URI);
      assertThat(entity.getStatus()).isEqualTo(Status.METHOD_NOT_ALLOWED);
      assertThat(entity.getScimType()).isNull();
      assertThat(entity.getDetail()).isNotNull();
    }
  }

  @Test
  void unsupportedOperationExceptionMapper_returns501() {
    UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    UnsupportedOperationException exception = new UnsupportedOperationException("not impl");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 501, null, "not impl");
  }

  @Test
  void genericExceptionMapper_returns500() {
    GenericExceptionMapper mapper = new GenericExceptionMapper();
    RuntimeException exception = new RuntimeException("unexpected");

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 500, null, "unexpected");
  }

  @Test
  void genericExceptionMapper_nullMessage() {
    GenericExceptionMapper mapper = new GenericExceptionMapper();
    RuntimeException exception = new RuntimeException();

    Response response = mapper.toResponse(exception);
    assertScimErrorResponse(response, 500, null, null);
  }

  @Test
  void allMappers_contentTypeIsScimJson() {
    assertContentType(new ScimExceptionMapper().toResponse(new ScimException(Status.BAD_REQUEST, "test")));
    assertContentType(new ResourceExceptionMapper().toResponse(new ResourceException(404, "not found")));
    assertContentType(new FilterParseExceptionMapper().toResponse(new FilterParseException("bad")));
    assertContentType(new UnsupportedFilterExceptionMapper().toResponse(new UnsupportedFilterException("unsupported")));
    assertContentType(new MutabilityExceptionMapper().toResponse(new MutabilityException("immutable")));
    assertContentType(new WebApplicationExceptionMapper().toResponse(new WebApplicationException("err", Status.BAD_REQUEST)));
    assertContentType(new UnsupportedOperationExceptionMapper().toResponse(new UnsupportedOperationException("nope")));
    assertContentType(new GenericExceptionMapper().toResponse(new RuntimeException("fail")));
  }

  private void assertContentType(Response response) {
    assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE))
      .isEqualTo(Constants.SCIM_CONTENT_TYPE);
  }

  private void assertScimErrorResponse(Response response, int expectedStatus, ErrorMessageType expectedScimType, String expectedDetail) {
    assertContentType(response);
    assertThat(response.getStatus()).isEqualTo(expectedStatus);

    Object entity = response.getEntity();
    assertThat(entity).isInstanceOf(ErrorResponse.class);

    ErrorResponse errorResponse = (ErrorResponse) entity;
    assertThat(errorResponse.getSchemas()).containsExactly(ErrorResponse.SCHEMA_URI);
    assertThat(errorResponse.getStatus()).isEqualTo(Status.fromStatusCode(expectedStatus));
    assertThat(errorResponse.getScimType()).isEqualTo(expectedScimType);
    assertThat(errorResponse.getDetail()).isEqualTo(expectedDetail);
  }
}
