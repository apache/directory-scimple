package edu.psu.swe.scim.server.rest;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides the SCIM defined set of end-points and resources without declaring
 * a JAX-RS application.  Additional end-points and extensions can be added
 * by the implementing class.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
public final class ScimResourceHelper {

  private ScimResourceHelper() {
    // Make this a utility class
  }
  
  /**
   * Provides a set of JAX-RS annotated classes for the basic SCIM protocol
   * functionality.
   * 
   * @return the JAX-RS annotated classes.
   */
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
    
    clazzez.add(ObjectMapperContextResolver.class);
    
    return clazzez;
  }
  
}
