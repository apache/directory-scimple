/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.GroupResource;
import edu.psu.swe.scim.spec.resources.ScimGroup;

public class GroupResourceImpl extends BaseResourceTypeResourceImpl<ScimGroup> implements GroupResource {

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Provider<ScimGroup> getProvider() {
    return providerRegistry.getProvider(ScimGroup.class);
  }
  
}
