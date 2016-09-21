package edu.psu.swe.scim.spec.protocol;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;

//@formatter:off
/**
* From SCIM Protocol Specification, section 3, page 9
* 
* @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
* 
* Resource Endpoint         Operations             Description
 -------- ---------------- ---------------------- --------------------
   Search   [prefix]/.search POST (Section 3.4.3)   Search from system
                                                    root or within a
                                                    resource endpoint
                                                    for one or more
                                                    resource types using
                                                    POST.

* @author chrisharm
*
*/
//@formatter:on


@Path(".search")
@Api("SCIM")
public interface SearchResource {

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec, query with post</a>
   * @return
   */
  @POST
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value="Search", produces=Constants.SCIM_CONTENT_TYPE, response=ScimResource.class, responseContainer="List", code=200)
  @ApiResponses(value={
      @ApiResponse(code=400, message="Bad Request"),
      @ApiResponse(code=500, message="Internal Server Error"),
      @ApiResponse(code=501, message="Not Implemented")
    })
  default Response find(SearchRequest request){
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
  
}
