package edu.psu.swe.scim.server.rest;

import javax.ws.rs.core.Response;

import edu.psu.swe.scim.spec.protocol.ResourceTypesResource;

public class ResourceTypesResourceImpl implements ResourceTypesResource {

  @Override
  public Response getAllResourceTypes(String filter) {
    // TODO Auto-generated method stub
    return ResourceTypesResource.super.getAllResourceTypes(filter);
  }

  @Override
  public Response getResourceType(String id) {
    // TODO Auto-generated method stub
    return ResourceTypesResource.super.getResourceType(id);
  }

}
