package edu.psu.swe.scim.server.rest;

import java.io.StringWriter;
import java.net.URI;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;
import edu.psu.swe.scim.spec.schema.Meta;

public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

  private static final String LOCATION_TAG = "Location";
  private static final String ETAG_TAG = "Etag";
  
  public abstract Provider<T> getProvider();
  
  @Context
  UriInfo uriInfo;
  
  @Override
  public Response getById(String id, String attributes) {
    Provider<T> provider = null;

    if ((provider = getProvider()) == null) {
      return BaseResourceTypeResource.super.getById(id, attributes);
    }

    T resource = provider.get(id);

    // TODO - Handle attributes

    if (resource == null) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("404");
      er.setDetail("User " + id + " not found");
      return Response.status(Status.NOT_FOUND).entity(er).build();
    }

    URI uri = uriInfo.getAbsolutePath();
    String  uriString = uri.toASCIIString() + "/" + resource.getId();
    
    Meta meta = resource.getMeta();
    String etag = null;
    
    try {
      etag = generateEtag(resource);
    } catch (JAXBException e) {
      ErrorResponse er = new ErrorResponse();
      er.setStatus("500");
      er.setDetail("Failed to generate the etag");
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(er).build();
    }
    
    meta.setVersion(etag);
    resource.setMeta(meta);
    
    return Response.ok().entity(resource).header(LOCATION_TAG, uriString).header(ETAG_TAG, etag).build();
  }

  @Override
  public Response query(String attributes, String filter, String sortBy, String sortOrder, Integer startIndex, Integer count) {
    // TODO Auto-generated method stub
    return BaseResourceTypeResource.super.query(attributes, filter, sortBy, sortOrder, startIndex, count);
  }

  @Override
  public Response create(T resource) {
    Provider<T> provider = null;

    if ((provider = getProvider()) == null) {
      return BaseResourceTypeResource.super.create(resource);
    }

    T created = provider.create(resource);

    return Response.status(Status.CREATED).entity(created).build();
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

    T updated = provider.update(resource);

    return Response.ok(updated).build();
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
  
  private String generateEtag(T resource) throws JAXBException {
    
    JAXBContext context = null;
    
    context = JAXBContext.newInstance(resource.getClass());
    
    Marshaller marshaller = context.createMarshaller(); 
        
    StringWriter sw = new StringWriter();
    
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    resource.setMeta(null);
    marshaller.marshal(resource, sw);
    
    Integer etag = sw.toString().hashCode();
    return etag.toString();
    
  }
}
