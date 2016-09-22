package edu.psu.swe.scim.server.provider.extensions;

import edu.psu.swe.scim.server.provider.extensions.exceptions.ClientFilterException;
import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimResource;

public interface AttributeFilterExtension extends ProcessingExtension {

  BaseResource filterAttributes(ScimResource baseResource, ScimRequestContext scimRequestContext) throws ClientFilterException;
  
}
