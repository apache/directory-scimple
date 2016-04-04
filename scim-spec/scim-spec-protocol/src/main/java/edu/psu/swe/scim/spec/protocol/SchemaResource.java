package edu.psu.swe.scim.spec.protocol;

import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * From SCIM Protocol Specification, section 4, page 74
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-4">Scim spec section 4</a>
 * 
 *      /Schemas An HTTP GET to this endpoint is used to retrieve information
 *      about resource schemas supported by a SCIM service provider. An HTTP GET
 *      to the endpoint "/Schemas" SHALL return all supported schemas in
 *      ListResponse format (see Figure 3). Individual schema definitions can be
 *      returned by appending the schema URI to the /Schemas endpoint. For
 *      example:
 * 
 *      /Schemas/urn:ietf:params:scim:schemas:core:2.0:User
 * 
 *      The contents of each schema returned are described in Section 7 of
 *      [RFC7643]. An example representation of SCIM schemas may be found in
 *      Section 8.7 of [RFC7643].
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

@Path("Schemas")
@Api("Configuration")
public interface SchemaResource {

  @GET
  @Produces("application/scim+json")
  default public Response getAllSchemas(@QueryParam("filter") String filter) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @GET
  @Produces("application/scim+json")
  @Path("{uri}")
  default public Response getSchema(@PathParam("uri") String uri) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
