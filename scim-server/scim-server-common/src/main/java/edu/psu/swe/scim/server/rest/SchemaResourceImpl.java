package edu.psu.swe.scim.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.SchemaResource;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.schema.Schema;

@Stateless
public class SchemaResourceImpl implements SchemaResource {
  
  @Inject
  Registry registry;
  
  @Override
  public Response getAllSchemas(String filter) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    ListResponse listResponse = new ListResponse();
    Collection<Schema> schemas = registry.getAllSchemas();
    
    listResponse.setItemsPerPage(schemas.size());
    listResponse.setStartIndex(1);
    listResponse.setTotalResults(schemas.size());
    
    List<Object> objectList = new ArrayList<>(schemas);
    listResponse.setResources(objectList);
    
    return Response.ok(listResponse).build();
  }

  @Override
  public Response getSchema(String urn) {
    
    Schema schema = registry.getSchema(urn);
    if (schema == null){
      return Response.status(Status.NOT_FOUND).build();  
    }
    
    return Response.ok(schema).build();
    
  }
}
