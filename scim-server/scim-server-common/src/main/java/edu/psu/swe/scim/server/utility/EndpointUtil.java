package edu.psu.swe.scim.server.utility;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.UriInfo;

import lombok.Data;

@RequestScoped
@Data
public class EndpointUtil {
  private URI groupEndpoint;
  private URI userEndpoint;
  
  public void process(UriInfo uriInfo) {
    groupEndpoint = uriInfo.getBaseUriBuilder().path("Groups").build();
    userEndpoint = uriInfo.getBaseUriBuilder().path("Users").build();
  }
}
