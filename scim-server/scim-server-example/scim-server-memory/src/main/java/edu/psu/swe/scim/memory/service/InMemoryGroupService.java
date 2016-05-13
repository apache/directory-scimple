package edu.psu.swe.scim.memory.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;

@Named
@ApplicationScoped
public class InMemoryGroupService implements Provider<ScimGroup> {

  private Map<String, ScimGroup> groups = new HashMap<>();
  
  @Override
  public ScimGroup create(ScimGroup resource) {
    String resourceId = resource.getId();
    int resourceHashCode = resource.hashCode();
    String id = resourceId != null ? resourceId : Integer.toString(resource.hashCode());
    int offset = 1;

    while (groups.containsKey(id)) {
      id = Integer.toString(resourceHashCode + offset);
      offset += 1;
    }
    groups.put(id, resource);
    resource.setId(id);
    return resource;
  }

  @Override
  public ScimGroup update(String id, ScimGroup resource) {
    groups.put(id, resource);
    return resource;
  }

  @Override
  public ScimGroup get(String id) {
    return groups.get(id);
  }

  @Override
  public void delete(String id) {
    groups.remove(id);
  }

  @Override
  public List<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
    return new ArrayList<ScimGroup>(groups.values());
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }
}
