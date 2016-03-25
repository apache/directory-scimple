package edu.psu.swe.scim.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.SchemaResource;

public class SchemaResourceImpl implements SchemaResource {
  
  @Override
  public Response getAllSchemas(String filter) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  @Override
  public Response getSchema(String uri) {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }
}
