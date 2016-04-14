package edu.psu.swe.scim.server.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.BulkResource;
import edu.psu.swe.scim.spec.protocol.data.BulkOperation;
import edu.psu.swe.scim.spec.protocol.data.BulkOperation.Status;
import edu.psu.swe.scim.spec.protocol.data.BulkRequest;
import edu.psu.swe.scim.spec.protocol.data.BulkResponse;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class BulkResourceImpl implements BulkResource {
  private static final Status METHOD_NOT_ALLOWED_STATUS = new Status();
  private static final Status OKAY_STATUS = new Status();
  private static final String METHOD_NOT_ALLOWED = "405";
  private static final String OKAY = "200";

  static {
    METHOD_NOT_ALLOWED_STATUS.setCode(METHOD_NOT_ALLOWED);
    OKAY_STATUS.setCode(OKAY);
  }

  @Inject
  Registry registry;

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Response doBulk(BulkRequest request, UriInfo uriInfo) {
    BulkResponse response;
//    int errorCount = 0;
    List<BulkOperation> completedOperations = new ArrayList<>();

    for (BulkOperation bulkOperation : request.getOperations()) {
      BulkOperation operationResult = new BulkOperation();
      ScimResource scimResource = bulkOperation.getData();
      @SuppressWarnings("unchecked")
      Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
      Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

      operationResult.setStatus(OKAY_STATUS);

      switch (bulkOperation.getMethod()) {
      case POST: {
        log.debug("POST: {}", scimResource);

        ScimResource newResource = provider.create(scimResource);

        operationResult.setLocation(uriInfo.getBaseUriBuilder().path(bulkOperation.getPath()).path(newResource.getId()).toString());
      } break;

      case DELETE: {
        log.debug("DELETE: {}", scimResource);

        String scimResourceId = scimResource.getId();

        provider.delete(scimResourceId);
      } break;

      case PATCH:
      case PUT: {
        log.debug("PUT/PATCH: {}", scimResource);

        ScimResource newResource = provider.update(scimResource);

        operationResult.setLocation(uriInfo.getBaseUriBuilder().path(bulkOperation.getPath()).path(newResource.getId()).toString());
      } break;

      default: {
        ErrorResponse error = new ErrorResponse();

        error.setStatus(METHOD_NOT_ALLOWED);
        error.setDetail("Method not allowed: " + bulkOperation.getMethod());
        operationResult.setResponse(error);
        operationResult.setStatus(METHOD_NOT_ALLOWED_STATUS);
      } break;
      }
      completedOperations.add(operationResult);
    }
    response = new BulkResponse();
    response.setOperations(completedOperations);
    response.setStatus(OKAY);

    return Response.ok(response).build();
  }
}
