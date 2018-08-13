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
import javax.enterprise.inject.spi.CDI;
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

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.directory.scim.server.exception.AttributeDoesNotExistException;
import org.apache.directory.scim.server.exception.ScimServerException;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.exception.UnableToDeleteResourceException;
import org.apache.directory.scim.server.exception.UnableToRetrieveResourceException;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.server.provider.annotations.ScimProcessingExtension;
import org.apache.directory.scim.server.provider.extensions.AttributeFilterExtension;
import org.apache.directory.scim.server.provider.extensions.ProcessingExtension;
import org.apache.directory.scim.server.provider.extensions.ScimRequestContext;
import org.apache.directory.scim.server.provider.extensions.exceptions.ClientFilterException;
import org.apache.directory.scim.server.utility.AttributeUtil;
import org.apache.directory.scim.server.utility.EndpointUtil;
import org.apache.directory.scim.server.utility.EtagGenerator;
import org.apache.directory.scim.spec.adapter.FilterWrapper;
import org.apache.directory.scim.spec.protocol.BaseResourceTypeResource;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.ListResponse;
import org.apache.directory.scim.spec.protocol.data.PatchRequest;
import org.apache.directory.scim.spec.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.PageRequest;
import org.apache.directory.scim.spec.protocol.search.SortOrder;
import org.apache.directory.scim.spec.protocol.search.SortRequest;
import org.apache.directory.scim.spec.resources.ScimResource;

@Slf4j
public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

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
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
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
      log.debug("Error Processing SCIM Request", sse);
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
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
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
      log.debug("Error Processing SCIM Request", sse);
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
        log.debug("Caught an UnableToRetrieveResourceException " + e1.getMessage() + " : " + e1.getStatus()
                                                                                              .toString());
        return createGenericExceptionResponse(e1, e1.getStatus());
      } catch (Exception e) {
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
      }

      // If no resources are found, we should still return a ListResponse with
      // the totalResults set to 0;
      // (https://tools.ietf.org/html/rfc7644#section-3.4.2)
      if (filterResp == null || filterResp.getResources() == null || filterResp.getResources()
                                                                               .isEmpty()) {
        listResponse.setTotalResults(0);
      } else {
        log.debug("Find returned " + filterResp.getResources()
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
            log.trace("=== Calling processFilterAttributeExtensions");
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
      log.debug("Error Processing SCIM Request", sse);
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
        log.debug("Unable to retrieve resource with id: {}", id, e2);
        return createGenericExceptionResponse(e2, e2.getStatus());
      } catch (Exception e) {
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
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
      } catch (Exception e1) {
        log.debug("Uncaught provider exception", e1);

        return provider.handleException(e1);
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
      log.debug("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }
  }

  @Override
  public Response patch(PatchRequest patchRequest, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
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
        log.debug("Unable to retrieve resource with id: {}", id, e2);
        return createGenericExceptionResponse(e2, e2.getStatus());
      } catch (Exception e) {
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
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
      } catch (UnsupportedOperationException e2) {
        return createGenericExceptionResponse(e2, Status.NOT_IMPLEMENTED);
      } catch (Exception e1) {
        log.debug("Uncaught provider exception", e1);

        return provider.handleException(e1);
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
      log.debug("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }

  }

  @Override
  public Response delete(String id) {
    Response response;
    try {
      Provider<T> provider = getProviderInternal();

      try {
        endpointUtil.process(uriInfo);
        response = Response.noContent()
                           .build();

        provider.delete(id);
        return response;
      } catch (UnableToDeleteResourceException e) {
        Status status = e.getStatus();
        response = Response.status(status)
                           .build();

        log.error("Unable to delete resource", e);

        return response;
      } catch (Exception e) {
        log.debug("Uncaught provider exception", e);

        return provider.handleException(e);
      }
    } catch (ScimServerException sse) {
      log.debug("Error Processing SCIM Request", sse);
      return sse.getErrorResponse()
                .toResponse();
    }
  }

  @SuppressWarnings("unchecked")
  private T processFilterAttributeExtensions(Provider<T> provider, T resource, Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ClientFilterException {
    ScimProcessingExtension annotation = provider.getClass()
                                                 .getAnnotation(ScimProcessingExtension.class);
    if (annotation != null) {
      Class<? extends ProcessingExtension>[] value = annotation.value();
      for (Class<? extends ProcessingExtension> class1 : value) {
        ProcessingExtension processingExtension = CDI.current().select(class1).get();
        if (processingExtension instanceof AttributeFilterExtension) {
          AttributeFilterExtension attributeFilterExtension = (AttributeFilterExtension) processingExtension;
          ScimRequestContext scimRequestContext = new ScimRequestContext(attributeReferences, excludedAttributeReferences);

          resource = (T) attributeFilterExtension.filterAttributes(resource, scimRequestContext);
          log.debug("Resource now - " + resource.toString());
        }
      }
    }

    return resource;
  }

  private URI buildLocationTag(T resource) {
    String id = resource.getId();
    if (id == null) {
      log.warn("Provider must supply an id for a resource");
      id = "unknown";
    }
    return uriInfo.getAbsolutePathBuilder()
                  .path(id)
                  .build();
  }

  public static Response createGenericExceptionResponse(Throwable e1, Status status) {
    Status myStatus = status;
    if (myStatus == null) {
      myStatus = Status.INTERNAL_SERVER_ERROR;
    }

    ErrorResponse er = new ErrorResponse(myStatus, e1 != null ? e1.getMessage() : "Unknown Server Error");
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
