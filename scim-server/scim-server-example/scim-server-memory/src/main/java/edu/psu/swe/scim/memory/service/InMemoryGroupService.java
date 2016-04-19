package edu.psu.swe.scim.memory.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.psu.swe.scim.memory.extensions.LuckyNumberExtension;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;

@Named
@ApplicationScoped
public class InMemoryGroupService implements Provider<ScimGroup> {

  private Map<String, ScimGroup> groups = new HashMap<>();
  
  @Override
  public ScimGroup create(ScimGroup resource) {
    groups.put(resource.getId(), resource);
    return resource;
  }

  @Override
  public ScimGroup update(ScimGroup resource) {
    groups.put(resource.getId(), resource);
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
  public List<ScimGroup> find(SearchRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }

}
