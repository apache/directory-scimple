/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.GroupResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.ErrorResponse;

/**
 * @author shawn
 *
 */
public class GroupResourceImpl extends BaseResourceTypeResourceImpl<ScimGroup> implements GroupResource {

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Provider<ScimGroup> getProvider() {
    return providerRegistry.getProvider(ScimGroup.class);
  }
  
}
