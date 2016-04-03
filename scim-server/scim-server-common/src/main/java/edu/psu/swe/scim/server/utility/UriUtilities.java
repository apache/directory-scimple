package edu.psu.swe.scim.server.utility;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class UriUtilities
{
  public static String urlAsString(UriInfo uriInfo, boolean secured) throws MalformedURLException
  {
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.build();
    URL url;
    
    url = uri.toURL();
    url = new URL("https", url.getHost(), url.getPort(), url.getFile());
    
    return url.toString();
  }
}
