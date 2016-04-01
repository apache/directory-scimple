package edu.psu.swe.scim.server.provider;

import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import lombok.Data;
import edu.psu.swe.scim.spec.resources.ScimResource;

@Singleton
@Startup
@Data
public class ProviderRegistry {
  
  public Map<Class<? extends ScimResource>, Provider<? extends ScimResource>> providerMap;
  
  public <T extends ScimResource> void registerProvider(Class<T> clazz, Provider<T> provider) {
    providerMap.put(clazz, provider);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends ScimResource> Provider<T> getProvider(Class<T> clazz) {
    return (Provider<T>) providerMap.get(clazz);
  }
  
//  private Provider<ScimGroup> groupProvider = null;
//  private Provider<ScimUser> userProvider = null;
}
