package edu.psu.swe.scim.spec.protocol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * From SCIM Protocol Specification, section 4, page 74
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-4">Scim spec
 *      section 4</a>
 * 
 *      /ResourceTypes An HTTP GET to this endpoint is used to discover the
 *      types of resources available on a SCIM service provider (e.g., Users and
 *      Groups). Each resource type defines the endpoints, the core schema URI
 *      that defines the resource, and any supported schema extensions. The
 *      attributes defining a resource type can be found in Section 6 of
 *      [RFC7643], and an example representation can be found in Section 8.6 of
 *      [RFC7643].
 * 
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

@Path("ResourceTypes")
@Api("Configuration")
public interface ResourceTypesResource {

  @GET
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value = "Get All Resource Types", produces=Constants.SCIM_CONTENT_TYPE)
  default public Response getAllResourceTypes(@QueryParam("filter") String filter) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }

    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @GET
  @Path("{name}")
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value = "Get Resource Type by URN", produces=Constants.SCIM_CONTENT_TYPE)
  default public Response getResourceType(@PathParam("name") String name) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
