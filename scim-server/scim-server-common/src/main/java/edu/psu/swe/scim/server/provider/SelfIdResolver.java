package edu.psu.swe.scim.server.provider;

import java.security.Principal;

public interface SelfIdResolver {

  String resolveToInternalId(Principal principal) throws UnableToResolveIdException;
  
}
