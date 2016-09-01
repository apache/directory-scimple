package edu.psu.swe.scim.spec.protocol;

import io.swagger.annotations.Api;

import javax.ws.rs.Path;

import edu.psu.swe.scim.spec.resources.ScimUser;

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

@Path("Me")
@Api("ResourceType")
public interface SelfResource extends BaseResourceTypeResource<ScimUser> {
  
}
