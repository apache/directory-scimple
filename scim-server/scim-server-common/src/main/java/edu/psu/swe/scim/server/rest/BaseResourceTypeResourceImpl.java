package edu.psu.swe.scim.server.rest;

import java.io.StringWriter;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.utility.AttributeUtil;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;
import edu.psu.swe.scim.spec.schema.Meta;
import lombok.extern.slf4j.Slf4j;

@Stateless
@Slf4j
public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

  private static final String LOCATION_TAG = "Location";
  private static final String ETAG_TAG = "Etag";
  
  public abstract Provider<T> getProvider();
  
  @Context
  UriInfo uriInfo;
  
  @Context
  Request request;
  
  @Inject
  AttributeUtil attributeUtil;
  
  @Override
  public Response getById(String id, String attributes, String excludedAttributes) {
    Provider<T> provider = null;

    if ((provider = getProvider()) == null) {
      return BaseResourceTypeResource.super.getById(id, attributes, excludedAttributes);
    }

    if (StringUtils.isNotEmpty(attributes) && StringUtils.isNotEmpty(excludedAttributes)) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("400");
      er.setDetail("Cannot include both attributes and excluded attributes in a single request");
      return Response.status(Status.BAD_REQUEST).entity(er).build();
    }
    
    T resource = provider.get(id);

    if (resource == null) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("404");
      er.setDetail("User " + id + " not found");
      return Response.status(Status.NOT_FOUND).entity(er).build();
    }

    EntityTag etag = null;
    
    try {
      etag = generateEtag(resource);
    } catch (JAXBException e) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("500");
      er.setDetail("Failed to generate the etag");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
    }
        
    // Process Attributes 
    try {
      if (StringUtils.isNotEmpty(excludedAttributes)) {
          resource = attributeUtil.setExcludedAttributesForDisplay(resource, excludedAttributes);
      } else {
        resource = attributeUtil.setAttributesForDisplay(resource, excludedAttributes);
      }
      
      return Response.ok().entity(resource).header(LOCATION_TAG, buildLocationTag(resource)).header(ETAG_TAG, etag).build();
    } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException e) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("500");
      er.setDetail("Failed to parse the attribute query value " + e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
    }
    
    
  }

  @Override
  public Response query(String attributes, String excludedAttributes, String filter, String sortBy, String sortOrder, Integer startIndex, Integer count) {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.query(attributes, excludedAttributes, filter, sortBy, sortOrder, startIndex, count);
  }

  @Override
  public Response create(T resource) {
    Provider<T> provider = null;

    if ((provider = getProvider()) == null) {
      return BaseResourceTypeResource.super.create(resource);
    }

    T created = provider.create(resource);

    EntityTag etag = null;
    try {
      etag = generateEtag(created);
    } catch (JAXBException e) {
      log.error("Failed to generate etag for newly created entity " + e.getMessage());
    }
    
    // Process Attributes 
    try {
      created = attributeUtil.setAttributesForDisplay(resource, "");
    } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException e) {
      if (etag == null) {
        return Response.status(Status.CREATED).header(LOCATION_TAG, buildLocationTag(resource)).build();
      } else {
        Response.status(Status.CREATED).header(LOCATION_TAG, buildLocationTag(resource)).header(ETAG_TAG, etag).build();
      }
    }
    
    //TODO - Is this the right behavior?
    if (etag == null) {
      return Response.status(Status.CREATED).header(LOCATION_TAG, buildLocationTag(resource)).entity(created).build();
    }
    
    return Response.status(Status.CREATED).header(LOCATION_TAG, buildLocationTag(resource)).header(ETAG_TAG, etag).entity(created).build();
  }

  @Override
  public Response find(SearchRequest request) {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.find(request);
  }

  @Override
  public Response update(T resource) {
    Provider<T> provider = null;

    if ((provider = getProvider()) == null) {
      return BaseResourceTypeResource.super.update(resource);
    }

    T stored = provider.get(resource.getId());
    
    EntityTag backingETag = null;
    try {
      backingETag = generateEtag(stored);
    } catch (JAXBException e1) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("500");
      er.setDetail("Failed to calculate etag for backing entity " + e1.getMessage());
      log.error("Failed to calculate etag for backing entity " + e1.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
    }
    
    ResponseBuilder evaluatePreconditionsResponse = request.evaluatePreconditions(backingETag);
    
    if (evaluatePreconditionsResponse != null){
      ErrorResponse er = new ErrorResponse();
      er.setStatus("412");
      er.setDetail("Failed to update record, backing record has changed - " + resource.getId());
      log.warn("Failed to update record, backing record has changed - " + resource.getId());
      return evaluatePreconditionsResponse.entity(er).build();
    }

    T updated = provider.update(resource);

    // Process Attributes 
    try {
      updated = attributeUtil.setAttributesForDisplay(resource, "");
    } catch (IllegalArgumentException | IllegalAccessException | AttributeDoesNotExistException e) {
      log.error("Failed to handle attribute processing in update " + e.getMessage());
    }
    
    EntityTag etag = null;
    try {
      etag = generateEtag(updated);
    } catch (JAXBException e) {
      log.error("Failed to generate etag for newly created entity " + e.getMessage());
    }
    
    //TODO - Is this correct or should we support roll back semantics
    if (etag == null) {
      return Response.ok(updated).header(LOCATION_TAG, buildLocationTag(resource)).build();
    }
    
    return Response.ok(updated).header(LOCATION_TAG, buildLocationTag(resource)).header(ETAG_TAG, etag).build();
  }

  @Override
  public Response patch() {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.patch();
  }

  @Override
  public Response delete(String id) {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.delete(id);
  }
  
  private EntityTag generateEtag(T resource) throws JAXBException {
    
    JAXBContext context = null;
    
    context = JAXBContext.newInstance(resource.getClass());
    
    Marshaller marshaller = context.createMarshaller(); 
        
    StringWriter sw = new StringWriter();
    
    Meta meta = resource.getMeta();

    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    resource.setMeta(null);
    marshaller.marshal(resource, sw);
    
    String etag = Integer.toString(sw.toString().hashCode());
    meta.setVersion(etag);

    resource.setMeta(meta);
    
    return EntityTag.valueOf(etag.toString());
  }
  
  private String buildLocationTag(T resource) {
    return uriInfo.getAbsolutePathBuilder().path(resource.getId()).build().toASCIIString();
  }
}
