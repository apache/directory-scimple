package edu.psu.swe.scim.memory.service;

import java.security.Principal;

import javax.ejb.Stateless;

import edu.psu.swe.scim.server.provider.SelfIdResolver;
import edu.psu.swe.scim.server.provider.UnableToResolveIdException;

@Stateless
public class InMemorySelfResolverImpl implements SelfIdResolver {

  @Override
  public String resolveToInternalId(Principal principal) throws UnableToResolveIdException {
    throw new UnableToResolveIdException("Caller Principal not available");
  }

}
