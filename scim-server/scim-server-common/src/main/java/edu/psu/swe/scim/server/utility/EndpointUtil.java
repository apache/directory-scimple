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
  
  public URI getEndpointUri(Class<? extends ScimResource> resource) {
    
    URI uri = null;
    if (registry.getProvider(resource) != null) {
      uri = baseUri.path(resource.getSimpleName()).build();
    }
    
    return uri;
  }
  
  public UriBuilder getEndpointUriBuilder(Class<? extends ScimResource> resource) {
    
    UriBuilder uriBuilder = null;
    if (registry.getProvider(resource) != null) {
      uriBuilder = baseUri.path(resource.getSimpleName());
    }
    
    return uriBuilder;
  }
  
  public void process(UriInfo uriInfo) {
    baseUri = uriInfo.getBaseUriBuilder();
  }
}
