package edu.psu.swe.scim.memory.service;

import java.security.Principal;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.exception.UnableToResolveIdException;
import edu.psu.swe.scim.server.provider.SelfIdResolver;

@Stateless
public class InMemorySelfResolverImpl implements SelfIdResolver {

  @Override
  public String resolveToInternalId(Principal principal) throws UnableToResolveIdException {
    throw new UnableToResolveIdException(Status.NOT_IMPLEMENTED, "Caller Principal not available");
  }

}
