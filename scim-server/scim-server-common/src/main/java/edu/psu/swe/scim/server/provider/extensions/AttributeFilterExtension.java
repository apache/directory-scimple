package edu.psu.swe.scim.server.provider.extensions;

import edu.psu.swe.scim.server.provider.extensions.exceptions.ClientFilterException;
import edu.psu.swe.scim.spec.resources.ScimResource;

public interface AttributeFilterExtension extends ProcessingExtension {

  ScimResource filterAttributes(ScimResource scimResource, ScimRequestContext scimRequestContext) throws ClientFilterException;
  
}
