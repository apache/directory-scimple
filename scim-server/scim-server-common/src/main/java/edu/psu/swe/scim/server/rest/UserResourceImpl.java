/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.ErrorResponse;

/**
 * @author shawn
 *
 */
public class UserResourceImpl implements UserResource {

	@Inject
	ProviderRegistry providerRegistry;
	
	@Override
	public Response getById(String id, String attributes) {
        Provider<ScimUser> provider = null;
        
		if ((provider = providerRegistry.getUserProvider()) == null){
		  return UserResource.super.getById(id, attributes);
		}
		
		ScimUser user = provider.get(id);
		
		//TODO - Handle attributes
		
		if (user == null){
			ErrorResponse er = new ErrorResponse();
			er.setStatus("404");
			er.setDetail("User " + id + " not found");
			return Response.status(Status.NOT_FOUND).entity(er).build();
		}
		
		return Response.ok().entity(user).build();
	}

	@Override
	public Response query(String attributes, String filter, String sortBy, String sortOrder, Integer startIndex,
			Integer count) {
		// TODO Auto-generated method stub
		return UserResource.super.query(attributes, filter, sortBy, sortOrder, startIndex, count);
	}

	@Override
	public Response create(ScimUser resource) {
        
		Provider<ScimUser> provider = null;
        
		if ((provider = providerRegistry.getUserProvider()) == null){
			return UserResource.super.create(resource);
		}
		
		ScimUser user = provider.create(resource);
		
		return Response.status(Status.CREATED).entity(user).build();
	}

	@Override
	public Response find(SearchRequest request) {
		// TODO Auto-generated method stub
		return UserResource.super.find(request);
	}

	@Override
	public Response update(ScimUser resource) {
        
		Provider<ScimUser> provider = null;
        
		if ((provider = providerRegistry.getUserProvider()) == null){
		  return UserResource.super.update(resource);
		}
		
		ScimUser user = provider.update(resource);
		
		return Response.ok(user).build();
	}

	@Override
	public Response patch() {
		// TODO Auto-generated method stub
		return UserResource.super.patch();
	}

	@Override
	public Response delete(String id) {
		// TODO Auto-generated method stub
		return UserResource.super.delete(id);
	}

}
