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

package org.apache.directory.scim.protocol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

/**
 * From SCIM Protocol Specification, section 4, page 74
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-4">Scim spec section 4</a>
 * <p>
 *      /Schemas An HTTP GET to this endpoint is used to retrieve information
 *      about resource schemas supported by a SCIM service provider. An HTTP GET
 *      to the endpoint "/Schemas" SHALL return all supported schemas in
 *      ListResponse format (see Figure 3). Individual schema definitions can be
 *      returned by appending the schema URI to the /Schemas endpoint. For
 *      example:
 * <p>
 *      /Schemas/urn:ietf:params:scim:schemas:core:2.0:User
 * <p>
 *      The contents of each schema returned are described in Section 7 of
 *      [RFC7643]. An example representation of SCIM schemas may be found in
 *      Section 8.7 of [RFC7643].
 * <p>
 *      In cases where a request is for a specific "ResourceType" or "Schema",
 *      the single JSON object is returned in the same way that a single User or
 *      Group is retrieved, as per Section 3.4.1. When returning multiple
 *      ResourceTypes or Schemas, the message form described by the
 *      "urn:ietf:params:scim:api:messages:2.0:ListResponse" (ListResponse) form
 *      SHALL be used as shown in Figure 3 and in Figure 9 below. Query
 *      parameters described in Section 3.4.2, such as filtering, sorting, and
 *      pagination, SHALL be ignored. If a "filter" is provided, the service
 *      provider SHOULD respond with HTTP status code 403 (Forbidden) to ensure
 *      that clients cannot incorrectly assume that any matching conditions
 *      specified in a filter are true.
 */

@Path("Schemas")
@Tag(name="SCIM-Configuration")
public interface SchemaResource {

  @GET
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Get All Schemas")
  @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    array = @ArraySchema(schema = @Schema(implementation = org.apache.directory.scim.spec.schema.Schema.class))))
  default Response getAllSchemas(@QueryParam("filter") String filter, @Context UriInfo uriInfo) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @GET
  @Path("{uri}")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Get Schemas by URN")
  @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    schema = @Schema(implementation = org.apache.directory.scim.spec.schema.Schema.class)))
  default Response getSchema(@PathParam("uri") String uri, @Context UriInfo uriInfo) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
