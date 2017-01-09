package edu.psu.swe.scim.client.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

import edu.psu.swe.commons.jaxrs.RestCall;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.resources.ScimGroup;

public class ScimGroupClient extends BaseScimClient<ScimGroup> {

  private static final GenericType<ListResponse<ScimGroup>> SCIM_GROUP_LIST = new GenericType<ListResponse<ScimGroup>>(){};

  public ScimGroupClient(Client client, String baseUrl) {
    super(client, baseUrl, ScimGroup.class, SCIM_GROUP_LIST);
  }

  public ScimGroupClient(Client client, String baseUrl, RestCall invoke) {
    super(client, baseUrl, ScimGroup.class, SCIM_GROUP_LIST, invoke);
  }
}
