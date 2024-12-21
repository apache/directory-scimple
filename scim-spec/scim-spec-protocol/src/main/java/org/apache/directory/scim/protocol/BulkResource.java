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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.directory.scim.protocol.data.BulkRequest;
import org.apache.directory.scim.protocol.data.BulkResponse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.directory.scim.protocol.Constants.SCIM_CONTENT_TYPE;

//@formatter:off
/**
* From SCIM Protocol Specification, section 3, page 9
* 
* @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
* <p>
* Resource Endpoint         Operations             Description
* -------- ---------------- ---------------------- --------------------
*    Bulk     /Bulk            POST (Section 3.7)     Bulk updates to one
*                                                     or more resources.
* </pre></code>
* @author chrisharm
*
*/
//@formatter:on

@Path("Bulk")
@Tag(name="SCIM")
public interface BulkResource {

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.7">Bulk Operations</a>
   * @return
   */
  @POST
  @Produces({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Consumes({SCIM_CONTENT_TYPE, APPLICATION_JSON})
  @Operation(description="Bulk Operations")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = SCIM_CONTENT_TYPE, schema = @Schema(implementation = BulkResponse.class))),
      @ApiResponse(responseCode="400", description="Bad Request"),
      @ApiResponse(responseCode="500", description="Internal Server Error"),
      @ApiResponse(responseCode="501", description="Not Implemented")
    })
  default Response doBulk(@RequestBody(content = @Content(mediaType = SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = BulkRequest.class)),
                                       required = true) BulkRequest bulkRequest,
                          @Context UriInfo uriInfo) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
}
