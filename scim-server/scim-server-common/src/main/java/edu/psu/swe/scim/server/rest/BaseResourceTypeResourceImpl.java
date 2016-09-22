package edu.psu.swe.scim.server.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.annotations.ScimProcessingExtension;
import edu.psu.swe.scim.server.provider.extensions.AttributeFilterExtension;
import edu.psu.swe.scim.server.provider.extensions.ProcessingExtension;
import edu.psu.swe.scim.server.provider.extensions.ScimRequestContext;
import edu.psu.swe.scim.server.provider.extensions.exceptions.ClientFilterException;
import edu.psu.swe.scim.server.utility.AttributeUtil;
import edu.psu.swe.scim.server.utility.EndpointUtil;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;
import edu.psu.swe.scim.spec.schema.Meta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BaseResourceTypeResourceImpl.class);

  public abstract Provider<T> getProvider();

  @Context
  UriInfo uriInfo;

  @Context
  Request request;

  @Context
  HttpServletRequest servletRequest;

  @Inject
  AttributeUtil attributeUtil;

  @Inject
  EndpointUtil endpointUtil;

  @Override
  public Response getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    Provider<T> provider = null;

    if (servletRequest.getParameter("filter") != null) {
      return Response.status(Status.FORBIDDEN).build();
    }

    provider = getProvider();
    if (provider == null) {
      return BaseResourceTypeResource.super.getById(id, attributes, excludedAttributes);
    }

    endpointUtil.process(uriInfo);
    T resource = null;
    try {
      resource = provider.get(id);
    } catch (UnableToRetrieveResourceException e2) {
      if (e2.getStatus().getFamily().equals(Family.SERVER_ERROR)) {
        return createGenericExceptionResponse(e2, e2.getStatus());
      }
    }

    if (resource != null) {
      EntityTag backingETag = null;
      try {
        backingETag = generateEtag(resource);
      } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e1) {
        return createETagErrorResponse();
      }

      ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);

      if (evaluatePreconditionsResponse != null) {
        return Response.status(Status.NOT_MODIFIED).build();
      }
    }

    Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());
    Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());

    if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
      return createAmbiguousAttributeParametersResponse();
    }

    if (resource == null) {
      return createNotFoundResponse(id);
    }

    EntityTag etag = null;

    try {
      etag = generateEtag(resource);
    } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
      return createETagErrorResponse();
    }

    // Process Attributes
    try {
      resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
    } catch (ClientFilterException e1) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus(Integer.toString(e1.getStatus().getStatusCode()));
      er.setDetail(e1.getMessage());
      return Response.status(e1.getStatus()).entity(er).build();
    }

    try {
      if (!excludedAttributeReferences.isEmpty()) {
        resource = attributeUtil.setExcludedAttributesForDisplay(resource, excludedAttributeReferences);
      } else {
        resource = attributeUtil.setAttributesForDisplay(resource, attributeReferences);
      }

      return Response.ok().entity(resource).location(buildLocationTag(resource)).tag(etag).build();
    } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
      e.printStackTrace();
      return createAttriubteProcessingErrorResponse(e);
    }

  }

  @Override
  public Response query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) {
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(Optional.ofNullable(attributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet()));
    searchRequest.setExcludedAttributes(Optional.ofNullable(excludedAttributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet()));

    searchRequest.setFilter(filter);

    searchRequest.setSortBy(sortBy);
    searchRequest.setSortOrder(sortOrder);
    searchRequest.setStartIndex(startIndex);
    searchRequest.setCount(count);

    return find(searchRequest);
  }

  @Override
  public Response create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    Provider<T> provider = null;

    provider = getProvider();
    if (provider == null) {
      return BaseResourceTypeResource.super.create(resource, attributes, excludedAttributes);
    }

    Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());
    Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());

    if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
      return createAmbiguousAttributeParametersResponse();
    }

    endpointUtil.process(uriInfo);
    T created;
    try {
      created = provider.create(resource);
    } catch (UnableToCreateResourceException e1) {
      ErrorResponse er = new ErrorResponse();
      Status status = e1.getStatus();
      if (e1.getStatus().equals(Status.CONFLICT)) {
        er.setStatus(e1.getStatus().toString());
        er.setDetail("uniqueness");
      } else {
        er.setStatus(e1.getStatus().toString());
        er.setDetail(e1.getMessage());
      }

      return Response.status(status).entity(er).build();
    }

    EntityTag etag = null;
    try {
      etag = generateEtag(created);
    } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Failed to generate etag for newly created entity " + e.getMessage());
    }

    // Process Attributes
    try {
      resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
    } catch (ClientFilterException e1) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus(Integer.toString(e1.getStatus().getStatusCode()));
      er.setDetail(e1.getMessage());
      return Response.status(e1.getStatus()).entity(er).build();
    }

    try {
      if (!excludedAttributeReferences.isEmpty()) {
        created = attributeUtil.setExcludedAttributesForDisplay(created, excludedAttributeReferences);
      } else {
        created = attributeUtil.setAttributesForDisplay(created, attributeReferences);
      }
    } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException | IOException e) {
      if (etag == null) {
        return Response.status(Status.CREATED).location(buildLocationTag(created)).build();
      } else {
        Response.status(Status.CREATED).location(buildLocationTag(created)).tag(etag).build();
      }
    }

    // TODO - Is this the right behavior?
    if (etag == null) {
      return Response.status(Status.CREATED).location(buildLocationTag(created)).entity(created).build();
    }

    return Response.status(Status.CREATED).location(buildLocationTag(created)).tag(etag).entity(created).build();
  }

  @Override
  public Response find(SearchRequest request) {
    Provider<T> provider = null;

    provider = getProvider();
    if (provider == null) {
      return BaseResourceTypeResource.super.find(request);
    }

    Set<AttributeReference> attributeReferences = Optional.ofNullable(request.getAttributes()).orElse(Collections.emptySet());
    Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(request.getExcludedAttributes()).orElse(Collections.emptySet());
    if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
      return createAmbiguousAttributeParametersResponse();
    }

    Filter filter = request.getFilter();
    PageRequest pageRequest = request.getPageRequest();
    SortRequest sortRequest = request.getSortRequest();

    ListResponse listResponse = new ListResponse();

    endpointUtil.process(uriInfo);
    FilterResponse<T> filterResp = null;
    try {
      filterResp = provider.find(filter, pageRequest, sortRequest);
    } catch (UnableToRetrieveResourceException e1) {
      log.info("Caught an UnableToRetrieveResourceException " + e1.getMessage() + " : " + e1.getStatus().toString());
      return createGenericExceptionResponse(e1, e1.getStatus());
    }

    // If no resources are found, we should still return a ListResponse with
    // the totalResults set to 0;
    // (https://tools.ietf.org/html/rfc7644#section-3.4.2)
    if (filterResp == null || filterResp.getResources() == null || filterResp.getResources().isEmpty()) {
      listResponse.setTotalResults(0);
    } else {
      log.info("Find returned " + filterResp.getResources().size());
      listResponse.setItemsPerPage(filterResp.getResources().size());
      listResponse.setStartIndex(1);
      listResponse.setTotalResults(filterResp.getResources().size());

      List<Object> results = new ArrayList<>();

      for (T resource : filterResp.getResources()) {
        EntityTag etag = null;

        try {
          etag = generateEtag(resource);
        } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
          return createETagErrorResponse();
        }

        // Process Attributes
        try {
          log.info("=== Calling processFilterAttributeExtensions");
          resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
        } catch (ClientFilterException e1) {
          ErrorResponse er = new ErrorResponse();
          er.setStatus(Integer.toString(e1.getStatus().getStatusCode()));
          er.setDetail(e1.getMessage());
          return Response.status(e1.getStatus()).entity(er).build();
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

    return Response.ok().entity(listResponse).build();
  }

  @Override
  public Response update(T resource, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) {
    Provider<T> provider = null;

    provider = getProvider();
    if (provider == null) {
      return BaseResourceTypeResource.super.update(resource, id, attributes, excludedAttributes);
    }

    Set<AttributeReference> attributeReferences = Optional.ofNullable(attributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());
    Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(excludedAttributes).map(wrapper -> wrapper.getAttributeReferences()).orElse(Collections.emptySet());

    if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
      return createAmbiguousAttributeParametersResponse();
    }

    endpointUtil.process(uriInfo);
    T stored;
    try {
      stored = provider.get(id);
    } catch (UnableToRetrieveResourceException e2) {
      return createGenericExceptionResponse(e2, e2.getStatus());
    }

    if (stored == null) {
      return createNotFoundResponse(id);
    }

    EntityTag backingETag = null;
    try {
      backingETag = generateEtag(stored);
    } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e1) {
      return createETagErrorResponse();
    }

    ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);

    if (evaluatePreconditionsResponse != null) {
      return createPreconditionFailedResponse(resource, evaluatePreconditionsResponse);
    }

    T updated;
    try {
      updated = provider.update(id, resource);
    } catch (UnableToUpdateResourceException e1) {
      return createGenericExceptionResponse(e1, e1.getStatus());
    }

    // Process Attributes
    try {
      resource = processFilterAttributeExtensions(provider, resource, attributeReferences, excludedAttributeReferences);
    } catch (ClientFilterException e1) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus(Integer.toString(e1.getStatus().getStatusCode()));
      er.setDetail(e1.getMessage());
      return Response.status(e1.getStatus()).entity(er).build();
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
      etag = generateEtag(updated);
    } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Failed to generate etag for newly created entity " + e.getMessage());
    }

    // TODO - Is this correct or should we support roll back semantics
    if (etag == null) {
      return Response.ok(updated).location(buildLocationTag(updated)).build();
    }

    return Response.ok(updated).location(buildLocationTag(updated)).tag(etag).build();
  }

  @Override
  public Response patch(PatchRequest patchRequest) {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.patch(patchRequest);
  }

  @Override
  public Response delete(String id) {
    try {
      Response response;
      Provider<T> provider = getProvider();

      if (provider == null) {
        response = BaseResourceTypeResource.super.delete(id);
      } else {
        endpointUtil.process(uriInfo);
        response = Response.noContent().build();

        provider.delete(id);
      }
      return response;
    } catch (UnableToDeleteResourceException e) {
      Status status = e.getStatus();
      Response response = Response.status(status).build();

      log.error("Unable to delete resource", e);

      return response;
    }
  }

  private EntityTag generateEtag(T resource) throws JsonProcessingException, NoSuchAlgorithmException, UnsupportedEncodingException {

    ObjectMapper objectMapper = new ObjectMapper();
    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Meta meta = resource.getMeta();

    if (meta == null) {
      meta = new Meta();
    }

    resource.setMeta(null);
    String writeValueAsString = objectMapper.writeValueAsString(resource);

    EntityTag etag = hash(writeValueAsString);
    meta.setVersion(etag.getValue());

    resource.setMeta(meta);

    return etag;
  }

  private T processFilterAttributeExtensions(Provider<T> provider, T resource, Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ClientFilterException {
    ScimProcessingExtension annotation = provider.getClass().getAnnotation(ScimProcessingExtension.class);
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
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    return resource;
  }

  public static EntityTag hash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(input.getBytes("UTF-8"));
    byte[] hash = digest.digest();
    return new EntityTag(Base64.getEncoder().encodeToString(hash));
  }

  private URI buildLocationTag(T resource) {
    String id = resource.getId();
    if (id == null) {
      LOG.warn("Provider must supply an id for a resource");
      id = "unknown";
    }
    return uriInfo.getAbsolutePathBuilder().path(id).build();
  }

  private Response createGenericExceptionResponse(Exception e1, Status status) {
    ErrorResponse er = new ErrorResponse();

    er.setDetail(e1.getLocalizedMessage());
    if (status != null) {
      er.setStatus(Integer.toString(status.getStatusCode()));
      return Response.status(status).entity(er).build();
    } else {
      er.setStatus("500");
      return Response.status(Status.BAD_REQUEST).entity(er).build();
    }
  }

  private Response createAmbiguousAttributeParametersResponse() {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("400");
    er.setDetail("Cannot include both attributes and excluded attributes in a single request");
    return Response.status(Status.BAD_REQUEST).entity(er).build();
  }

  private Response createNotFoundResponse(String id) {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("404");
    er.setDetail("Resource " + id + " not found");
    return Response.status(Status.NOT_FOUND).entity(er).build();
  }

  private Response createETagErrorResponse() {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("500");
    er.setDetail("Failed to generate the etag");
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
  }

  private Response createAttriubteProcessingErrorResponse(Exception e) {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("500");
    er.setDetail("Failed to parse the attribute query value " + e.getMessage());
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
  }

  private Response createPreconditionFailedResponse(T resource, ResponseBuilder evaluatePreconditionsResponse) {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("412");
    er.setDetail("Failed to update record, backing record has changed - " + resource.getId());
    log.warn("Failed to update record, backing record has changed - " + resource.getId());
    return evaluatePreconditionsResponse.entity(er).build();
  }
}
