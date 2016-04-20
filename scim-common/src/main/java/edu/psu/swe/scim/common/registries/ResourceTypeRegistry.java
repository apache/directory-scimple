package edu.psu.swe.scim.common.registries;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.psu.swe.scim.spec.schema.ResourceType;

/**
 * Provides a registry for ResourceTypes, whether defined in the schema
 * specification or from implementors new ResourceTypes. In most applications,
 * this class should be instantiated as a singleton at application start.
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
public class ResourceTypeRegistry {

  Map<String, ResourceType> registry;

  public ResourceTypeRegistry() {
    registry = new ConcurrentHashMap<>();
    // TODO - default ResourceTypes from scim-spec-schema
  }

  /**
   * Adds a ResourceType to the registry.
   * 
   * @param resourceType
   *          the ResourceType to register.
   */
  public void add(ResourceType resourceType) {
    String key = resourceType.getId().getValue();
    if (key == null) {
      key = resourceType.getName();
    }
    registry.put(key, resourceType);
  }

  /**
   * Adds a Set of ResourceType objects to the registry.
   * 
   * @param resourceTypeSet
   *          the ResourceType objects to register.
   */
  public void addAll(Set<ResourceType> resourceTypeSet) {
    resourceTypeSet.forEach(r -> add(r));
  }

}
