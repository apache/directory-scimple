/**
 * 
 */
package edu.psu.swe.scim.server.rest;

import javax.ws.rs.core.Response;

import edu.psu.swe.scim.spec.protocol.UserResource;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * @author shawn
 *
 */
public class UserResourceImpl implements UserResource {

	@Override
	public Response getById(String id, String attributes) {
		// TODO Auto-generated method stub
		return UserResource.super.getById(id, attributes);
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
