package edu.psu.swe.scim.api.protocol;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//@formatter:off
/**
 * From SCIM Protocol Specification, section 3, page 9
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
 * 
 * Resource Endpoint         Operations             Description
   -------- ---------------- ---------------------- --------------------
   User     /Users           GET (Section 3.4.1),   Retrieve, add,
                             POST (Section 3.3),    modify Users.
                             PUT (Section 3.5.1),
                             PATCH (Section 3.5.2),
                             DELETE (Section 3.6)

 * @author shawn
 *
 */
//@formatter:on

@Path("Users")
public interface UserResource {
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec, retrieving known resources</a>
   * @return
   */
  @GET
  @Path("{id}")
  default Response getUser(@PathParam("id") String id, @QueryParam("attributes") String attributes) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec, query resources</a>
   * @return
   */
  @GET
  default Response queryForUsers(@QueryParam("attributes") String attributes, 
                                 @QueryParam("filter") String filter,
                                 @QueryParam("sortBy") String sortBy,
                                 @QueryParam("sortOrder") String sortOrder,
                                 @QueryParam("startIndex") Integer startIndex,
                                 @QueryParam("count") Integer count) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
}
