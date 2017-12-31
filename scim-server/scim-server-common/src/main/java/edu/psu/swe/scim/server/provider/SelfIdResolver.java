package edu.psu.swe.scim.server.provider;

import java.security.Principal;

import edu.psu.swe.scim.server.exception.UnableToResolveIdException;

public interface SelfIdResolver {

  String resolveToInternalId(Principal principal) throws UnableToResolveIdException;
  
}
