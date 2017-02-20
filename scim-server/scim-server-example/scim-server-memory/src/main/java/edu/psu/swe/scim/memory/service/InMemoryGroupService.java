package edu.psu.swe.scim.memory.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.UpdateRequest;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
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
    int idCandidate = resource.hashCode();
    String id = resourceId != null ? resourceId : Integer.toString(idCandidate);

    while (groups.containsKey(id)) {
      id = Integer.toString(idCandidate);
      ++idCandidate;
    }
    groups.put(id, resource);
    resource.setId(id);
    return resource;
  }

  @Override
  public ScimGroup update(UpdateRequest<ScimGroup> updateRequest) throws UnableToUpdateResourceException {
    String id = updateRequest.getId();
    ScimGroup resource = updateRequest.getResource();
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
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
    return new FilterResponse<ScimGroup>(groups.values(), pageRequest, groups.size());
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }

}
