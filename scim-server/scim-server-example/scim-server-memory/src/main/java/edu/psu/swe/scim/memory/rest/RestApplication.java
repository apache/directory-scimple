package edu.psu.swe.scim.memory.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.psu.swe.scim.server.rest.ScimResourceHelper;

@ApplicationPath("v2")
public class RestApplication extends Application {
  
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> clazzes = new HashSet<Class<?>>();
    
    clazzes.addAll(ScimResourceHelper.getScimClassesToLoad());
    clazzes.addAll(ScimResourceHelper.getSwaggerClassesToLoad());

    return clazzes;
  }
  
}
