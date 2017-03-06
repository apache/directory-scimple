package edu.psu.swe.scim.server.utility;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.exception.InvalidScimResourceException;
import edu.psu.swe.scim.spec.resources.ScimResource;

@RequestScoped
public class EndpointUtil {
  private URI baseUri;
  
  public UriBuilder getBaseUriBuilder() {
    return UriBuilder.fromUri(baseUri);
  }
  
  public UriBuilder getEndpointUriBuilder(Class<? extends ScimResource> resource) {
    ScimResourceType[] sr = resource.getAnnotationsByType(ScimResourceType.class);
    
    if (baseUri == null) {
      throw new IllegalStateException("BaseUri for Resource "+resource+" was null");
    }

    if (sr.length == 0 || sr.length > 1) {
      throw new InvalidScimResourceException("ScimResource class must have a ScimResourceType annotation");
    }

    // yuck! TODO where to get REST endpoint from?
    String resourceName = sr[0].name() + "s";  
    
    return UriBuilder.fromUri(baseUri).path(resourceName);
  }
  
  public void process(UriInfo uriInfo) {
    baseUri = uriInfo.getBaseUri();
  }
}
