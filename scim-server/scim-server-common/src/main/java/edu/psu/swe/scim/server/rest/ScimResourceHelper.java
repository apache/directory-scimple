package edu.psu.swe.scim.server.rest;

import java.util.HashSet;
import java.util.Set;

public class ScimResourceHelper {

  private ScimResourceHelper() {
    
  }
  
  public static Set<Class<?>> getScimClassesToLoad(){
    Set<Class<?>> clazzez_ = new HashSet<>();
    
     //Required scim classes.
    clazzez_.add(SchemaResourceImpl.class);
    
    return clazzez_;
  }
  
}
