package edu.psu.swe.scim.api.protocol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/*
From  SCIM Protocol Specification, section 4, page 74

/ResourceTypes
      An HTTP GET to this endpoint is used to discover the types of
      resources available on a SCIM service provider (e.g., Users and
      Groups).  Each resource type defines the endpoints, the core
      schema URI that defines the resource, and any supported schema
      extensions.  The attributes defining a resource type can be found
      in Section 6 of [RFC7643], and an example representation can be
      found in Section 8.6 of [RFC7643].
*/

@Path("ResourceTypes")
public class ResourceTypesResource {

  @GET
  public Response getResourceTypes() {
    
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
