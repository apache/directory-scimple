package edu.psu.swe.scim.spec.protocol;

import io.swagger.annotations.Api;

import javax.ws.rs.Path;

//@formatter:off
/**
* From SCIM Protocol Specification, section 3, page 9
* 
* @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
* 
* Resource Endpoint         Operations             Description
 -------- ---------------- ---------------------- --------------------
   Bulk     /Bulk            POST (Section 3.7)     Bulk updates to one
                                                    or more resources.

* @author chrisharm
*
*/
//@formatter:on

@Path("Me")
@Api("ResourceType")
public interface BulkResource {

}
