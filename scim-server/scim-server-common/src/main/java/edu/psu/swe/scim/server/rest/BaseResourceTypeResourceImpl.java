package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.schema.ErrorResponse;

public abstract class BaseResourceTypeResourceImpl<T> implements BaseResourceTypeResource<T> {

  public abstract Provider<T> getProvider();
  
  @Inject 
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

    return Response.ok().entity(resource).build();
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
}
