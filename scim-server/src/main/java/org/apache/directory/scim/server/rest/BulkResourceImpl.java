/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.server.rest;

import java.util.*;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.exception.UnableToDeleteResourceException;
import org.apache.directory.scim.server.exception.UnableToRetrieveResourceException;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.protocol.BulkResource;
import org.apache.directory.scim.protocol.data.BulkOperation;
import org.apache.directory.scim.protocol.data.BulkOperation.Method;
import org.apache.directory.scim.protocol.data.BulkOperation.StatusWrapper;
import org.apache.directory.scim.protocol.data.BulkRequest;
import org.apache.directory.scim.protocol.data.BulkResponse;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.core.schema.SchemaRegistry;

@Slf4j
@ApplicationScoped
public class BulkResourceImpl implements BulkResource {
//  private static final StatusWrapper OKAY_STATUS = new StatusWrapper();
//  private static final StatusWrapper CREATED_STATUS = new StatusWrapper();
//  private static final StatusWrapper NO_CONTENT_STATUS = new StatusWrapper();
//  private static final StatusWrapper METHOD_NOT_ALLOWED_STATUS = new StatusWrapper();
//  private static final StatusWrapper CONFLICT_STATUS = new StatusWrapper();
//  private static final StatusWrapper CLIENT_ERROR_STATUS = new StatusWrapper();
//  private static final StatusWrapper NOT_FOUND_STATUS = new StatusWrapper();
//  private static final StatusWrapper INTERNAL_SERVER_ERROR_STATUS = new StatusWrapper();
//  private static final StatusWrapper METHOD_NOT_IMPLEMENTED_STATUS = new StatusWrapper();
//  private static final String OKAY = "200";
//  private static final String CREATED = "201";
//  private static final String NO_CONTENT = "204";
//  private static final String CLIENT_ERROR = "400";
//  private static final String NOT_FOUND = "404";
//  private static final String METHOD_NOT_ALLOWED = "405";
//  private static final String CONFLICT = "409";
//  private static final String INTERNAL_SERVER_ERROR = "500";
//  private static final String METHOD_NOT_IMPLEMENTED = "501";
  private static final String BULK_ID_DOES_NOT_EXIST = "Bulk ID cannot be resolved because it refers to no bulkId in any Bulk Operation: %s";
  private static final String BULK_ID_REFERS_TO_FAILED_RESOURCE = "Bulk ID cannot be resolved because the resource it refers to had failed to be created: %s";
  private static final String OPERATION_DEPENDS_ON_FAILED_OPERATION = "Operation depends on failed bulk operation: %s";
  private static final Pattern PATH_PATTERN = Pattern.compile("^/[^/]+/[^/]+$");

//  static {
//    METHOD_NOT_ALLOWED_STATUS.setCode(METHOD_NOT_ALLOWED);
//    OKAY_STATUS.setCode(OKAY);
//    CREATED_STATUS.setCode(CREATED);
//    NO_CONTENT_STATUS.setCode(NO_CONTENT);
//    CONFLICT_STATUS.setCode(CONFLICT);
//    CLIENT_ERROR_STATUS.setCode(CLIENT_ERROR);
//    NOT_FOUND_STATUS.setCode(NOT_FOUND);
//    INTERNAL_SERVER_ERROR_STATUS.setCode(INTERNAL_SERVER_ERROR);
//    METHOD_NOT_IMPLEMENTED_STATUS.setCode(METHOD_NOT_IMPLEMENTED);
//  }

  private final SchemaRegistry schemaRegistry;

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public BulkResourceImpl(SchemaRegistry schemaRegistry, RepositoryRegistry repositoryRegistry) {
    this.schemaRegistry = schemaRegistry;
    this.repositoryRegistry = repositoryRegistry;
  }

  public BulkResourceImpl() {
    // CDI
    this(null, null);
  }

  @Override
  public Response doBulk(BulkRequest request, UriInfo uriInfo) {
    BulkResponse response;
    int errorCount = 0;
    Integer requestFailOnErrors = request.getFailOnErrors();
    int maxErrorCount = requestFailOnErrors != null && requestFailOnErrors > 0 ? requestFailOnErrors : Integer.MAX_VALUE;
    int errorCountIncrement = requestFailOnErrors == null || requestFailOnErrors > 0 ? 1 : 0;
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
          errorOccurred = true;
          BulkOperation duplicateOperation = bulkIdKeyToOperationResult.get(bulkIdKey);

          createAndSetErrorResponse(operationRequest, Status.CONFLICT, "Duplicate bulkId");

          if (!(duplicateOperation.getResponse() instanceof ErrorResponse)) {
            duplicateOperation.setData(null);
            createAndSetErrorResponse(duplicateOperation, Status.CONFLICT, "Duplicate bulkId");
          }
        }
      }
      // bad/missing input for method
      if (method != null && !(operationRequest.getResponse() instanceof ErrorResponse)) {
        switch (method) {
        case POST:
        case PUT: {
          if (operationRequest.getData() == null) {
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, Status.BAD_REQUEST, "data not provided");
          }
        }
          break;

        case DELETE: {
          String path = operationRequest.getPath();

          if (path == null) {
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, Status.BAD_REQUEST, "path not provided");
          } else if (!PATH_PATTERN.matcher(path)
                                  .matches()) {
            errorOccurred = true;

            createAndSetErrorResponse(operationRequest, Status.BAD_REQUEST, "path is not a valid path (e.g. \"/Groups/123abc\", \"/Users/123xyz\", ...)");
          } else {
            String endPoint = path.substring(0, path.lastIndexOf('/'));
            Class<ScimResource> clazz = (Class<ScimResource>) schemaRegistry.getScimResourceClassFromEndpoint(endPoint);

            if (clazz == null) {
              errorOccurred = true;

              createAndSetErrorResponse(operationRequest, Status.BAD_REQUEST, "path does not contain a recognized endpoint (e.g. \"/Groups/...\", \"/Users/...\", ...)");
            }
          }
        }
          break;

        case PATCH: {
          errorOccurred = true;

          createAndSetErrorResponse(operationRequest, Status.NOT_IMPLEMENTED, "Method not implemented: PATCH");
        }
          break;

        default: {
        }
          break;
        }
      } else if (method == null) {
        errorOccurred = true;

        operationRequest.setData(null);
        createAndSetErrorResponse(operationRequest, Status.BAD_REQUEST, "no method provided (e.g. PUT, POST, ...");
      }
      if (errorOccurred) {
        operationRequest.setData(null);

        if (bulkIdKey != null) {
          Set<String> reverseDependencies = transitiveReverseDependencies.getOrDefault(bulkIdKey, Collections.emptySet());
          String detail = String.format(OPERATION_DEPENDS_ON_FAILED_OPERATION, bulkIdKey);

          for (String dependentBulkIdKey : reverseDependencies) {
            BulkOperation dependentOperation = bulkIdKeyToOperationResult.get(dependentBulkIdKey);

            if (!(dependentOperation.getResponse() instanceof ErrorResponse)) {
              dependentOperation.setData(null);
              createAndSetErrorResponse(dependentOperation, Status.CONFLICT, detail);
            }
          }
        }
      }
    }

    boolean errorCountExceeded = false;

    // do the operations
    for (BulkOperation operationResult : bulkOperations) {

      if (!errorCountExceeded && !(operationResult.getResponse() instanceof ErrorResponse)) {
        try {
          this.handleBulkOperationMethod(allUnresolveds, operationResult, bulkIdKeyToOperationResult, uriInfo);
        } catch (ResourceException resourceException) {
          log.error("Failed to do bulk operation", resourceException);

          errorCount += errorCountIncrement;
          errorCountExceeded = errorCount >= maxErrorCount;

          String detail = resourceException.getLocalizedMessage();
          createAndSetErrorResponse(operationResult, resourceException.getStatus(), detail);

          if (operationResult.getBulkId() != null) {
            String bulkIdKey = "bulkId:" + operationResult.getBulkId();

            this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
            operationResult.setData(null);
          }
        } catch (UnresolvableOperationException unresolvableOperationException) {
          log.error("Could not resolve bulkId during Bulk Operation method handling", unresolvableOperationException);

          errorCount += errorCountIncrement;
          String detail = unresolvableOperationException.getLocalizedMessage();

          createAndSetErrorResponse(operationResult, Status.CONFLICT, detail);

          if (operationResult.getBulkId() != null) {
            String bulkIdKey = "bulkId:" + operationResult.getBulkId();

            this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
            operationResult.setData(null);
          }
        }
      } else if (errorCountExceeded) {

        // continue processing bulk operations to cleanup any dependencies

        createAndSetErrorResponse(operationResult, Status.CONFLICT, "failOnErrors count reached");
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
        Repository<ScimResource> repository = repositoryRegistry.getRepository(scimResourceClass);

        repository.update(scimResourceId, null, scimResource, Collections.emptySet(), Collections.emptySet());
      } catch (UnresolvableOperationException unresolvableOperationException) {
        log.error("Could not complete final resolution pass, unresolvable bulkId", unresolvableOperationException);

        String detail = unresolvableOperationException.getLocalizedMessage();

        bulkOperationResult.setData(null);
        bulkOperationResult.setLocation(null);
        createAndSetErrorResponse(bulkOperationResult, Status.CONFLICT, detail);
        this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
      } catch (UnableToUpdateResourceException unableToUpdateResourceException) {
        log.error("Failed to update Scim Resource with resolved bulkIds", unableToUpdateResourceException);

        String detail = unableToUpdateResourceException.getLocalizedMessage();

        bulkOperationResult.setData(null);
        bulkOperationResult.setLocation(null);
        createAndSetErrorResponse(bulkOperationResult, unableToUpdateResourceException.getStatus(), detail);
        this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
      } catch (ResourceException e) {
        log.error("Could not complete final resolution pass, unresolvable bulkId", e);

        String detail = e.getLocalizedMessage();

        bulkOperationResult.setData(null);
        bulkOperationResult.setLocation(null);
        createAndSetErrorResponse(bulkOperationResult, Status.NOT_FOUND, detail);
        this.cleanup(bulkIdKey, transitiveReverseDependencies, bulkIdKeyToOperationResult);
      }
    }

    Status status = errorCountExceeded ? Status.BAD_REQUEST : Status.OK;

    response = new BulkResponse()
      .setOperations(bulkOperations)
      .setStatus(status);

    return Response.status(status)
      .entity(response)
      .build();
  }

  /**
   * Delete resources that depend on {@code bulkIdKeyToCleanup}, remove
   * {@link BulkOperation}s data, and set their code and response
   * 
   * @param bulkIdKeyToCleanup
   * @param transitiveReverseDependencies
   * @param bulkIdKeyToOperationResult
   */
  private void cleanup(String bulkIdKeyToCleanup, Map<String, Set<String>> transitiveReverseDependencies, Map<String, BulkOperation> bulkIdKeyToOperationResult) {
    Set<String> reverseDependencies = transitiveReverseDependencies.getOrDefault(bulkIdKeyToCleanup, Collections.emptySet());
    BulkOperation operationResult = bulkIdKeyToOperationResult.get(bulkIdKeyToCleanup);
    String bulkId = operationResult.getBulkId();
    ScimResource scimResource = operationResult.getData();
    @SuppressWarnings("unchecked")
    Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
    Repository<ScimResource> repository = this.repositoryRegistry.getRepository(scimResourceClass);

    try {
      if (StringUtils.isNotBlank(scimResource.getId())) {
        repository.delete(scimResource.getId());
      }
    } catch (ResourceException unableToDeleteResourceException) {
      log.error("Could not delete ScimResource after failure: {}", scimResource);
    }
    for (String dependentBulkIdKey : reverseDependencies) {
      BulkOperation dependentOperationResult = bulkIdKeyToOperationResult.get(dependentBulkIdKey);

      if (!(dependentOperationResult.getResponse() instanceof ErrorResponse))
        try {
          ScimResource dependentResource = dependentOperationResult.getData();
          String dependentResourceId = dependentResource.getId();
          @SuppressWarnings("unchecked")
          Class<ScimResource> dependentResourceClass = (Class<ScimResource>) dependentResource.getClass();
          Repository<ScimResource> dependentResourceRepository = this.repositoryRegistry.getRepository(dependentResourceClass);

          dependentOperationResult.setData(null);
          dependentOperationResult.setLocation(null);
          createAndSetErrorResponse(dependentOperationResult, Status.CONFLICT, String.format(OPERATION_DEPENDS_ON_FAILED_OPERATION, bulkId, dependentBulkIdKey));
          dependentResourceRepository.delete(dependentResourceId);
        } catch (ResourceException unableToDeleteResourceException) {
          log.error("Could not delete depenedent ScimResource after failing to update dependee", unableToDeleteResourceException);
        }
    }
  }

  /**
   * Based on the method requested by {@code operationResult}, invoke that
   * method. Fill {@code unresolveds} with unresolved bulkIds and complexes that
   * contain unresolved bulkIds.
   * 
   * @param unresolveds
   * @param operationResult
   * @param bulkIdKeyToOperationResult
   * @param uriInfo
   * @throws UnableToCreateResourceException
   * @throws UnableToDeleteResourceException
   * @throws UnableToUpdateResourceException
   * @throws UnresolvableOperationException
   */
  private void handleBulkOperationMethod(List<IWishJavaHadTuples> unresolveds, BulkOperation operationResult, Map<String, BulkOperation> bulkIdKeyToOperationResult, UriInfo uriInfo) throws ResourceException, UnresolvableOperationException {
    ScimResource scimResource = operationResult.getData();
    Method bulkOperationMethod = operationResult.getMethod();
    String bulkId = operationResult.getBulkId();
    Class<ScimResource> scimResourceClass;

    if (scimResource == null) {
      String path = operationResult.getPath();
      String endPoint = path.substring(0, path.lastIndexOf('/'));
      Class<ScimResource> clazz = (Class<ScimResource>) schemaRegistry.getScimResourceClassFromEndpoint(endPoint);
      scimResourceClass = clazz;
    } else {
      @SuppressWarnings("unchecked")
      Class<ScimResource> clazz = (Class<ScimResource>) scimResource.getClass();
      scimResourceClass = clazz;
    }
    Repository<ScimResource> repository = repositoryRegistry.getRepository(scimResourceClass);

    switch (bulkOperationMethod) {
    case POST: {
      log.debug("POST: {}", scimResource);

      this.resolveTopLevel(unresolveds, operationResult, bulkIdKeyToOperationResult);

      log.debug("Creating {}", scimResource);

      ScimResource newScimResource = repository.create(scimResource);
      String bulkOperationPath = operationResult.getPath();
      String newResourceId = newScimResource.getId();
      String newResourceUri = uriInfo.getBaseUriBuilder()
                                     .path(bulkOperationPath)
                                     .path(newResourceId)
                                     .build()
                                     .toString();

      if (bulkId != null) {
        String bulkIdKey = "bulkId:" + bulkId;

        log.debug("adding {} = {}", bulkIdKey, newResourceId);
        bulkIdKeyToOperationResult.get(bulkIdKey)
                                  .setData(newScimResource);
      }
      operationResult.setData(newScimResource);
      operationResult.setLocation(newResourceUri);
      operationResult.setPath(null);
      operationResult.setStatus(StatusWrapper.wrap(Status.CREATED));
    }
      break;

    case DELETE: {
      log.debug("DELETE: {}", operationResult.getPath());

      String scimResourceId = operationResult.getPath()
                                             .substring(operationResult.getPath()
                                                                       .lastIndexOf("/")
                                                 + 1);

      repository.delete(scimResourceId);
      operationResult.setStatus(StatusWrapper.wrap(Status.NO_CONTENT));
    }
      break;

    case PUT: {
      log.debug("PUT: {}", scimResource);

      this.resolveTopLevel(unresolveds, operationResult, bulkIdKeyToOperationResult);
      String id = operationResult.getPath()
                                 .substring(operationResult.getPath()
                                                           .lastIndexOf("/")
                                     + 1);

      try {
        repository.update(id, null, scimResource, Collections.emptySet(), Collections.emptySet());
        operationResult.setStatus(StatusWrapper.wrap(Status.OK));
      } catch (UnableToRetrieveResourceException e) {
        operationResult.setStatus(StatusWrapper.wrap(Status.NOT_FOUND));
      }
    }
      break;

    default: {
      BulkOperation.Method method = operationResult.getMethod();
      String detail = "Method not allowed: " + method;

      log.error("Received unallowed method: {}", method);
      createAndSetErrorResponse(operationResult, Status.METHOD_NOT_ALLOWED, detail);
    }
      break;
    }
  }

  private static void createAndSetErrorResponse(BulkOperation operationResult, int statusCode, String detail) {
    createAndSetErrorResponse(operationResult, Status.fromStatusCode(statusCode), detail);
  }

  private static void createAndSetErrorResponse(BulkOperation operationResult, Status status, String detail) {
    ErrorResponse error = new ErrorResponse(status, detail);
    operationResult.setResponse(error);
    operationResult.setStatus(new StatusWrapper(status));
    operationResult.setPath(null);
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
    private final Schema.AttributeAccessor accessor;
    private final String bulkIdKey;

    public void resolve(Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      BulkOperation resolvedOperation = bulkIdKeyToOperationResult.get(this.bulkIdKey);
      BaseResource response = resolvedOperation.getResponse();
      ScimResource resolvedResource = resolvedOperation.getData();

      if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
        String resolvedId = resolvedResource.getId();
        this.accessor.set(this.object, resolvedId);
      } else {
        throw new UnresolvableOperationException(String.format(BULK_ID_REFERS_TO_FAILED_RESOURCE, this.bulkIdKey));
      }
    }
  }

  @AllArgsConstructor
  private static abstract class UnresolvedTopLevel {
    protected final Schema.AttributeAccessor accessor;

    public abstract void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException;
  }

  private static class UnresolvedTopLevelBulkId extends UnresolvedTopLevel {
    private final String unresolvedBulkIdKey;

    public UnresolvedTopLevelBulkId(Schema.AttributeAccessor accessor, String bulkIdKey) {
      super(accessor);
      this.unresolvedBulkIdKey = bulkIdKey;
    }

    @Override
    public void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(this.unresolvedBulkIdKey);
      BaseResource response = resolvedOperationResult.getResponse();
      ScimResource resolvedResource = resolvedOperationResult.getData();

      if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
        String resolvedId = resolvedResource.getId();

        super.accessor.set(scimResource, resolvedId);
      } else {
        throw new UnresolvableOperationException("Bulk ID cannot be resolved because the resource it refers to had failed to be created: " + this.unresolvedBulkIdKey);
      }
    }
  }

  private static class UnresolvedTopLevelComplex extends UnresolvedTopLevel {
    public final Object complex;
    public final List<UnresolvedComplex> unresolveds;

    public UnresolvedTopLevelComplex(Schema.AttributeAccessor accessor, Object complex, List<UnresolvedComplex> unresolveds) {
      super(accessor);
      this.complex = complex;
      this.unresolveds = unresolveds;
    }

    @Override
    public void resolve(ScimResource scimResource, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
      for (UnresolvedComplex unresolved : this.unresolveds) {
        unresolved.resolve(bulkIdKeyToOperationResult);
      }
      this.accessor.set(scimResource, this.complex);
    }
  }

  /**
   * Search through the subattribute {@code subAttributeValue} and fill
   * {@code unresolveds} with unresolved bulkIds.
   * 
   * @param unresolveds
   * @param attributeValue
   * @param attribute
   * @param bulkIdKeyToOperationResult
   * @return
   * @throws UnresolvableOperationException
   */
  private static List<UnresolvedComplex> resolveAttribute(List<UnresolvedComplex> unresolveds, Object attributeValue, Schema.Attribute attribute, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
    if (attributeValue == null) {
      return unresolveds;
    }
    List<Schema.Attribute> attributes = attribute.getAttributes();

    for (Schema.Attribute subAttribute : attributes) {
      Schema.AttributeAccessor accessor = subAttribute.getAccessor();

      if (subAttribute.isScimResourceIdReference()) {
        // TODO - This will fail if field is a char or Character array
        String bulkIdKey = accessor.get(attributeValue);

        if (bulkIdKey != null && bulkIdKey.startsWith("bulkId:")) {
          log.debug("Found bulkId: {}", bulkIdKey);
          if (bulkIdKeyToOperationResult.containsKey(bulkIdKey)) {
            BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(bulkIdKey);
            BaseResource response = resolvedOperationResult.getResponse();
            ScimResource resolvedResource = resolvedOperationResult.getData();

            if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null && resolvedResource.getId() != null) {
              String resolvedId = resolvedResource.getId();

              accessor.set(attributeValue, resolvedId);
            } else {
              UnresolvedComplex unresolved = new UnresolvedComplex(attributeValue, accessor, bulkIdKey);

              unresolveds.add(unresolved);
            }
          } else {
            throw new UnresolvableOperationException(String.format(BULK_ID_DOES_NOT_EXIST, bulkIdKey));
          }
        }
      } else if (subAttribute.getType() == Schema.Attribute.Type.COMPLEX) {
        Object subFieldValue = accessor.get(attributeValue);

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
    }
    log.debug("Resolved attribute had {} unresolved fields", unresolveds.size());
    return unresolveds;
  }

  /**
   * Attempt to resolve the bulkIds referenced inside of the
   * {@link ScimResource} contained inside of {@code bulkOperationResult}. Fill
   * {@code unresolveds} with bulkIds that could not be yet resolved.
   * 
   * @param unresolveds
   * @param bulkOperationResult
   * @param bulkIdKeyToOperationResult
   * @throws UnresolvableOperationException
   */
  private void resolveTopLevel(List<IWishJavaHadTuples> unresolveds, BulkOperation bulkOperationResult, Map<String, BulkOperation> bulkIdKeyToOperationResult) throws UnresolvableOperationException {
    ScimResource scimResource = bulkOperationResult.getData();
    String schemaUrn = scimResource.getBaseUrn();
    Schema schema = this.schemaRegistry.getSchema(schemaUrn);
    List<UnresolvedTopLevel> unresolvedTopLevels = new ArrayList<>();

    for (Schema.Attribute attribute : schema.getAttributes()) {
      Schema.AttributeAccessor accessor = attribute.getAccessor();

      if (attribute.isScimResourceIdReference()) {
        String bulkIdKey = accessor.get(scimResource);

        if (bulkIdKey != null && bulkIdKey.startsWith("bulkId:")) {
          if (bulkIdKeyToOperationResult.containsKey(bulkIdKey)) {
            BulkOperation resolvedOperationResult = bulkIdKeyToOperationResult.get(bulkIdKey);
            BaseResource response = resolvedOperationResult.getResponse();
            ScimResource resolvedResource = resolvedOperationResult.getData();

            if ((response == null || !(response instanceof ErrorResponse)) && resolvedResource != null) {
              String resolvedId = resolvedResource.getId();

              accessor.set(scimResource, resolvedId);
            } else {
              UnresolvedTopLevel unresolved = new UnresolvedTopLevelBulkId(accessor, bulkIdKey);

              accessor.set(scimResource, null);
              unresolvedTopLevels.add(unresolved);
            }
          } else {
            throw new UnresolvableOperationException(String.format(BULK_ID_DOES_NOT_EXIST, bulkIdKey));
          }
        }
      } else if (attribute.getType() == Schema.Attribute.Type.COMPLEX) {
        Object attributeFieldValue = accessor.get(scimResource);

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
            UnresolvedTopLevel unresolved = new UnresolvedTopLevelComplex(accessor, attributeFieldValue, subUnresolveds);

            accessor.set(scimResource, null);
            unresolvedTopLevels.add(unresolved);
          }
        }
      }
    }
    if (unresolvedTopLevels.size() > 0) {
      String bulkIdKey = "bulkId:" + bulkOperationResult.getBulkId();

      unresolveds.add(new IWishJavaHadTuples(bulkIdKey, unresolvedTopLevels, bulkOperationResult));
    }
  }

  /**
   * Traverse the provided dependency graph and fill {@code visited} with
   * visited bulkIds.
   * 
   * @param visited
   * @param dependencyGraph
   * @param root
   * @param current
   */
  private static void generateVisited(Set<String> visited, Map<String, Set<String>> dependencyGraph, String root, String current) {
    if (!root.equals(current) && !visited.contains(current)) {
      visited.add(current);

      Set<String> dependencies = dependencyGraph.getOrDefault(current, Collections.emptySet());
      for (String dependency : dependencies) {
        generateVisited(visited, dependencyGraph, root, dependency);
      }
    }
  }

  /**
   * If A -> {B} and B -> {C} then A -> {B, C}.
   * 
   * @param dependenciesGraph
   * @return
   */
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

  private static void generateReverseDependenciesGraph(Map<String, Set<String>> reverseDependenciesGraph, String dependentBulkId, Object scimObject, List<Schema.Attribute> scimObjectAttributes) {
    for (Schema.Attribute scimObjectAttribute : scimObjectAttributes)
      if (scimObjectAttribute.isScimResourceIdReference()) {
        String reference = scimObjectAttribute.getAccessor().get(scimObject);

        if (reference != null && reference.startsWith("bulkId:")) {
          Set<String> dependents = reverseDependenciesGraph.computeIfAbsent(reference, (unused) -> new HashSet<>());

          dependents.add("bulkId:" + dependentBulkId);
        }
      } else if (scimObjectAttribute.isMultiValued()) { // all multiValueds
                                                        // are COMPLEX, not
                                                        // all COMPLEXES are
                                                        // multiValued
        Object attributeObject = scimObjectAttribute.getAccessor().get(scimObject);
        if (attributeObject != null) {
          Class<?> attributeObjectClass = attributeObject.getClass();
          boolean isCollection = Collection.class.isAssignableFrom(attributeObjectClass);
          Collection<?> attributeValues = isCollection ? (Collection<?>) attributeObject : List.of(attributeObject);
          List<Schema.Attribute> subAttributes = scimObjectAttribute.getAttributes();

          for (Object attributeValue : attributeValues) {
            generateReverseDependenciesGraph(reverseDependenciesGraph, dependentBulkId, attributeValue, subAttributes);
          }
        }
      } else if (scimObjectAttribute.getType() == Schema.Attribute.Type.COMPLEX) {
        Object attributeValue = scimObjectAttribute.getAccessor().get(scimObject);
        List<Schema.Attribute> subAttributes = scimObjectAttribute.getAttributes();

        generateReverseDependenciesGraph(reverseDependenciesGraph, dependentBulkId, attributeValue, subAttributes);
      }
  }

  /**
   * Finds the reverse dependencies of each {@link BulkOperation}.
   * 
   * @param bulkOperations
   * @return
   */
  private Map<String, Set<String>> generateReverseDependenciesGraph(List<BulkOperation> bulkOperations) {
    Map<String, Set<String>> reverseDependenciesGraph = new HashMap<>();

    for (BulkOperation bulkOperation : bulkOperations) {
      String bulkId = bulkOperation.getBulkId();

      if (bulkId != null) {
        ScimResource scimResource = bulkOperation.getData();
        String scimResourceBaseUrn = scimResource.getBaseUrn();
        Schema schema = this.schemaRegistry.getSchema(scimResourceBaseUrn);
        List<Schema.Attribute> attributes = schema.getAttributes();

        generateReverseDependenciesGraph(reverseDependenciesGraph, bulkId, scimResource, attributes);
      }
    }
    return reverseDependenciesGraph;
  }
}
