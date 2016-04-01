package edu.psu.swe.scim.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.ResourceTypesResource;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.schema.ResourceType;

@Stateless
public class ResourceTypesResourceImpl implements ResourceTypesResource {

  @Inject
  private Registry registry;
  
  @Override
  public Response getAllResourceTypes(String filter) {
    
    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }

    Collection<ResourceType> resourceTypes = registry.getAllResourceTypes();
    
    ListResponse listResponse = new ListResponse();
    listResponse.setItemsPerPage(resourceTypes.size());
    listResponse.setStartIndex(0);
    listResponse.setTotalResults(resourceTypes.size());
    
    List<Object> objectList = new ArrayList<>(resourceTypes);
    listResponse.setResources(objectList);
    
    return Response.ok(listResponse).build();
  }

  @Override
  public Response getResourceType(String id) {
    ResourceType resourceType = registry.getResourceType(id);
    if (resourceType == null){
      return Response.status(Status.NOT_FOUND).build();  
    }
    
    return Response.ok(resourceType).build();
  }

}
