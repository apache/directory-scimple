package edu.psu.swe.scim.server.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
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
  private static final Status OKAY_STATUS = new Status();
  private static final Status CREATED_STATUS = new Status();
  private static final Status NO_CONTENT_STATUS = new Status();
  private static final Status METHOD_NOT_ALLOWED_STATUS = new Status();
  private static final Status INTERNAL_SERVER_ERROR_STATUS = new Status();
  private static final Status METHOD_NOT_IMPLEMENTED_STATUS = new Status();
  private static final String OKAY = "200";
  private static final String CREATED = "201";
  private static final String NO_CONTENT = "204";
  private static final String METHOD_NOT_ALLOWED = "405";
  private static final String INTERNAL_SERVER_ERROR = "500";
  private static final String METHOD_NOT_IMPLEMENTED = "501";

  static {
    METHOD_NOT_ALLOWED_STATUS.setCode(METHOD_NOT_ALLOWED);
    OKAY_STATUS.setCode(OKAY);
    CREATED_STATUS.setCode(CREATED);
    NO_CONTENT_STATUS.setCode(NO_CONTENT);
    INTERNAL_SERVER_ERROR_STATUS.setCode(INTERNAL_SERVER_ERROR);
    METHOD_NOT_IMPLEMENTED_STATUS.setCode(METHOD_NOT_IMPLEMENTED);
  }

  @Inject
  Registry registry;

  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Response doBulk(BulkRequest request, UriInfo uriInfo) {
    BulkResponse response;
    int errorCount = 0;
    int requestFailOnErrors = request.getFailOnErrors();
    long maxErrorCount = requestFailOnErrors > 0 ? requestFailOnErrors : Long.MAX_VALUE;
    List<BulkOperation> completedOperations = new ArrayList<>();

    log.info("request.failOnErrors = {} requestFailOnErrors = {} maxErrorCount = {}", request.getFailOnErrors(), requestFailOnErrors, maxErrorCount);

    BULK_OPERATIONS:
    for (BulkOperation bulkOperation : request.getOperations()) {
      BulkOperation operationResult = new BulkOperation();

      try {
        ScimResource scimResource = bulkOperation.getData();
        @SuppressWarnings("unchecked")
        Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
        Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

        operationResult.setStatus(OKAY_STATUS);

        switch (bulkOperation.getMethod()) {
        case POST: {
          log.debug("POST: {}", scimResource);

          ScimResource newResource = provider.create(scimResource);
          String bulkOperationPath = bulkOperation.getPath();
          String newResourceId = newResource.getId();
          String newResourceUri = uriInfo.getBaseUriBuilder().path(bulkOperationPath).path(newResourceId).build().toString();

          operationResult.setLocation(newResourceUri);
          operationResult.setStatus(CREATED_STATUS);
        } break;

        case DELETE: {
          log.debug("DELETE: {}", scimResource);

          String scimResourceId = scimResource.getId();

          provider.delete(scimResourceId);
          operationResult.setStatus(NO_CONTENT_STATUS);
        } break;

        case PUT: {
          log.debug("PUT: {}", scimResource);
          String id = bulkOperation.getPath().substring(bulkOperation.getPath().lastIndexOf("/") + 1);
          provider.update(id, scimResource);
          operationResult.setStatus(OKAY_STATUS);
        } break;

        case PATCH: {
          log.debug("PATCH: {}", scimResource);
          createAndSetErrorResponse(operationResult, METHOD_NOT_IMPLEMENTED_STATUS, "Method not implemented: PATCH");
        } break;

        default: {
          BulkOperation.Method method = bulkOperation.getMethod();
          String detail = "Method not allowed: " + method;

          log.error("Received unallowed method: {}", method);
          createAndSetErrorResponse(operationResult, METHOD_NOT_ALLOWED_STATUS, detail);
        } break;
        }
      } catch (UnableToCreateResourceException | UnableToDeleteResourceException | UnableToUpdateResourceException resourceException) {
        log.error("Failed to do bulk operation", resourceException);

        errorCount += 1;
        String detail = resourceException.getLocalizedMessage();

        createAndSetErrorResponse(operationResult, INTERNAL_SERVER_ERROR_STATUS, detail);
      }
      completedOperations.add(operationResult);

      if (errorCount >= maxErrorCount) {
        break BULK_OPERATIONS;
      }
    }
    response = new BulkResponse();
    response.setOperations(completedOperations);
    response.setStatus(OKAY);

    return Response.ok(response).build();
  }

  private static void createAndSetErrorResponse(BulkOperation operationResult, Status status, String detail) {
    ErrorResponse error = new ErrorResponse();
    String code = status.getCode();

    error.setStatus(code);
    error.setDetail(detail);
    operationResult.setResponse(error);
    operationResult.setStatus(status);
  }
}
