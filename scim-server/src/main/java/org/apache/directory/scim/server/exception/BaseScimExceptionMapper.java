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

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.data.ErrorResponse;

@Slf4j
abstract class BaseScimExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

  protected abstract ErrorResponse errorResponse(E throwable);

  @Override
  public Response toResponse(E throwable) {
    Response response = errorResponse(throwable).toResponse();
    // log client errors (e.g. 404s) at debug, and anything else at warn
    if (Response.Status.Family.CLIENT_ERROR.equals(response.getStatusInfo().getFamily())) {
      log.debug("Returning error status: {}", response.getStatus(), throwable);
    } else {
      log.warn("Returning error status: {}", response.getStatus(), throwable);
    }
    response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, Constants.SCIM_CONTENT_TYPE);
    return response;
  }
}
