package edu.psu.swe.scim.server.utility;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.Data;

@RequestScoped
@Data
public class EndpointUtil {
  private UriBuilder baseUri;
  
  @Inject
  ProviderRegistry registry;
  
  public URI getEndpoint(Class<? extends ScimResource> resource) {
    
    URI uri = null;
    if (registry.getProvider(resource) != null) {
      uri = baseUri.path(resource.getSimpleName()).build();
    }
    
    return uri;
  }
  
  public void process(UriInfo uriInfo) {
    baseUri = uriInfo.getBaseUriBuilder();
  }
}
