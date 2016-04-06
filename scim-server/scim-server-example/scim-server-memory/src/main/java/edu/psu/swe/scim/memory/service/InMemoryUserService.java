package edu.psu.swe.scim.memory.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.psu.swe.scim.memory.extensions.LuckyNumberExtension;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimUser;

public class InMemoryUserService implements Provider<ScimUser> {

  private Map<String, ScimUser> users = new HashMap<>();
  
  @Override
  public ScimUser create(ScimUser resource) {
    users.put(resource.getId(), resource);
    return resource;
  }

  @Override
  public ScimUser update(ScimUser resource) {
    users.put(resource.getId(), resource);
    return resource;
  }

  @Override
  public ScimUser get(String id) {
    return users.get(id);
  }

  @Override
  public void delete(String id) {
    users.remove(id);
  }

  @Override
  public List<ScimUser> find(SearchRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Arrays.asList(LuckyNumberExtension.class);
  }

}
