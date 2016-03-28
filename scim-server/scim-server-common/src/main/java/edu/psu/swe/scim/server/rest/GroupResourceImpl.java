/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.GroupResource;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimGroup;

/**
 * @author shawn
 *
 */
public class GroupResourceImpl implements GroupResource {

	@Inject
	ProviderRegistry providerRegistry;
	
	@Override
	public Response getById(String id, String attributes) {
        Provider<ScimGroup> provider = null;
        
		if ((provider = providerRegistry.getGroupProvider()) == null){
		  return GroupResource.super.getById(id, attributes);
		}
		
		ScimGroup group = provider.get(id);
		
		//TODO - Handle attributes
		
		if (group == null){
			ErrorResponse er = new ErrorResponse();
			er.setStatus("404");
			er.setDetail("group " + id + " not found");
			return Response.status(Status.NOT_FOUND).entity(er).build();
		}
		
		return Response.ok().entity(group).build();
	}

	@Override
	public Response query(String attributes, String filter, String sortBy, String sortOrder, Integer startIndex,
			Integer count) {
		// TODO Auto-generated method stub
		return GroupResource.super.query(attributes, filter, sortBy, sortOrder, startIndex, count);
	}

	@Override
	public Response create(ScimGroup resource) {
        
		Provider<ScimGroup> provider = null;
        
		if ((provider = providerRegistry.getGroupProvider()) == null){
			return GroupResource.super.create(resource);
		}
		
		ScimGroup group = provider.create(resource);
		
		return Response.status(Status.CREATED).entity(group).build();
	}

	@Override
	public Response find(SearchRequest request) {
		// TODO Auto-generated method stub
		return GroupResource.super.find(request);
	}

	@Override
	public Response update(ScimGroup resource) {
        
		Provider<ScimGroup> provider = null;
        
		if ((provider = providerRegistry.getGroupProvider()) == null){
		  return GroupResource.super.update(resource);
		}
		
		ScimGroup group = provider.update(resource);
		
		return Response.ok(group).build();
	}

	@Override
	public Response patch() {
		// TODO Auto-generated method stub
		return GroupResource.super.patch();
	}

	@Override
	public Response delete(String id) {
		// TODO Auto-generated method stub
		return GroupResource.super.delete(id);
	}

}
