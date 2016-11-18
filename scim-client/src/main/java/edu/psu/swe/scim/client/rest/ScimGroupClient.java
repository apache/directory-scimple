package edu.psu.swe.scim.client.rest;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

import edu.psu.swe.scim.spec.resources.ScimGroup;

public class ScimGroupClient extends BaseScimClient<ScimGroup> {

  private static final GenericType<List<ScimGroup>> SCIM_GROUP_LIST = new GenericType<List<ScimGroup>>(){};

  public ScimGroupClient(Client client, String baseUrl) throws IllegalArgumentException {
    super(client, baseUrl, ScimGroup.class, SCIM_GROUP_LIST);
  }
}
