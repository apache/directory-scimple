package edu.psu.swe.scim.spec.protocol;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;

//@formatter:off
/**
 * From SCIM Protocol Specification, section 3, page 9
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
 * 
 * Resource Endpoint         Operations             Description
   -------- ---------------- ---------------------- --------------------
   Self     /Me              GET, POST, PUT, PATCH, Alias for operations
                             DELETE (Section 3.11)  against a resource
                                                    mapped to an
                                                    authenticated
                                                    subject (e.g.,
                                                    User).

 * @author chrisharm
 *
 */
//@formatter:on

@Path(SelfResource.PATH)
@Api("SCIM")
public interface SelfResource {

  public static final String PATH = "Me";

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec,
   *      retrieving known resources</a>
   * @return
   * @throws UnableToRetrieveResourceException 
   */
  @GET
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value="Get self record", produces=Constants.SCIM_CONTENT_TYPE, response=ScimResource.class, code=200)
  @ApiResponses(value={
                  @ApiResponse(code=400, message="Bad Request"),
                  @ApiResponse(code=404, message="Not found"),
                  @ApiResponse(code=500, message="Internal Server Error"),
                  @ApiResponse(code=501, message="Not Implemented")
                })
    default Response getSelf(@ApiParam(value="attributes", required=false) @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                             @ApiParam(value="excludedAttributes", required=false) @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws Exception {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec,
   *      query resources</a>
   * @return
   */
  @POST
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value = "Create self record", produces=Constants.SCIM_CONTENT_TYPE, consumes=Constants.SCIM_CONTENT_TYPE, response = ScimResource.class, code = 201)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 409, message = "Conflict"), @ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 501, message = "Not Implemented") })
  default Response create(ScimUser resource,
                          @ApiParam(value="attributes", required=false) @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @ApiParam(value="excludedAttributes", required=false) @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws Exception {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec,
   *      update</a>
   * @return
   */
  @PUT
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value = "Update self record", produces=Constants.SCIM_CONTENT_TYPE, consumes=Constants.SCIM_CONTENT_TYPE, response = ScimResource.class, code = 200)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 501, message = "Not Implemented") })
  default Response update(ScimUser resource, 
                          @ApiParam(value="attributes", required=false) @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                          @ApiParam(value="excludedAttributes", required=false) @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws Exception {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @PATCH
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @ApiOperation(value = "Patch a portion of the backing store", produces=Constants.SCIM_CONTENT_TYPE, consumes=Constants.SCIM_CONTENT_TYPE, code = 204)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 501, message = "Not Implemented") })
  default Response patch(PatchRequest patchRequest,
                         @ApiParam(value="attributes", required=false) @QueryParam("attributes") AttributeReferenceListWrapper attributes,
                         @ApiParam(value="excludedAttributes", required=false) @QueryParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws Exception {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @DELETE
  @ApiOperation(value = "Delete self record", code = 204)
  @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 501, message = "Not Implemented") })
  default Response delete() throws Exception {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
