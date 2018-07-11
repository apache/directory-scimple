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

package org.apache.directory.scim.spec.protocol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@formatter:off
/**
* From SCIM Protocol Specification, section 4, page 73
* 
* @see <a href="https://tools.ietf.org/html/rfc7644#section-4">Scim spec section 4</a>
* 
* /ServiceProviderConfig
*      An HTTP GET to this endpoint will return a JSON structure that
*      describes the SCIM specification features available on a service
*      provider.  This endpoint SHALL return responses with a JSON object
*      using a "schemas" attribute of
*      "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig".
*      The attributes returned in the JSON object are defined in
*      Section 5 of [RFC7643].  An example representation of SCIM service
*      provider configuration may be found in Section 8.5 of [RFC7643].
*
* @author chrisharm
*
*/
//@formatter:on

@Path("ServiceProviderConfig")
@Api("SCIM-Configuration")
public interface ServiceProviderConfigResource {

  @GET
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value="Get Service Provider Configuration", produces=Constants.SCIM_CONTENT_TYPE)
  default public Response getServiceProviderConfiguration(@Context UriInfo context) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
}
