package edu.psu.swe.scim.api.protocol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/*
From  SCIM Protocol Specification, section 4, page 74

/Schemas
An HTTP GET to this endpoint is used to retrieve information about
resource schemas supported by a SCIM service provider.  An HTTP
GET to the endpoint "/Schemas" SHALL return all supported schemas
in ListResponse format (see Figure 3).  Individual schema
definitions can be returned by appending the schema URI to the
/Schemas endpoint.  For example:

      /Schemas/urn:ietf:params:scim:schemas:core:2.0:User

The contents of each schema returned are described in Section 7 of
[RFC7643].  An example representation of SCIM schemas may be found
in Section 8.7 of [RFC7643].

*/

@Path("Schemas")
public class SchemaResource {

  @GET
  @Path("{uri}")
  public Response getSchema(@PathParam("uri") String uri) {
    
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
