package edu.psu.swe.scim.spec.protocol;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
@Api("Users")
public interface UserResource {
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec, retrieving known resources</a>
   * @return
   */
  @GET
  @Path("{id}")
  @Produces("application/scim+json")
  @ApiOperation(value="Find by user id", response=ScimUser.class, code=200)
  @ApiResponses(value={
                  @ApiResponse(code=400, message="Bad Request"),
                  @ApiResponse(code=404, message="Not found"),
                  @ApiResponse(code=500, message="Internal Server Error"),
                  @ApiResponse(code=501, message="Not Implemented")
                })
  default Response getUser(@ApiParam(value="id", required=true) @PathParam("id") String id, 
                           @ApiParam(value="attributes", required=false) @QueryParam("attributes") String attributes) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec, query resources</a>
   * @return
   */
  @GET
  @Produces("application/scim+json")
  @ApiOperation(value="Find users by a combination of query parameters", response=ScimUser.class, responseContainer="List", code=200)
  @ApiResponses(value={
                  @ApiResponse(code=400, message="Bad Request"),
                  @ApiResponse(code=404, message="Not found"),
                  @ApiResponse(code=500, message="Internal Server Error"),
                  @ApiResponse(code=501, message="Not Implemented")
                })
  default Response queryForUsers(@ApiParam(value="attributes", required=false) @QueryParam("attributes") String attributes, 
                                 @ApiParam(value="filter", required=false) @QueryParam("filter") String filter,
                                 @ApiParam(value="sortBy", required=false) @QueryParam("sortBy") String sortBy,
                                 @ApiParam(value="sortOrder", required=false) @QueryParam("sortOrder") String sortOrder,
                                 @ApiParam(value="startIndex", required=false) @QueryParam("startIndex") Integer startIndex,
                                 @ApiParam(value="count", required=false) @QueryParam("count") Integer count) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec, query resources</a>
   * @return
   */
  @POST
  @Consumes("application/scim+json")
  @Produces("application/scim+json")
  @ApiOperation(value="Create a user", response=ScimUser.class, code=201)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=409, message=ErrorMessageConstants.UNIQUENESS),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response creatUser(ScimUser user) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec, query with post</a>
   * @return
   */
  @POST
  @Path("/.search")
  @Produces("application/scim+json")
  @ApiOperation(value="Search for users", response=ScimUser.class, responseContainer="List", code=200)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response findUsers(SearchRequest request){
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
