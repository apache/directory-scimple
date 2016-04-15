package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;

public interface Provider<T> {
  T create(T resource) throws UnableToCreateResourceException;
  T update(T resource) throws UnableToUpdateResourceException;
  T get(String id) throws UnableToRetrieveResourceException;
  void delete(String id) throws UnableToDeleteResourceException;
  List<T> find(SearchRequest request) throws UnableToRetrieveResourceException;
  
  List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException;
}
