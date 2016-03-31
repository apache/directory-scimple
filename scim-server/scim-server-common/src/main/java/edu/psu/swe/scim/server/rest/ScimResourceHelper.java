package edu.psu.swe.scim.server.rest;

import java.util.HashSet;
import java.util.Set;

public class ScimResourceHelper {

  private ScimResourceHelper() {
    
  }
  
  public static Set<Class<?>> getScimClassesToLoad(){
    Set<Class<?>> clazzez = new HashSet<>();
    
     //Required scim classes.
    clazzez.add(BulkResourceImpl.class);
    clazzez.add(GroupResourceImpl.class);
    clazzez.add(ResourceTypesResourceImpl.class);
    clazzez.add(SchemaResourceImpl.class);
    clazzez.add(SearchResourceImpl.class);
    clazzez.add(SelfResourceImpl.class);
    clazzez.add(ServiceProviderConfigResourceImpl.class);
    clazzez.add(UserResourceImpl.class);
    
    return clazzez;
  }
  
}
