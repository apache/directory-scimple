package edu.psu.swe.scim.client.rest;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

import edu.psu.swe.commons.jaxrs.RestCall;
import edu.psu.swe.scim.spec.resources.ScimUser;

public class ScimUserClient extends BaseScimClient<ScimUser> {

  private static final GenericType<List<ScimUser>> LIST_SCIM_USER = new GenericType<List<ScimUser>>(){};

  public ScimUserClient(Client client, String baseUrl) throws IllegalArgumentException {
    super(client, baseUrl, ScimUser.class, LIST_SCIM_USER);
  }

  public ScimUserClient(Client client, String baseUrl, RestCall invoke) throws IllegalArgumentException {
    super(client, baseUrl, ScimUser.class, LIST_SCIM_USER, invoke);
  }
}
