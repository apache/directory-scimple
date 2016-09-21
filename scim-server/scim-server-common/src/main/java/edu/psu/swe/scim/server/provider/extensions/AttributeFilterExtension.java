package edu.psu.swe.scim.server.provider.extensions;

import edu.psu.swe.scim.spec.resources.BaseResource;

public interface AttributeFilterExtension extends ProcessingExtension {

  BaseResource filterAttributes(BaseResource baseResource, ScimRequestContext scimRequestContext);
  
}
