/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * @author shawn
 *
 */
public class UserResourceImpl extends BaseResourceTypeResourceImpl<ScimUser> implements UserResource {

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Provider<ScimUser> getProvider() {
    return providerRegistry.getProvider(ScimUser.class);
  }

}
