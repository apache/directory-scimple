package edu.psu.swe.scim.server.rest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.psu.swe.scim.spec.schema.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class BulkResourceImpl implements BulkResource {
  private static final Status OKAY_STATUS = new Status();
  private static final Status CREATED_STATUS = new Status();
  private static final Status NO_CONTENT_STATUS = new Status();
  private static final Status METHOD_NOT_ALLOWED_STATUS = new Status();
  private static final Status CONFLICT_STATUS = new Status();
  private static final Status INTERNAL_SERVER_ERROR_STATUS = new Status();
  private static final Status METHOD_NOT_IMPLEMENTED_STATUS = new Status();
  private static final String OKAY = "200";
  private static final String CREATED = "201";
  private static final String NO_CONTENT = "204";
  private static final String METHOD_NOT_ALLOWED = "405";
  private static final String CONFLICT = "409";
  private static final String INTERNAL_SERVER_ERROR = "500";
  private static final String METHOD_NOT_IMPLEMENTED = "501";
  private static final String BULK_ID_DOES_NOT_EXIST = "Bulk ID cannot be resolved because it refers to no bulkId in any Bulk Operation";

  static {
    METHOD_NOT_ALLOWED_STATUS.setCode(METHOD_NOT_ALLOWED);
    OKAY_STATUS.setCode(OKAY);
    CREATED_STATUS.setCode(CREATED);
    NO_CONTENT_STATUS.setCode(NO_CONTENT);
    CONFLICT_STATUS.setCode(CONFLICT);
    INTERNAL_SERVER_ERROR_STATUS.setCode(INTERNAL_SERVER_ERROR);
    METHOD_NOT_IMPLEMENTED_STATUS.setCode(METHOD_NOT_IMPLEMENTED);
  }

  @Inject
  Registry registry;

  @Inject
  ProviderRegistry providerRegistry;

  // TODO - handle failures on forward dependencies
  @Override
  public Response doBulk(BulkRequest request, UriInfo uriInfo) {
    BulkResponse response;
    int errorCount = 0;
    int requestFailOnErrors = request.getFailOnErrors();
    long maxErrorCount = requestFailOnErrors > 0 ? requestFailOnErrors : Long.MAX_VALUE;
    List<BulkOperation> completedOperations = new ArrayList<>();
    Map<String, String> bulkIdToResourceId = new HashMap<>();
    List<IWishJavaHadTuples> allUnresolveds = new ArrayList<>();

    for (BulkOperation bulkOperation : request.getOperations()) {
      String bulkId = bulkOperation.getBulkId();

      if (bulkId != null && !bulkIdToResourceId.containsKey(bulkId)) {
        bulkIdToResourceId.put("bulkId:" + bulkId, null);
      } else {
        // TODO - Return an error response
      }
    }
    for (BulkOperation bulkOperation : request.getOperations()) {
      BulkOperation operationResult = new BulkOperation();
      if (errorCount < maxErrorCount) {
        String bulkId = bulkOperation.getBulkId();

        try {
          ScimResource scimResource = bulkOperation.getData();
          @SuppressWarnings("unchecked")
          Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
          Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

          switch (bulkOperation.getMethod()) {
          case POST: {
            log.debug("POST: {}", scimResource);

            List<Unresolved> unresolveds = this.resolveTopLevel(new ArrayList<>(), scimResource, bulkIdToResourceId);
            log.debug("Creating {}", scimResource);
            ScimResource newResource = provider.create(scimResource);
            String bulkOperationPath = bulkOperation.getPath();
            String newResourceId = newResource.getId();
            String newResourceUri = uriInfo.getBaseUriBuilder().path(bulkOperationPath).path(newResourceId).build().toString();

            if (bulkId != null) {
              log.debug("adding bulkId:{} = {}", bulkId, newResourceId);
              bulkIdToResourceId.put("bulkId:" + bulkId, newResourceId);
            }
            operationResult.setLocation(newResourceUri);
            operationResult.setStatus(CREATED_STATUS);

            if (unresolveds.size() > 0) {
              log.debug("Resource had unresolved bulkIds");
              for (Unresolved unresolved : unresolveds) {
                log.debug("    {}", unresolved);
              }
              IWishJavaHadTuples iwjht = new IWishJavaHadTuples(newResource, unresolveds, operationResult);

              allUnresolveds.add(iwjht);
            }
          } break;

          case DELETE: {
            log.debug("DELETE: {}", scimResource);

            String scimResourceId = scimResource.getId();

            provider.delete(scimResourceId);
            operationResult.setStatus(NO_CONTENT_STATUS);
          } break;

          case PUT: {
            log.debug("PUT: {}", scimResource);

            List<Unresolved> unresolveds = this.resolveTopLevel(new ArrayList<>(), scimResource, bulkIdToResourceId);
            String id = bulkOperation.getPath().substring(bulkOperation.getPath().lastIndexOf("/") + 1);
            ScimResource newResource = provider.update(id, scimResource);

            operationResult.setStatus(OKAY_STATUS);

            if (unresolveds.size() > 0) {
              IWishJavaHadTuples iwjht = new IWishJavaHadTuples(newResource, unresolveds, operationResult);

              allUnresolveds.add(iwjht);
            }
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
        } catch (UnresolvableOperationException unresolvableOperationException) {
          log.error("Could not resolve bulkId during Bulk Operation method handling", unresolvableOperationException);

          errorCount += 1;
          String detail = unresolvableOperationException.getLocalizedMessage();

          createAndSetErrorResponse(operationResult, CONFLICT_STATUS, detail);
        }
      } else {
        createAndSetErrorResponse(operationResult, CONFLICT_STATUS, "failOnErrors count reached");
      }
      completedOperations.add(operationResult);
    }
    for (IWishJavaHadTuples iwjht : allUnresolveds) {
      for (Unresolved unresolved : iwjht.unresolveds) {
        try {
          log.debug("Final resolution pass for {}", unresolved);
          unresolved.resolve(bulkIdToResourceId);
        } catch (UnresolvableOperationException unresolvableOperationException) {
          log.error("Could not complete final resolution pass, unresolvable bulkId", unresolvableOperationException);

          String detail = unresolvableOperationException.getLocalizedMessage();

          createAndSetErrorResponse(iwjht.bulkOperationResult, CONFLICT_STATUS, detail);
        }
      }
      ScimResource scimResource = iwjht.scimResource;
      @SuppressWarnings("unchecked")
      Class<ScimResource> scimResourceClass = (Class<ScimResource>) scimResource.getClass();
      Provider<ScimResource> provider = providerRegistry.getProvider(scimResourceClass);

      try {
        log.debug("Updating {}", scimResource);
        provider.update(iwjht.scimResource.getId(), iwjht.scimResource);
      } catch (UnableToUpdateResourceException unableToUpdateResourceException) {
        String detail = unableToUpdateResourceException.getLocalizedMessage();
        String code = unableToUpdateResourceException.getStatus().toString();
        Status status = new Status();

        status.setCode(code);
        createAndSetErrorResponse(iwjht.bulkOperationResult, status, detail);
        log.error("Failed to update Scim Resource with resolved bulkIds", unableToUpdateResourceException);

        try {
          provider.delete(iwjht.scimResource.getId());
        } catch (UnableToDeleteResourceException unableToDeleteResourceException) {
          log.error("Could not delete ScimResource after failing to update it after resolving bulkIds: {}", iwjht.scimResource);
        }
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

  @AllArgsConstructor
  private static class IWishJavaHadTuples {
    public final ScimResource scimResource;
    public final List<Unresolved> unresolveds;
    public final BulkOperation bulkOperationResult;
  }

  private static class UnresolvableOperationException extends Exception {
    private static final long serialVersionUID = -6081994707016671935L;

    public UnresolvableOperationException(String message, String bulkId) {
      super(message);
    }
  }

  private interface Unresolved {
    public void resolve(Map<String, String> bulkIds) throws UnresolvableOperationException;
  }

  @Data
  private static class UnresolvedBulkId implements Unresolved {
    private final Object object;
    private final Field field;
    private final String bulkId;

    public UnresolvedBulkId(Object object, Field field, String bulkId) {
      this.object = object;
      this.field = field;
      this.bulkId = bulkId;
    }

    @Override
    public void resolve(Map<String, String> bulkIds) throws UnresolvableOperationException {
      String resolvedId = bulkIds.get(this.bulkId);

      if (resolvedId != null) {
        boolean accessible = this.field.isAccessible();

        try {
          this.field.setAccessible(true);
          this.field.set(this.object, resolvedId);
        } catch (IllegalAccessException illegalAccessException) {
          log.error("Failed to access bulkId field", illegalAccessException);
        } finally {
          this.field.setAccessible(accessible);
        }
      } else {
        throw new UnresolvableOperationException("Bulk ID cannot be resolved because the resource it refers to had failed to be created: " + this.bulkId, this.bulkId);
      }
    }
  }

  @Data
  private static class UnresolvedTopLevel implements Unresolved {
    private final ScimResource scimResource;
    private final Field field;
    public final Object complex;
    public final List<UnresolvedBulkId> unresolveds;

    public UnresolvedTopLevel(ScimResource scimResource, Field field, Object complex, List<UnresolvedBulkId> unresolveds) {
      this.scimResource = scimResource;
      this.field = field;
      this.complex = complex;
      this.unresolveds = unresolveds;
    }

    public void resolve(Map<String, String> bulkIds) throws UnresolvableOperationException {
      boolean accessible = this.field.isAccessible();

      try {
        for (UnresolvedBulkId unresolved : this.unresolveds) {
          unresolved.resolve(bulkIds);
        }
        this.field.setAccessible(true);
        this.field.set(this.scimResource, this.complex);
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Could not resolve top level SCIM resource", illegalAccessException);
      } finally {
        this.field.setAccessible(accessible);
      }
    }
  }

  private static List<UnresolvedBulkId> resolveAttribute(List<UnresolvedBulkId> unresolveds, Object attributeValue, Schema.Attribute attribute, Map<String, String> bulkIds) throws UnresolvableOperationException {
    if (attributeValue == null) {
      return unresolveds;
    }
    List<Schema.Attribute> attributes = attribute.getAttributes();

    for (Schema.Attribute subAttribute : attributes) {
      Field attributeField = subAttribute.getField();

      try {
        if (subAttribute.isScimResourceIdReference()) {
          String bulkId = (String) attributeField.get(attributeValue);

          if (bulkId != null && bulkId.startsWith("bulkId:")) {
            if (bulkIds.containsKey(bulkId)) {
              String resolvedId = bulkIds.get(bulkId);

              if (resolvedId != null) {
                attributeField.set(attributeValue, resolvedId);
              } else {
                UnresolvedBulkId unresolved = new UnresolvedBulkId(attributeValue, attributeField, bulkId);

                unresolveds.add(unresolved);
              }
            } else {
              throw new UnresolvableOperationException(BULK_ID_DOES_NOT_EXIST + ": " + bulkId, bulkId);
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
                resolveAttribute(unresolveds, subArrayFieldValue, attribute, bulkIds);
              }
            } else {
              resolveAttribute(unresolveds, subFieldValue, subAttribute, bulkIds);
            }
          }
        }
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Could not resolve bulkId within ScimResource attribute", illegalAccessException);
      }
    }
    return unresolveds;
  }

  private List<Unresolved> resolveTopLevel(List<Unresolved> unresolveds, ScimResource scimResource, Map<String, String> bulkIds) throws UnresolvableOperationException {
    String schemaUrn = scimResource.getBaseUrn();
    Schema schema = this.registry.getSchema(schemaUrn);

    for (Schema.Attribute attribute : schema.getAttributes()) {
      Field attributeField = attribute.getField();

      try {
        if (attribute.isScimResourceIdReference()) {
          String bulkId = (String) attributeField.get(scimResource);

          if (bulkId != null && bulkId.startsWith("bulkId:")) {
            if (bulkIds.containsKey(bulkId)) {
              String resolvedId = bulkIds.get(bulkId);

              if (resolvedId != null) {
                attributeField.set(scimResource, resolvedId);
              } else {
                Unresolved unresolved = new UnresolvedBulkId(scimResource, attributeField, bulkId);

                attributeField.set(scimResource, null);
                unresolveds.add(unresolved);
              }
            } else {
              throw new UnresolvableOperationException(BULK_ID_DOES_NOT_EXIST, bulkId);
            }
          }
        } else if (attribute.getType() == Schema.Attribute.Type.COMPLEX) {
          Object attributeFieldValue = attributeField.get(scimResource);

          if (attributeFieldValue != null) {
            List<UnresolvedBulkId> subUnresolveds = new ArrayList<>();
            Class<?> subFieldClass = attributeFieldValue.getClass();
            boolean isCollection = Collection.class.isAssignableFrom(subFieldClass);

            if (isCollection || subFieldClass.isArray()) {
              @SuppressWarnings("unchecked")
              Collection<Object> subFieldValues = isCollection ? (Collection<Object>) attributeFieldValue : Arrays.asList((Object[]) attributeFieldValue);

              for (Object subArrayFieldValue : subFieldValues) {
                resolveAttribute(subUnresolveds, subArrayFieldValue, attribute, bulkIds);
              }
            } else {
              resolveAttribute(subUnresolveds, attributeFieldValue, attribute, bulkIds);
            }
            if (subUnresolveds.size() > 0) {
              Unresolved unresolved = new UnresolvedTopLevel(scimResource, attributeField, attributeFieldValue, subUnresolveds);

              attributeField.set(scimResource, null);
              unresolveds.add(unresolved);
            }
          }
        }
      } catch (IllegalAccessException illegalAccessException) {
        log.error("Failed to access a ScimResource ID reference field to resolve it", illegalAccessException);
      }
    }
    return unresolveds;
  }
}
