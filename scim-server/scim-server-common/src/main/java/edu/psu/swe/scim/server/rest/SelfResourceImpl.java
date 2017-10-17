package edu.psu.swe.scim.server.rest;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.exception.UnableToResolveIdException;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.provider.SelfIdResolver;
import edu.psu.swe.scim.spec.protocol.SelfResource;
import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.exception.ScimException;
import edu.psu.swe.scim.spec.resources.ScimUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class SelfResourceImpl implements SelfResource {

  @Inject
  ProviderRegistry providerRegistry;

  @Inject
  UserResource userResource;

  @Inject
  SelfIdResolver selfIdResolver;

  @Resource
  private SessionContext sessionContext;

  @Override
  public Response getSelf(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    try {
      String internalId = getInternalId();
      return userResource.getById(internalId, attributes, excludedAttributes);
    } catch (UnableToResolveIdException e) {
      return createErrorResponse(e);
    } catch (ScimException e) {
      return createErrorResponse(e);
    }
  }

  // @Override
  // public Response create(ScimUser resource, AttributeReferenceListWrapper
  // attributes, AttributeReferenceListWrapper excludedAttributes) {
  // String internalId = getInternalId();
  // //TODO check if ids match in request
  // return userResourceImpl.create(resource, attributes, excludedAttributes);
  // }

  @Override
  public Response update(ScimUser resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    try {
      String internalId = getInternalId();
      return userResource.update(resource, internalId, attributes, excludedAttributes);
    } catch (UnableToResolveIdException e) {
      return createErrorResponse(e);
    } catch (ScimException e) {
      return createErrorResponse(e);
    }
  }

  @Override
  public Response patch(PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    try {
      String internalId = getInternalId();
      return userResource.patch(patchRequest, internalId, attributes, excludedAttributes);
    } catch (UnableToResolveIdException e) {
      return createErrorResponse(e);
    } catch (ScimException e) {
      return createErrorResponse(e);
    }
  }

  @Override
  public Response delete() {
    try {
      String internalId = getInternalId();
      return userResource.delete(internalId);
    } catch (UnableToResolveIdException e) {
      return createErrorResponse(e);
    } catch (ScimException e) {
      return createErrorResponse(e);
    }
  }

  private Response createErrorResponse(ScimException e) {
    ErrorResponse er = new ErrorResponse(e.getStatus(), "Error");
    er.addErrorMessage(e.getMessage());
    return er.toResponse();
  }

  private Response createErrorResponse(UnableToResolveIdException e) {
    ErrorResponse er = new ErrorResponse(e.getStatus(), "Error");
    er.addErrorMessage(e.getMessage());
    return er.toResponse();
  }

  private String getInternalId() throws UnableToResolveIdException {
    Principal callerPrincipal = sessionContext.getCallerPrincipal();

    if (callerPrincipal != null) {
      log.info("Resolved SelfResource principal to : {}", callerPrincipal.getName());
    } else {
      throw new UnableToResolveIdException(Status.UNAUTHORIZED, "Unauthorized");
    }

    String internalId = selfIdResolver.resolveToInternalId(callerPrincipal);
    return internalId;
  }

}
