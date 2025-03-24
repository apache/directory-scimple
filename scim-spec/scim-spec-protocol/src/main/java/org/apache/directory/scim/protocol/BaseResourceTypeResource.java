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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.protocol.adapter.FilterWrapper;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.resources.ScimResource;

@Tag(name="SCIM")
@Hidden
public interface BaseResourceTypeResource<T> {

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec,
   *      retrieving known resources</a>
   * @return
   * @throws ScimException
   * @throws ResourceException
   */
  @GET
  @Path("{id}")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Find by id")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode="400", description="Bad Request"),
    @ApiResponse(responseCode="404", description="Not found"),
    @ApiResponse(responseCode="500", description="Internal Server Error"),
    @ApiResponse(responseCode="501", description="Not Implemented")
  })
    default Response getById(@Parameter(name="id", required=true) @PathParam("id") String id,
                             @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                             @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec,
   *      query resources</a>
   * @return
   */
  @GET
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Find by a combination of query parameters")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode="400", description="Bad Request"),
    @ApiResponse(responseCode="404", description="Not found"),
    @ApiResponse(responseCode="500", description="Internal Server Error"),
    @ApiResponse(responseCode="501", description="Not Implemented")
  })
  default Response query(@Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                         @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes,
                         @Parameter(name="filter") @QueryParam("filter") FilterWrapper filterWrapper,
                         @Parameter(name="sortBy") @QueryParam("sortBy") AttributeReference sortBy,
                         @Parameter(name="sortOrder") @QueryParam("sortOrder") SortOrder sortOrder,
                         @Parameter(name="startIndex") @QueryParam("startIndex") Integer startIndex,
                         @Parameter(name="count") @QueryParam("count") Integer count) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec,
   *      query resources</a>
   * @return
   */
  @POST
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Create")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201",
                 content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "409", description = "Conflict"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response create(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimResource.class)),
                                       required = true) T resource,
                          @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec,
   *      query with post</a>
   * @return
   */
  @POST
  @Path("/.search")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Search")
  @ApiResponses(value = {
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response find(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                     schema = @Schema(implementation = SearchRequest.class)),
                                     required = true) SearchRequest request) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec,
   *      update</a>
   * @return
   */
  @PUT
  @Path("{id}")
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Update")
  @ApiResponses(value = {
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response update(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimResource.class)),
                                       required = true) T resource,
                          @PathParam("id") String id,
                          @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @PATCH
  @Path("{id}")
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Patch a portion of the backing store")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response patch(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                      schema = @Schema(implementation = PatchRequest.class)),
                                      required = true) PatchRequest patchRequest,
                         @PathParam("id") String id,
                         @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                         @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @DELETE
  @Path("{id}")
  @Operation(description = "Delete from the backing store")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response delete(@Parameter(name = "id", required = true) @PathParam("id") String id) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
