package edu.psu.swe.scim.server.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.exception.ScimServerException;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.UpdateRequest;
import edu.psu.swe.scim.server.provider.annotations.ScimProcessingExtension;
import edu.psu.swe.scim.server.provider.extensions.AttributeFilterExtension;
import edu.psu.swe.scim.server.provider.extensions.ProcessingExtension;
import edu.psu.swe.scim.server.provider.extensions.ScimRequestContext;
import edu.psu.swe.scim.server.provider.extensions.exceptions.ClientFilterException;
import edu.psu.swe.scim.server.utility.AttributeUtil;
import edu.psu.swe.scim.server.utility.EndpointUtil;
import edu.psu.swe.scim.server.utility.EtagGenerator;
import edu.psu.swe.scim.spec.adapter.FilterWrapper;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BaseResourceTypeResourceImpl.class);

  @Context
  UriInfo uriInfo;

  @Context
  Request request;

  @Context
  HttpServletRequest servletRequest;

  @Inject
  private AttributeUtil attributeUtil;

  @Inject
  private EndpointUtil endpointUtil;

  @Inject
  private EtagGenerator etagGenerator;

  @Inject
  private Instance<UpdateRequest<T>> updateRequestInstance;

  public abstract Provider<T> getProvider();

  Provider<T> getProviderInternal() throws ScimServerException {
    Provider<T> provider = getProvider();
    if (provider == null) {
      throw new ScimServerException(Status.INTERNAL_SERVER_ERROR, "Provider not defined");
    }
    return provider;
  }

  @Override
  public Response getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    if (servletRequest.getParameter("filter") != null) {
      return Response.status(Status.FORBIDDEN)
                     .build();
    }

    try {
      Provider<T> provider = getProviderInternal();

      endpointUtil.process(uriInfo);
      T resource = null;
      try {
        resource = provider.get(id);
      } catch (UnableToRetrieveResourceException e2) {
        if (e2.getStatus()
              .getFamily()
              .equals(Family.SERVER_ERROR)) {
          return createGenericExceptionResponse(e2, e2.getStatus());
        }
      } catch (Exception e) {
        throw new ScimServerException(Status.INTERNAL_SERVER_ERROR, "Uncaught provider exception: " + e.getMessage(), e);
      }

      if (resource != null) {
        EntityTag backingETag = null;
        try {
          backingETag = etagGenerator.generateEtag(resource);
        } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e1) {
          return createETagErrorResponse();
        }

        ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);

        if (evaluatePreconditionsResponse != null) {
          return Response.status(Status.NOT_MODIFIED)
                         .build();
        }
      }

      Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes)
                                                            .map(wrapper -> wrapper.getAttributeReferences())
                                                            .orElse(Collections.emptySet());
      Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes)
                                                                    .map(wrapper -> wrapper.getAttributeReferences())
                                                                    .orElse(Collections.emptySet());

      if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
        return createAmbiguousAttributeParametersResponse();
      }

      if (resource == null) {
        return createNotFoundResponse(id);
      }

      EntityTag etag = null;

      try {
        etag = etagGenerator.generateEtag(resource);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
        return createETagErrorResponse();
      }

      // Process Attributes
      try {
        resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
      } catch (ClientFilterException e1) {
        ErrorResponse er = new ErrorResponse(e1.getStatus(), e1.getMessage());
        return er.toResponse();
      }

      try {
        if (!excludedAttributeReferences.isEmpty()) {
          resource = attributeUtil.setExcludedAttributesForDisplay(resource, excludedAttributeReferences);
        } else {
          resource = attributeUtil.setAttributesForDisplay(resource, attributeReferences);
        }

        return Response.ok()
                       .entity(resource)
                       .location(buildLocationTag(resource))
                       .tag(etag)
                       .build();
      } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
        e.printStackTrace();
        return createAttriubteProcessingErrorResponse(e);
      }
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }

  }

  @Override
  public Response query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, FilterWrapper filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) {
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(Optional.ofNullable(attributes)
                                        .map(wrapper -> wrapper.getAttributeReferences())
                                        .orElse(Collections.emptySet()));
    searchRequest.setExcludedAttributes(Optional.ofNullable(excludedAttributes)
                                                .map(wrapper -> wrapper.getAttributeReferences())
                                                .orElse(Collections.emptySet()));

    if (filter != null) {
      searchRequest.setFilter(filter.getFilter());
    }
    else {
      searchRequest.setFilter(null);
    }
    
    searchRequest.setSortBy(sortBy);
    searchRequest.setSortOrder(sortOrder);
    searchRequest.setStartIndex(startIndex);
    searchRequest.setCount(count);

    return find(searchRequest);
  }

  @Override
  public Response create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    try {
      Provider<T> provider = getProviderInternal();

      Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes)
                                                            .map(wrapper -> wrapper.getAttributeReferences())
                                                            .orElse(Collections.emptySet());
      Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes)
                                                                    .map(wrapper -> wrapper.getAttributeReferences())
                                                                    .orElse(Collections.emptySet());

      if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
        return createAmbiguousAttributeParametersResponse();
      }

      endpointUtil.process(uriInfo);
      T created;
      try {
        created = provider.create(resource);
      } catch (UnableToCreateResourceException e1) {
        Status status = e1.getStatus();
        ErrorResponse er = new ErrorResponse(status, "Error");

        if (status == Status.CONFLICT) {
          er.setScimType(ErrorMessageType.UNIQUENESS);
          
          //only use default error message if the ErrorResponse does not already contain a message
          if (e1.getMessage() == null) {
            er.setDetail(ErrorMessageType.UNIQUENESS.getDetail());
          } else {
            er.setDetail(e1.getMessage());
          }
        } else {
          er.setDetail(e1.getMessage());
        }

        return er.toResponse();
      } catch (Exception e) {
        log.error("Uncaught provider exception", e);
        return createGenericExceptionResponse(e, Status.INTERNAL_SERVER_ERROR);
      }

      EntityTag etag = null;
      try {
        etag = etagGenerator.generateEtag(created);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
        log.error("Failed to generate etag for newly created entity " + e.getMessage());
      }

      // Process Attributes
      try {
        created = processFilterAttributeExtensions(provider, created, attributeReferences, excludedAttributeReferences);
      } catch (ClientFilterException e1) {
        ErrorResponse er = new ErrorResponse(e1.getStatus(), e1.getMessage());
        return er.toResponse();
      }

      try {
        if (!excludedAttributeReferences.isEmpty()) {
          created = attributeUtil.setExcludedAttributesForDisplay(created, excludedAttributeReferences);
        } else {
          created = attributeUtil.setAttributesForDisplay(created, attributeReferences);
        }
      } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
        if (etag == null) {
          return Response.status(Status.CREATED)
                         .location(buildLocationTag(created))
                         .build();
        } else {
          Response.status(Status.CREATED)
                  .location(buildLocationTag(created))
                  .tag(etag)
                  .build();
        }
      }

      // TODO - Is this the right behavior?
      if (etag == null) {
        return Response.status(Status.CREATED)
                       .location(buildLocationTag(created))
                       .entity(created)
                       .build();
      }

      return Response.status(Status.CREATED)
                     .location(buildLocationTag(created))
                     .tag(etag)
                     .entity(created)
                     .build();
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }
  }

  @Override
  public Response find(SearchRequest request) {
    try {
      Provider<T> provider = getProviderInternal();

      Set<AttributeReference> attributeReferences = Optional.ofNullable(request.getAttributes())
                                                            .orElse(Collections.emptySet());
      Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(request.getExcludedAttributes())
                                                                    .orElse(Collections.emptySet());
      if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
        return createAmbiguousAttributeParametersResponse();
      }

      Filter filter = request.getFilter();
      PageRequest pageRequest = request.getPageRequest();
      SortRequest sortRequest = request.getSortRequest();

      ListResponse<T> listResponse = new ListResponse<>();

      endpointUtil.process(uriInfo);
      FilterResponse<T> filterResp = null;
      try {
        filterResp = provider.find(filter, pageRequest, sortRequest);
      } catch (UnableToRetrieveResourceException e1) {
        log.info("Caught an UnableToRetrieveResourceException " + e1.getMessage() + " : " + e1.getStatus()
                                                                                              .toString());
        return createGenericExceptionResponse(e1, e1.getStatus());
      } catch (Exception e) {
        log.error("Uncaught provider exception", e);
        return createGenericExceptionResponse(e, Status.INTERNAL_SERVER_ERROR);
      }

      // If no resources are found, we should still return a ListResponse with
      // the totalResults set to 0;
      // (https://tools.ietf.org/html/rfc7644#section-3.4.2)
      if (filterResp == null || filterResp.getResources() == null || filterResp.getResources()
                                                                               .isEmpty()) {
        listResponse.setTotalResults(0);
      } else {
        log.info("Find returned " + filterResp.getResources()
                                              .size());
        listResponse.setItemsPerPage(filterResp.getResources()
                                               .size());
        listResponse.setStartIndex(1);
        listResponse.setTotalResults(filterResp.getResources()
                                               .size());

        List<T> results = new ArrayList<>();

        for (T resource : filterResp.getResources()) {
          EntityTag etag = null;

          try {
            etag = etagGenerator.generateEtag(resource);
          } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return createETagErrorResponse();
          }

          // Process Attributes
          try {
            log.info("=== Calling processFilterAttributeExtensions");
            resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
          } catch (ClientFilterException e1) {
            ErrorResponse er = new ErrorResponse(e1.getStatus(), e1.getMessage());
            return er.toResponse();
          }

          try {
            if (!excludedAttributeReferences.isEmpty()) {
              resource = attributeUtil.setExcludedAttributesForDisplay(resource, excludedAttributeReferences);
            } else {
              resource = attributeUtil.setAttributesForDisplay(resource, attributeReferences);
            }

            results.add(resource);
          } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
            return createAttriubteProcessingErrorResponse(e);
          }
        }

        listResponse.setResources(results);
      }

      return Response.ok()
                     .entity(listResponse)
                     .build();
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }
  }

  @Override
  public Response update(T resource, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    try {
      Provider<T> provider = getProviderInternal();

      Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes)
                                                            .map(wrapper -> wrapper.getAttributeReferences())
                                                            .orElse(Collections.emptySet());
      Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes)
                                                                    .map(wrapper -> wrapper.getAttributeReferences())
                                                                    .orElse(Collections.emptySet());

      if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
        return createAmbiguousAttributeParametersResponse();
      }

      endpointUtil.process(uriInfo);
      T stored;
      try {
        stored = provider.get(id);
      } catch (UnableToRetrieveResourceException e2) {
        log.error("Unable to retrieve resource with id: {}", id, e2);
        return createGenericExceptionResponse(e2, e2.getStatus());
      } catch (Exception e) {
        log.error("Uncaught provider exception", e);
        return createGenericExceptionResponse(e, Status.INTERNAL_SERVER_ERROR);
      }

      if (stored == null) {
        return createNotFoundResponse(id);
      }

      EntityTag backingETag = null;
      try {
        backingETag = etagGenerator.generateEtag(stored);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e1) {
        return createETagErrorResponse();
      }

      ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);

      if (evaluatePreconditionsResponse != null) {
        return createPreconditionFailedResponse(id, evaluatePreconditionsResponse);
      }

      T updated;
      try {
        UpdateRequest<T> updateRequest = updateRequestInstance.get();
        updateRequest.initWithResource(id, stored, resource);
        updated = provider.update(updateRequest);
      } catch (UnableToUpdateResourceException e1) {
        return createGenericExceptionResponse(e1, e1.getStatus());
      }

      // Process Attributes
      try {
        updated = processFilterAttributeExtensions(provider, updated, attributeReferences, excludedAttributeReferences);
      } catch (ClientFilterException e1) {
        ErrorResponse er = new ErrorResponse(e1.getStatus(), e1.getMessage());
        return er.toResponse();
      }

      try {
        if (!excludedAttributeReferences.isEmpty()) {
          updated = attributeUtil.setExcludedAttributesForDisplay(updated, excludedAttributeReferences);
        } else {
          updated = attributeUtil.setAttributesForDisplay(updated, attributeReferences);
        }
      } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
        log.error("Failed to handle attribute processing in update " + e.getMessage());
      }

      EntityTag etag = null;
      try {
        etag = etagGenerator.generateEtag(updated);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
        log.error("Failed to generate etag for newly created entity " + e.getMessage());
      }

      // TODO - Is this correct or should we support roll back semantics
      if (etag == null) {
        return Response.ok(updated)
                       .location(buildLocationTag(updated))
                       .build();
      }

      return Response.ok(updated)
                     .location(buildLocationTag(updated))
                     .tag(etag)
                     .build();
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }
  }

  @Override
  public Response patch(PatchRequest patchRequest, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws Exception {
    try {
      Provider<T> provider = getProviderInternal();

      Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes)
                                                            .map(wrapper -> wrapper.getAttributeReferences())
                                                            .orElse(Collections.emptySet());
      Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes)
                                                                    .map(wrapper -> wrapper.getAttributeReferences())
                                                                    .orElse(Collections.emptySet());

      if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
        return createAmbiguousAttributeParametersResponse();
      }

      endpointUtil.process(uriInfo);
      T stored;
      try {
        stored = provider.get(id);
      } catch (UnableToRetrieveResourceException e2) {
        log.error("Unable to retrieve resource with id: {}", id, e2);
        return createGenericExceptionResponse(e2, e2.getStatus());
      } catch (Exception e) {
        log.error("Uncaught provider exception", e);
        return createGenericExceptionResponse(e, Status.INTERNAL_SERVER_ERROR);
      }

      if (stored == null) {
        return createNotFoundResponse(id);
      }

      EntityTag backingETag = null;
      try {
        backingETag = etagGenerator.generateEtag(stored);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e1) {
        return createETagErrorResponse();
      }

      ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);

      if (evaluatePreconditionsResponse != null) {
        return createPreconditionFailedResponse(id, evaluatePreconditionsResponse);
      }

      T updated;
      try {
        UpdateRequest<T> updateRequest = updateRequestInstance.get();
        updateRequest.initWithPatch(id, stored, patchRequest.getPatchOperationList());
        updated = provider.update(updateRequest);
      } catch (UnableToUpdateResourceException e1) {
        return createGenericExceptionResponse(e1, e1.getStatus());
      }

      // Process Attributes
      try {
        updated = processFilterAttributeExtensions(provider, updated, attributeReferences, excludedAttributeReferences);
      } catch (ClientFilterException e1) {
        ErrorResponse er = new ErrorResponse(e1.getStatus(), e1.getMessage());
        return er.toResponse();
      }

      try {
        if (!excludedAttributeReferences.isEmpty()) {
          updated = attributeUtil.setExcludedAttributesForDisplay(updated, excludedAttributeReferences);
        } else {
          updated = attributeUtil.setAttributesForDisplay(updated, attributeReferences);
        }
      } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
        log.error("Failed to handle attribute processing in update " + e.getMessage());
      }

      EntityTag etag = null;
      try {
        etag = etagGenerator.generateEtag(updated);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
        log.error("Failed to generate etag for newly created entity " + e.getMessage());
      }

      // TODO - Is this correct or should we support roll back semantics
      if (etag == null) {
        return Response.ok(updated)
                       .location(buildLocationTag(updated))
                       .build();
      }

      return Response.ok(updated)
                     .location(buildLocationTag(updated))
                     .tag(etag)
                     .build();
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }

  }

  @Override
  public Response delete(String id) {
    try {
      Response response;
      Provider<T> provider = getProviderInternal();

      endpointUtil.process(uriInfo);
      response = Response.noContent()
                         .build();

      provider.delete(id);
      return response;
    } catch (UnableToDeleteResourceException e) {
      Status status = e.getStatus();
      Response response = Response.status(status)
                                  .build();

      log.error("Unable to delete resource", e);

      return response;
    } catch (ScimServerException sse) {
      LOG.error("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    } catch (Exception e) {
      log.error("Uncaught provider exception", e);
      return createGenericExceptionResponse(e, Status.INTERNAL_SERVER_ERROR);
    }
  }

  private T processFilterAttributeExtensions(Provider<T> provider, T resource, Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ClientFilterException {
    ScimProcessingExtension annotation = provider.getClass()
                                                 .getAnnotation(ScimProcessingExtension.class);
    if (annotation != null) {
      Class<? extends ProcessingExtension>[] value = annotation.value();
      for (Class<? extends ProcessingExtension> class1 : value) {
        try {
          ProcessingExtension processingExtension = class1.newInstance();
          if (processingExtension instanceof AttributeFilterExtension) {
            AttributeFilterExtension attributeFilterExtension = (AttributeFilterExtension) processingExtension;
            ScimRequestContext scimRequestContext = new ScimRequestContext(attributeReferences, excludedAttributeReferences);

            resource = (T) attributeFilterExtension.filterAttributes(resource, scimRequestContext);
            log.info("Resource now - " + resource.toString());
          }
        } catch (InstantiationException | IllegalAccessException e) {
          log.error("Error processing filter attriute extensions", e);
        }
      }
    }

    return resource;
  }

  private URI buildLocationTag(T resource) {
    String id = resource.getId();
    if (id == null) {
      LOG.warn("Provider must supply an id for a resource");
      id = "unknown";
    }
    return uriInfo.getAbsolutePathBuilder()
                  .path(id)
                  .build();
  }

  private Response createGenericExceptionResponse(Exception e1, Status status) {
    Status myStatus = status;
    if (myStatus == null) {
      myStatus = Status.INTERNAL_SERVER_ERROR;
    }

    ErrorResponse er = new ErrorResponse(myStatus, e1.getMessage());
    return er.toResponse();
  }

  private Response createAmbiguousAttributeParametersResponse() {
    ErrorResponse er = new ErrorResponse(Status.BAD_REQUEST, "Cannot include both attributes and excluded attributes in a single request");
    return er.toResponse();
  }

  private Response createNotFoundResponse(String id) {
    ErrorResponse er = new ErrorResponse(Status.NOT_FOUND, "Resource " + id + " not found");
    return er.toResponse();
  }

  private Response createETagErrorResponse() {
    ErrorResponse er = new ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Failed to generate the etag");
    return er.toResponse();
  }

  private Response createAttriubteProcessingErrorResponse(Exception e) {
    ErrorResponse er = new ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Failed to parse the attribute query value " + e.getMessage());
    return er.toResponse();
  }

  private Response createNoProviderException() {
    ErrorResponse er = new ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Provider not defined");
    return er.toResponse();
  }

  private Response createPreconditionFailedResponse(String id, ResponseBuilder evaluatePreconditionsResponse) {
    ErrorResponse er = new ErrorResponse(Status.PRECONDITION_FAILED, "Failed to update record, backing record has changed - " + id);
    log.warn("Failed to update record, backing record has changed - " + id);
    return evaluatePreconditionsResponse.entity(er)
                                        .build();
  }
}
