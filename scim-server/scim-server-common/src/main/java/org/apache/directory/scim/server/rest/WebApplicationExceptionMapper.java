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

import org.apache.directory.scim.spec.protocol.Constants;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;

import javax.enterprise.inject.Specializes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;


@Provider
@Specializes
@Produces(Constants.SCIM_CONTENT_TYPE)
public class WebApplicationExceptionMapper extends edu.psu.swe.commons.jaxrs.exceptions.mappers.WebApplicationExceptionMapper {

  public Response toResponse(WebApplicationException e) {
    ErrorResponse em = new ErrorResponse(Status.fromStatusCode(e.getResponse().getStatus()), e.getMessage());

    Response response = em.toResponse();
    response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, Constants.SCIM_CONTENT_TYPE);

    return response;
  }
}
