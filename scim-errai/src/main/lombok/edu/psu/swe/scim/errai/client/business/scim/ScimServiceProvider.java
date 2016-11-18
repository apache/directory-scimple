/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.scim;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import edu.psu.swe.scim.errai.client.business.common.ListResponse;
import edu.psu.swe.scim.errai.client.business.resourcetype.ResourceType;
import edu.psu.swe.scim.errai.client.business.schema.Schema;

/**
 * Defines the REST end-points needed to retrieve the SCIM ResourceTypes
 * and SCIM Schemas from the server.  See sections 6 and 7 respectively
 * in the SCIM Schema Specification at:
 * 
 * https://tools.ietf.org/html/rfc7643#section-6
 * https://tools.ietf.org/html/rfc7643#section-7
 *
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Path("/v2")
public interface ScimServiceProvider {
  
  @Path("ResourceTypes")
  @GET
  ListResponse<ResourceType> getResourceTypes();
  
  @Path("Schemas")
  @GET
  ListResponse<Schema> getSchemas();

}
