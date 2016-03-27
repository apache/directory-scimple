/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.provider.UserProvider;
import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * @author shawn
 *
 */
public class UserResourceImpl implements UserResource {

	@Inject
	ProviderRegistry providerRegistry;
	
	@Override
	public Response getById(String id, String attributes) {
        UserProvider provider = null;
        
		if ((provider = providerRegistry.getUserProfider()) == null){
		  return UserResource.super.getById(id, attributes);
		}
		
		ScimUser user = provider.getUser(id);
		
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
		// TODO Auto-generated method stub
		return UserResource.super.create(resource);
	}

	@Override
	public Response find(SearchRequest request) {
		// TODO Auto-generated method stub
		return UserResource.super.find(request);
	}

	@Override
	public Response update(ScimUser resource) {
		// TODO Auto-generated method stub
		return UserResource.super.update(resource);
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
