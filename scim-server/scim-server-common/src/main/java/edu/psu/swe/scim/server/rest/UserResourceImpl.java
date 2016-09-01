/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * @author shawn
 *
 */
@Stateless
public class UserResourceImpl extends BaseResourceTypeResourceImpl<ScimUser> implements UserResource {

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Provider<ScimUser> getProvider() {
    return providerRegistry.getProvider(ScimUser.class);
  }

}
