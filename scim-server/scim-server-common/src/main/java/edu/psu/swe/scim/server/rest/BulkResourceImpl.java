package edu.psu.swe.scim.server.rest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import edu.psu.swe.scim.spec.protocol.data.BulkOperation.Method;
import edu.psu.swe.scim.spec.protocol.data.BulkOperation.Status;
import edu.psu.swe.scim.spec.protocol.data.BulkRequest;
import edu.psu.swe.scim.spec.protocol.data.BulkResponse;
import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;
import edu.psu.swe.scim.spec.schema.Schema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class BulkResourceImpl implements BulkResource {
  private static final Status OKAY_STATUS = new Status();
  private static final Status CREATED_STATUS = new Status();
  private static final Status NO_CONTENT_STATUS = new Status();
  private static final Status METHOD_NOT_ALLOWED_STATUS = new Status();
  private static final Status CONFLICT_STATUS = new Status();
  private static final Status CLIENT_ERROR_STATUS = new Status();
  private static final Status INTERNAL_SERVER_ERROR_STATUS = new Status();
  private static final Status METHOD_NOT_IMPLEMENTED_STATUS = new Status();
  private static final String OKAY = "200";
  private static final String CREATED = "201";
  private static final String NO_CONTENT = "204";
  private static final String CLIENT_ERROR = "400";
  private static final String METHOD_NOT_ALLOWED = "405";
  private static final String CONFLICT = "409";
  private static final String INTERNAL_SERVER_ERROR = "500";
  private static final String METHOD_NOT_IMPLEMENTED = "501";
  private static final String BULK_ID_DOES_NOT_EXIST = "Bulk ID cannot be resolved because it refers to no bulkId in any Bulk Operation: %s";
  private static final String BULK_ID_REFERS_TO_FAILED_RESOURCE = "Bulk ID cannot be resolved because the resource it refers to had failed to be created: %s";
  private static final String OPERATION_DEPENDS_ON_FAILED_OPERATION = "Operation depends on failed bulk operation: %s";
  private static final Pattern PATH_PATTERN = Pattern.compile("^/[^/]+/[^/]+$");

  static {
    METHOD_NOT_ALLOWED_STATUS.setCode(METHOD_NOT_ALLOWED);
    OKAY_STATUS.setCode(OKAY);
    CREATED_STATUS.setCode(CREATED);
    NO_CONTENT_STATUS.setCode(NO_CONTENT);
    CONFLICT_STATUS.setCode(CONFLICT);
    CLIENT_ERROR_STATUS.setCode(CLIENT_ERROR);
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
    int maxErrorCount = requestFailOnErrors > 0 ? requestFailOnErrors : Integer.MAX_VALUE;
    int errorCountIncrement = requestFailOnErrors > 0 ? 1 : 0;
    List<BulkOperation> bulkOperations = request.getOperations();
    Map<String, BulkOperation> bulkIdKeyToOperationResult = new HashMap<>();
    List<IWishJavaHadTuples> allUnresolveds = new ArrayList<>();
    Map<String, Set<String>> reverseDependenciesGraph = this.generateReverseDependenciesGraph(bulkOperations);
    Map<String, Set<String>> transitiveReverseDependencies = generateTransitiveDependenciesGraph(reverseDependenciesGraph);

    log.debug("Reverse dependencies: {}", reverseDependenciesGraph);
    log.debug("Transitive reverse dependencies: {}", transitiveReverseDependencies);

    // clean out unwanted data
    for (BulkOperation operationRequest : bulkOperations) {
      operationRequest.setResponse(null);
      operationRequest.setStatus(null);
    }
    // get all known bulkIds, handle bad input
    for (BulkOperation operationRequest : bulkOperations) {
      String bulkId = operationRequest.getBulkId();
      Method method = operationRequest.getMethod();
      String bulkIdKey = bulkId != null ? "bulkId:" + bulkId : null;
      boolean errorOccurred = false;

      // duplicate bulkId
      if (bulkIdKey != null) {
        if (!bulkIdKeyToOperationResult.containsKey(bulkIdKey)) {
          bulkIdKeyToOperationResult.put(bulkIdKey, operationRequest);
        } else {
          errorCount += errorCountIncrement;
          errorOccurred = true;
          BulkOperation duplicateOperation = bulkIdKeyToOperationResult.get(bulkIdKey);

          createAndSetErrorResponse(operationRequest, CONFLICT_STATUS, "Duplicate bulkId");

          if (!(duplicateOperation.getResponse() instanceof ErrorResponse)) {
            errorCount += errorCountIncrement;

            duplicateOperation.setData(null);
            createAndSetErrorResponse(duplicateOperation, CONFLICT_STATUS, "Duplicate bulkId");
          }
        }
      }
      // bad/missing input for method
      if (method != null && !(operationRequest.getResponse() instanceof ErrorResponse)) {
        switch (method) {
        case POST:
        case PUT: {
          if (operationRequest.getData() == null) {
            errorCount += errorCountIncrement;
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, CLIENT_ERROR_STATUS, "data not provided");
          }
        } break;

        case DELETE: {
          String path = operationRequest.getPath();

          if (path == null) {
            errorCount += errorCountIncrement;
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, CLIENT_ERROR_STATUS, "path not provided");
          } else if (!PATH_PATTERN.matcher(path).matches()) {
            errorCount += errorCountIncrement;
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, CLIENT_ERROR_STATUS, "path is not valid path (e.g. \"/Groups/123abc\", \"/Users/123xyz\", ...)");
          } else {
            String endPoint = path.substring(0, path.lastIndexOf('/'));
            Class<ScimResource> clazz = (Class<ScimResource>) registry.findScimResourceClassFromEndpoint(endPoint);

            if (clazz == null) {
              errorOccurred = true;

              createAndSetErrorResponse(operationRequest, CLIENT_ERROR_STATUS, "path does not contain a valid endpoint (e.g. \"/Groups/...\", \"/Users/...\", ...)");
            }
          }
        } break;

        default: {
        } break;
        }
      }
      if (errorOccurred) {
        operationRequest.setData(null);

        if (bulkIdKey != null) {
          Set<String> reverseDependencies = transitiveReverseDependencies.get(bulkIdKey);
          String detail = String.format(OPERATION_DEPENDS_ON_FAILED_OPERATION, bulkIdKey);

          for (String dependentBulkIdKey : reverseDependencies) {
            BulkOperation dependentOperation = bulkIdKeyToOperationResult.get(dependentBulkIdKey);

            if (!(dependentOperation.getResponse() instanceof ErrorResponse)) {
              errorCount += errorCountIncrement;

              dependentOperation.setData(null);
              createAndSetErrorResponse(dependentOperation, CONFLICT_STATUS, detail);
            }
          }
        }
      }
    }
    // do the operations
    for (BulkOperation operationResult : bulkOperations) {
      boolean errorCountExceeded = errorCount >= maxErrorCount;

      if (!errorCountExceeded && !(operationResult.getResponse() instanceof ErrorResponse)) {
        try {
          this.handleBulkOperationMethod(allUnresolveds, operationResult, bulkIdKeyToOperationResult, uriInfo);
        } catch (UnableToCreateResourceException | UnableToDeleteResourceException | UnableToUpdateResourceException resourceException) {
          log.error("Failed to do bulk operation", resourceException);

          errorCount += errorCountIncrement;
          String detail = resourceException.getLocalizedMessage();
          Status status = new Status();
          String code;

          if (resourceException instanceof UnableToCreateResourceException) {
            code = ((UnableToCreateResourceException) resourceException).getStatus().toString();
          } else if (resourceException instanceof UnableToDeleteResourceException) {
            code = ((UnableToDeleteResourceException) resourceException).getStatus().toString();
          } else {
            code = ((UnableToUpdateResourceException) resourceException).getStatus().toString();
          }

          status.setCode(code);
          createAndSetErrorResponse(operationResult, status, detail);

          if (operationResult.getBulkId() != null) {
            String bulkIdKey = "bulkId:" + operationResult.getBulkId();

            this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
          }
        } catch (UnresolvableOperationException unresolvableOperationException) {
          log.error("Could not resolve bulkId during Bulk Operation method handling", unresolvableOperationException);

          errorCount += errorCountIncrement;
          String detail = unresolvableOperationException.getLocalizedMessage();

          createAndSetErrorResponse(operationResult, CONFLICT_STATUS, detail);

          if (operationResult.getBulkId() != null) {
            String bulkIdKey = "bulkId:" + operationResult.getBulkId();

            this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
          }
        }
      } else if (errorCountExceeded) {
        createAndSetErrorResponse(operationResult, CONFLICT_STATUS, "failOnErrors count reached");

        if (operationResult.getBulkId() != null) {
          String bulkIdKey = "bulkId:" + operationResult.getBulkId();

          this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
        }
      }
    }
    // Resolve unresolved bulkIds
    for (IWishJavaHadTuples iwjht : allUnresolveds) {
      BulkOperation bulkOperationResult = iwjht.bulkOperationResult;
      String bulkIdKey = iwjht.bulkIdKey;
      ScimResource scimResource = bulkOperationResult.getData();

      try {
        for (UnresolvedTopLevel unresolved : iwjht.unresolveds) {
          log.debug("Final resolution pass for {}", unresolved);
          unresolved.resolve(scimResource, bulkIdKeyToOperationResult);
        }
        String scimResourceId = scimResource.getId();
        @SuppressWarnings("unchecked")
        Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
        Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

        provider.update(scimResourceId, scimResource);
      } catch (UnresolvableOperationException unresolvableOperationException) {
        log.error("Could not complete final resolution pass, unresolvable bulkId", unresolvableOperationException);

        String detail = unresolvableOperationException.getLocalizedMessage();

        bulkOperationResult.setData(null);
        createAndSetErrorResponse(bulkOperationResult, CONFLICT_STATUS, detail);
        this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
      } catch (UnableToUpdateResourceException unableToUpdateResourceException) {
          log.error("Failed to update Scim Resource with resolved bulkIds", unableToUpdateResourceException);

          String detail = unableToUpdateResourceException.getLocalizedMessage();
          String code = unableToUpdateResourceException.getStatus().toString();
          Status status = new Status();

          status.setCode(code);
          bulkOperationResult.setData(null);
          createAndSetErrorResponse(bulkOperationResult, status, detail);
          this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
      }
    }
    response = new BulkResponse();
    response.setOperations(bulkOperations);
    response.setStatus(OKAY);

    return Response.ok(response).build();
  }

  private void cleanup(
      String bulkIdKeyToCleanup,
      Map<String, Set<String>> transitiveReverseDependencies,
      Map<String, BulkOperation> bulkIdKeyToOperationResult) {
    Set<String> reverseDependencies = transitiveReverseDependencies.get(bulkIdKeyToCleanup);
    BulkOperation operationResult = bulkIdKeyToOperationResult.get(bulkIdKeyToCleanup);
    String bulkId = operationResult.getBulkId();
    ScimResource scimResource = operationResult.getData();
    @SuppressWarnings("unchecked")
    Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
    Provider<ScimResource> provider = this.providerRegistry.getProvider(scimResourceClass);

    try {
      provider.delete(scimResource.getId());
    } catch (UnableToDeleteResourceException unableToDeleteResourceException) {
      log.error("Could not delete ScimResource after failure: {}", scimResource);
    }
    for (String dependentBulkIdKey : reverseDependencies) {
      BulkOperation dependentOperationResult = bulkIdKeyToOperationResult.get(dependentBulkIdKey);

      if (!(dependentOperationResult.getResponse() instanceof ErrorResponse)) try {
        ScimResource dependentResource = dependentOperationResult.getData();
        String dependentResourceId  = dependentResource.getId();
        @SuppressWarnings("unchecked")
        Class<ScimResource> dependentResourceClass = (Class<ScimResource>) dependentResource.getClass();
        Provider<ScimResource> dependentResourceProvider = this.providerRegistry.getProvider(dependentResourceClass);

        dependentOperationResult.setData(null);
        createAndSetErrorResponse(dependentOperationResult, CONFLICT_STATUS, String.format(OPERATION_DEPENDS_ON_FAILED_OPERATION, bulkId, dependentBulkIdKey));
        dependentResourceProvider.delete(dependentResourceId);
      } catch (UnableToDeleteResourceException unableToDeleteResourceException) {
        log.error("Could not delete depenedent ScimResource after failing to update dependee", unableToDeleteResourceException);
      }
    }
  }

  private void handleBulkOperationMethod(
      List<IWishJavaHadTuples> unresolveds,
      BulkOperation operationResult,
      Map<String, BulkOperation> bulkIdKeyToOperationResult,
      UriInfo uriInfo)
      throws UnableToCreateResourceException, UnableToDeleteResourceException, UnableToUpdateResourceException, UnresolvableOperationException {
    ScimResource scimResource = operationResult.getData();
    Method bulkOperationMethod = operationResult.getMethod();
    String bulkId = operationResult.getBulkId();
    Class<ScimResource> scimResourceClass;

    if (scimResource == null) {
      String path = operationResult.getPath();
      String endPoint = path.substring(0, path.lastIndexOf('/'));
      Class<ScimResource> clazz = (Class<ScimResource>) registry.findScimResourceClassFromEndpoint(endPoint);
      scimResourceClass = clazz;
    } else {
      @SuppressWarnings("unchecked")
      Class<ScimResource> clazz = (Class<ScimResource>) scimResource.getClass();
      scimResourceClass = clazz;
    }
    Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

    switch (bulkOperationMethod) {
    case POST: {
      log.debug("POST: {}", scimResource);

      this.resolveTopLevel(unresolveds, operationResult, bulkIdKeyToOperationResult);

      log.debug("Creating {}", scimResource);

      ScimResource newScimResource = provider.create(scimResource);
      String bulkOperationPath = operationResult.getPath();
      String newResourceId = newScimResource.getId();
      String newResourceUri = uriInfo.getBaseUriBuilder().path(bulkOperationPath).path(newResourceId).build().toString();

      if (bulkId != null) {
        String bulkIdKey = "bulkId:" + bulkId;

        log.debug("adding {} = {}", bulkIdKey, newResourceId);
        bulkIdKeyToOperationResult.get(bulkIdKey).setData(newScimResource);
      }
      operationResult.setData(newScimResource);
      operationResult.setLocation(newResourceUri.toString());
      operationResult.setStatus(CREATED_STATUS);
    } break;

    case DELETE: {
      log.debug("DELETE: {}", operationResult.getPath());

      String scimResourceId = operationResult.getPath().substring(operationResult.getPath().lastIndexOf("/") + 1);

      provider.delete(scimResourceId);
      operationResult.setStatus(NO_CONTENT_STATUS);
    } break;

    case PUT: {
      log.debug("PUT: {}", scimResource);

      this.resolveTopLevel(unresolveds, operationResult, bulkIdKeyToOperationResult);
      String id = operationResult.getPath().substring(operationResult.getPath().lastIndexOf("/") + 1);

      provider.update(id, scimResource);
      operationResult.setStatus(OKAY_STATUS);
    } break;

    case PATCH: {
      log.debug("PATCH: {}", scimResource);
      createAndSetErrorResponse(operationResult, METHOD_NOT_IMPLEMENTED_STATUS, "Method not implemented: PATCH");
    } break;

    default: {
      BulkOperation.Method method = operationResult.getMethod();
      String detail = "Method not allowed: " + method;

      log.error("Received unallowed method: {}", method);
      createAndSetErrorResponse(operationResult, METHOD_NOT_ALLOWED_STATUS, detail);
    } break;
    }
  }

  private static void createAndSetErrorResponse(BulkOperation operationResult, Status status, String detail) {
    ErrorResponse error = new ErrorResponse();
    String code = status.getCode();

    error.setStatus(code);
    error.setDetail(detail);
    operationResult.setResponse(error);
    operationResult.setStatus(status);
  }

  @AllArgsConstructor
  private static class IWishJavaHadTuples {
    public final String bulkIdKey;
    public final List<UnresolvedTopLevel> unresolveds;
    public final BulkOperation bulkOperationResult;
  }

  private static class UnresolvableOperationException extends Exception {
    private static final long serialVersionUID = -6081994707016671935L;

    public UnresolvableOperationException(String message) {
      super(message);
    }
  }

  @AllArgsConstructor
  private static class UnresolvedComplex {
    private final Object object;
    private final Field field;
    private final String bulkIdKey;

    public void resolve(Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      BulkOperation resolvedOperation = bulkIdKeyToOperationResult.get(this.bulkIdKey);
      BaseResource response = resolvedOperation.getResponse();
      ScimResource resolvedResource = resolvedOperation.getData();

      if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
        String resolvedId = resolvedResource.getId();

        try {
          this.field.set(this.object, resolvedId);
        } catch (IllegalAccessException illegalAccessException) {
          log.error("Failed to access bulkId field", illegalAccessException);
        }
      } else {
        throw new UnresolvableOperationException(String.format(BULK_ID_REFERS_TO_FAILED_RESOURCE, this.bulkIdKey));
      }
    }
  }

  @AllArgsConstructor
  private static abstract class UnresolvedTopLevel {
    protected final Field field;

    public abstract void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException;
  }

  private static class UnresolvedTopLevelBulkId extends UnresolvedTopLevel {
    private final String unresolvedBulkIdKey;

    public UnresolvedTopLevelBulkId(Field field, String bulkIdKey) {
      super(field);
      this.unresolvedBulkIdKey = bulkIdKey;
    }

    @Override
    public void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(this.unresolvedBulkIdKey);
      BaseResource response = resolvedOperationResult.getResponse();
      ScimResource resolvedResource = resolvedOperationResult.getData();

      if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
        String resolvedId = resolvedResource.getId();

        try {
          super.field.set(scimResource, resolvedId);
        } catch (IllegalAccessException illegalAccessException) {
          log.error("Failed to access bulkId field", illegalAccessException);
        }
      } else {
        throw new UnresolvableOperationException("Bulk ID cannot be resolved because the resource it refers to had failed to be created: " + this.unresolvedBulkIdKey);
      }
    }
  }

  private static class UnresolvedTopLevelComplex extends UnresolvedTopLevel {
    public final Object complex;
    public final List<UnresolvedComplex> unresolveds;

    public UnresolvedTopLevelComplex(Field field, Object complex, List<UnresolvedComplex> unresolveds) {
      super(field);
      this.complex = complex;
      this.unresolveds = unresolveds;
    }

    @Override
    public void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      try {
        for (UnresolvedComplex unresolved : this.unresolveds) {
          unresolved.resolve(bulkIdKeyToOperationResult);
        }
        this.field.set(scimResource, this.complex);
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Could not resolve top level SCIM resource", illegalAccessException);
      }
    }
  }

  private static List<UnresolvedComplex> resolveAttribute(
      List<UnresolvedComplex> unresolveds,
      Object attributeValue,
      Schema.Attribute attribute,
      Map<String, BulkOperation> bulkIdKeyToOperationResult)
      throws UnresolvableOperationException {
    if (attributeValue == null) {
      return unresolveds;
    }
    List<Schema.Attribute> attributes = attribute.getAttributes();

    for (Schema.Attribute subAttribute : attributes) {
      Field attributeField = subAttribute.getField();

      try {
        if (subAttribute.isScimResourceIdReference()) {
          // TODO - This will fail if field is a char or Character array
          String bulkIdKey = (String) attributeField.get(attributeValue);

          if (bulkIdKey != null && bulkIdKey.startsWith("bulkId:")) {
            log.debug("Found bulkId: {}", bulkIdKey);
            if (bulkIdKeyToOperationResult.containsKey(bulkIdKey)) {
              BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(bulkIdKey);
              BaseResource response = resolvedOperationResult.getResponse();
              ScimResource resolvedResource = resolvedOperationResult.getData();

              if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null && resolvedResource.getId() != null) {
                String resolvedId = resolvedResource.getId();

                attributeField.set(attributeValue, resolvedId);
              } else {
                UnresolvedComplex unresolved = new UnresolvedComplex(attributeValue, attributeField, bulkIdKey);

                unresolveds.add(unresolved);
              }
            } else {
              throw new UnresolvableOperationException(String.format(BULK_ID_DOES_NOT_EXIST, bulkIdKey));
            }
          }
        } else if (subAttribute.getType() == Schema.Attribute.Type.COMPLEX) {
          Object subFieldValue = attributeField.get(attributeValue);

          if (subFieldValue != null) {
            Class<?> subFieldClass = subFieldValue.getClass();
            boolean isCollection = Collection.class.isAssignableFrom(subFieldClass);

            if (isCollection || subFieldClass.isArray()) {
              @SuppressWarnings("unchecked")
              Collection<Object> subFieldValues = isCollection ? (Collection<Object>) subFieldValue : Arrays.asList((Object[]) subFieldValue);

              for (Object subArrayFieldValue : subFieldValues) {
                resolveAttribute(unresolveds, subArrayFieldValue, subAttribute, bulkIdKeyToOperationResult);
              }
            } else {
              resolveAttribute(unresolveds, subFieldValue, subAttribute, bulkIdKeyToOperationResult);
            }
          }
        }
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Could not resolve bulkId within ScimResource attribute", illegalAccessException);
      }
    }
    log.debug("Resolved attribute had {} unresolved fields", unresolveds.size());
    return unresolveds;
  }

  private void resolveTopLevel(
      List<IWishJavaHadTuples> unresolveds,
      BulkOperation bulkOperationResult,
      Map<String, BulkOperation> bulkIdKeyToOperationResult)
      throws UnresolvableOperationException {
    ScimResource scimResource = bulkOperationResult.getData();
    String schemaUrn = scimResource.getBaseUrn();
    Schema schema = this.registry.getSchema(schemaUrn);
    List<UnresolvedTopLevel> unresolvedTopLevels = new ArrayList<>();

    for (Schema.Attribute attribute : schema.getAttributes()) {
      Field attributeField = attribute.getField();

      try {
        if (attribute.isScimResourceIdReference()) {
          String bulkIdKey = (String) attributeField.get(scimResource);

          if (bulkIdKey != null && bulkIdKey.startsWith("bulkId:")) {
            if (bulkIdKeyToOperationResult.containsKey(bulkIdKey)) {
              BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(bulkIdKey);
              BaseResource response = resolvedOperationResult.getResponse();
              ScimResource resolvedResource = resolvedOperationResult.getData();

              if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
                String resolvedId = resolvedResource.getId();

                attributeField.set(scimResource, resolvedId);
              } else {
                UnresolvedTopLevel unresolved = new UnresolvedTopLevelBulkId(attributeField, bulkIdKey);

                attributeField.set(scimResource, null);
                unresolvedTopLevels.add(unresolved);
              }
            } else {
              throw new UnresolvableOperationException(String.format(BULK_ID_DOES_NOT_EXIST, bulkIdKey));
            }
          }
        } else if (attribute.getType() == Schema.Attribute.Type.COMPLEX) {
          Object attributeFieldValue = attributeField.get(scimResource);

          if (attributeFieldValue != null) {
            List<UnresolvedComplex> subUnresolveds = new ArrayList<>();
            Class<?> subFieldClass = attributeFieldValue.getClass();
            boolean isCollection = Collection.class.isAssignableFrom(subFieldClass);

            if (isCollection || subFieldClass.isArray()) {
              @SuppressWarnings("unchecked")
              Collection<Object> subFieldValues = isCollection ? (Collection<Object>) attributeFieldValue : Arrays.asList((Object[]) attributeFieldValue);

              for (Object subArrayFieldValue : subFieldValues) {
                resolveAttribute(subUnresolveds, subArrayFieldValue, attribute, bulkIdKeyToOperationResult);
              }
            } else {
              resolveAttribute(subUnresolveds, attributeFieldValue, attribute, bulkIdKeyToOperationResult);
            }

            if (subUnresolveds.size() > 0) {
              UnresolvedTopLevel unresolved = new UnresolvedTopLevelComplex(attributeField, attributeFieldValue, subUnresolveds);

              attributeField.set(scimResource, null);
              unresolvedTopLevels.add(unresolved);
            }
          }
        }
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Failed to access a ScimResource ID reference field to resolve it", illegalAccessException);
      }
    }
    if (unresolvedTopLevels.size() > 0) {
      String bulkIdKey = "bulkId:" + bulkOperationResult.getBulkId();

      unresolveds.add(new IWishJavaHadTuples(bulkIdKey, unresolvedTopLevels, bulkOperationResult));
    }
  }

  private static void generateVisited(Set<String> visited, Map<String, Set<String>> dependencyGraph, String root, String current) {
    if (!root.equals(current) && !visited.contains(current)) {
      visited.add(current);

      Set<String> dependencies = dependencyGraph.get(current);

      for (String dependency : dependencies) {
        generateVisited(visited, dependencyGraph, root, dependency);
      }
    }
  }

  private static Map<String, Set<String>> generateTransitiveDependenciesGraph(Map<String, Set<String>> dependenciesGraph) {
    Map<String, Set<String>> transitiveDependenciesGraph = new HashMap<>();

    for (Map.Entry<String, Set<String>> entry : dependenciesGraph.entrySet()) {
      String root = entry.getKey();
      Set<String> dependencies = entry.getValue();
      Set<String> visited = new HashSet<>();

      transitiveDependenciesGraph.put(root, visited);

      for (String dependency : dependencies) {
        generateVisited(visited, dependenciesGraph, root, dependency);
      }
    }
    return transitiveDependenciesGraph;
  }

  private static void generateReverseDependenciesGraph(
      Map<String, Set<String>> reverseDependenciesGraph,
      String dependentBulkId,
      Object scimObject,
      List<Schema.Attribute> scimObjectAttributes) {
    for (Schema.Attribute scimObjectAttribute : scimObjectAttributes) try {
      if (scimObjectAttribute.isScimResourceIdReference()) {
        String reference = (String) scimObjectAttribute.getField().get(scimObject);

        if (reference != null && reference.startsWith("bulkId:")) {
          Set<String> dependents = reverseDependenciesGraph.computeIfAbsent(reference, (unused) -> new HashSet<>());

          dependents.add("bulkId:" + dependentBulkId);
        }
      } else if (scimObjectAttribute.isMultiValued()) { // all multiValueds are COMPLEX, not all COMPLEXES are multiValued
        Object attributeObject = scimObjectAttribute.getField().get(scimObject);
        Class<?> attributeObjectClass = attributeObject.getClass();
        boolean isCollection = Collection.class.isAssignableFrom(attributeObjectClass);
        Collection<?> attributeValues = isCollection ? (Collection<?>) attributeObject : Arrays.asList(attributeObject);
        List<Schema.Attribute> subAttributes = scimObjectAttribute.getAttributes();

        for (Object attributeValue : attributeValues) {
          generateReverseDependenciesGraph(reverseDependenciesGraph, dependentBulkId, attributeValue, subAttributes);
        }
      } else if (scimObjectAttribute.getType() == Schema.Attribute.Type.COMPLEX) {
        Object attributeValue = scimObjectAttribute.getField().get(scimObject);
        List<Schema.Attribute> subAttributes = scimObjectAttribute.getAttributes();

        generateReverseDependenciesGraph(reverseDependenciesGraph, dependentBulkId, attributeValue, subAttributes);
      }
    } catch (IllegalAccessException illegalAccessException) {
      log.error("Resolving reverse dependencies", illegalAccessException);
    }
  }

  private Map<String, Set<String>> generateReverseDependenciesGraph(List<BulkOperation> bulkOperations) {
    Map<String, Set<String>> reverseDependenciesGraph = new HashMap<>();

    for (BulkOperation bulkOperation : bulkOperations) {
      String bulkId = bulkOperation.getBulkId();

      if (bulkId != null) {
        ScimResource scimResource = bulkOperation.getData();
        String scimResourceBaseUrn = scimResource.getBaseUrn();
        Schema schema = this.registry.getSchema(scimResourceBaseUrn);
        List<Schema.Attribute> attributes = schema.getAttributes();

        generateReverseDependenciesGraph(reverseDependenciesGraph, bulkId, scimResource, attributes);
      }
    }
    return reverseDependenciesGraph;
  }
}
