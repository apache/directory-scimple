package edu.psu.swe.scim.client.rest.legacy;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import edu.psu.swe.commons.jaxrs.RestCall;
import edu.psu.swe.scim.client.rest.ScimGroupClient;

public class Version1ScimGroupClient extends ScimGroupClient {

  public Version1ScimGroupClient(Client client, String baseUrl) {
    super(client, baseUrl);
  }

  public Version1ScimGroupClient(Client client, String baseUrl, RestCall invoke) {
    super(client, baseUrl, invoke);
  }
  
  @Override
  protected String getContentType() {
    return MediaType.APPLICATION_JSON;
  }
}
