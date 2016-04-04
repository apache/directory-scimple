package edu.psu.swe.scim.spec.protocol;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;

@Api("ResourceType")
public interface BaseResourceTypeResource<T> {
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec, retrieving known resources</a>
   * @return
   */
  @GET
  @Path("{id}")
  @Produces("application/scim+json")
  @ApiOperation(value="Find by id", response=ScimResource.class, code=200)
  @ApiResponses(value={
                  @ApiResponse(code=400, message="Bad Request"),
                  @ApiResponse(code=404, message="Not found"),
                  @ApiResponse(code=500, message="Internal Server Error"),
                  @ApiResponse(code=501, message="Not Implemented")
                })
    default Response getById(@ApiParam(value="id", required=true) @PathParam("id") String id, 
                             @ApiParam(value="attributes", required=false) @QueryParam("attributes") String attributes) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec, query resources</a>
   * @return
   */
  @GET
  @Produces("application/scim+json")
  @ApiOperation(value="Find by a combination of query parameters", response=ScimResource.class, responseContainer="List", code=200)
  @ApiResponses(value={
                  @ApiResponse(code=400, message="Bad Request"),
                  @ApiResponse(code=404, message="Not found"),
                  @ApiResponse(code=500, message="Internal Server Error"),
                  @ApiResponse(code=501, message="Not Implemented")
                })
  default Response query(@ApiParam(value="attributes", required=false) @QueryParam("attributes") String attributes, 
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
  @ApiOperation(value="Create", response=ScimResource.class, code=201)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=409, message=ErrorMessageConstants.UNIQUENESS),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response create(T resource) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec, query with post</a>
   * @return
   */
  @POST
  @Path("/.search")
  @Produces("application/scim+json")
  @ApiOperation(value="Search", response=ScimResource.class, responseContainer="List", code=200)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response find(SearchRequest request){
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec, update</a>
   * @return
   */
  @PUT
  @Consumes("application/scim+json")
  @Produces("application/scim+json")
  @ApiOperation(value="Update", response=ScimResource.class, code=200)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response update(T resource) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  @PATCH
  @Consumes("application/scim+json")
  @Produces("application/scim+json")
  @ApiOperation(value="Patch a portion of the backing store", code=204)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=404, message="Not found"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default public Response patch() {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
  @DELETE
  @Path("{id}")
  @ApiOperation(value="Delete from the backing store", code=204)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=404, message="Not found"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default public Response delete(@ApiParam(value="id", required=true) @PathParam("id") String id){
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
