package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.schema.ResourceType;

public interface Provider<T> {
  T create(T resource);
  T update(T resource);
  T get(String id);
  void delete(String id);
  List<T> find(SearchRequest request);
  
  ResourceType getResourceType();
}
