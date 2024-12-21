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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;

import static jakarta.ws.rs.core.MediaType.*;
import static org.apache.directory.scim.protocol.Constants.SCIM_CONTENT_TYPE;

//@formatter:off
/**
 * From SCIM Protocol Specification, section 3, page 9
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
 * <p>
 * Resource Endpoint         Operations             Description
   -------- ---------------- ---------------------- --------------------
   Self     /Me              GET, POST, PUT, PATCH, Alias for operations
                             DELETE (Section 3.11)  against a resource
                                                    mapped to an
                                                    authenticated
                                                    subject (e.g.,
                                                    User).

 * @author chrisharm
 *
 */
//@formatter:on

@Path(SelfResource.PATH)
@Tag(name="SCIM")
public interface SelfResource {

  String PATH = "Me";

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec,
   *      retrieving known resources</a>
   * @return
   * @throws ResourceException
   */
  @GET
  @Produces({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Operation(description="Get self record")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode="400", description="Bad Request"),
    @ApiResponse(responseCode="404", description="Not found"),
    @ApiResponse(responseCode="500", description="Internal Server Error"),
    @ApiResponse(responseCode="501", description="Not Implemented")
  })
    default Response getSelf(@Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                             @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec,
   *      query resources</a>
   * @return
   */
  @POST
  @Consumes({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Produces({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Operation(description = "Create self record")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201",
                 content = @Content(mediaType = SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "409", description = "Conflict"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response create(@RequestBody(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimResource.class)),
                                       required = true) ScimUser resource,
                          @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec,
   *      update</a>
   * @return
   */
  @PUT
  @Consumes({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Produces({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Operation(description = "Update self record")
  @ApiResponses(value = {
    @ApiResponse(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response update(@RequestBody(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimUser.class)),
                                       required = true) ScimUser resource,
                          @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @PATCH
  @Consumes({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Produces({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Operation(description = "Patch a portion of the backing store")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204",
                 content = @Content(mediaType = SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response patch(@RequestBody(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                                      schema = @Schema(implementation = PatchRequest.class)),
                                      required = true) PatchRequest patchRequest,
                         @Parameter(name="attributes") @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                         @Parameter(name="excludedAttributes") @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @DELETE
  @Operation(description = "Delete self record")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default Response delete() throws ScimException, ResourceException {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
