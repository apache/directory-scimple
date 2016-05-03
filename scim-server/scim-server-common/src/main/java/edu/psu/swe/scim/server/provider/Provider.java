package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;

/**
 * Defines the interface between the SCIM protocol implementation and the
 * Provider implementation for type T.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 *
 * @param <T> a SCIM ResourceType that extends ScimResource
 */
public interface Provider<T> {
  
  /**
   * Allows the SCIM server's REST implementation to create a resource via
   * a POST to a valid end-point.
   * 
   * @param resource The ScimResource to create and persist.
   * @return The newly created ScimResource.
   * @throws UnableToCreateResourceException When the ScimResource cannot be
   *         created.
   */
  T create(T resource) throws UnableToCreateResourceException;
  
  /**
   * Allows the SCIM server's REST implementation to update and existing
   * resource via a PUT to a valid end-point.
   * 
   * @param resource The ScimResource to update and persist.
   * @return The newly updated ScimResource.
   * @throws UnableToUpdateResourceException When the ScimResource cannot be
   *         updated.
   */
  T update(String id, T resource) throws UnableToUpdateResourceException;
  
  /**
   * Retrieves the ScimResource associated with the provided identifier.
   * @param id The identifier of the target ScimResource.
   * @return The requested ScimResource.
   * @throws UnableToRetrieveResourceException When the ScimResource cannot be
   *         retrieved.
   */
  T get(String id) throws UnableToRetrieveResourceException;
  
  /**
   * Finds and retrieves all ScimResource objects known to the persistence
   * layer that match the criteria specified by the passed Filter.  The results
   * may be truncated by the scope specified by the passed PageRequest and
   * the order of the returned resources may be controlled by the passed
   * SortRequest.
   * 
   * @param filter The filter that determines the ScimResources that will be
   *        part of the ResultList
   * @param pageRequest For paged requests, this object specifies the start
   *        index and number of ScimResources that should be returned.
   * @param sortRequest Specifies which fields the returned ScimResources
   *        should be sorted by and whether the sort order is ascending or
   *        descending.
   * @return A list of the ScimResources that pass the filter criteria,
   *         truncated to match the requested "page" and sorted according
   *         to the provided requirements.
   * @throws UnableToRetrieveResourceException If one or more ScimResouces
   *         cannot be retrieved.
   */
  List<T> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException;
  
  /**
   * Deletes the ScimResource with the provided identifier (if it exists).
   * This interface makes no distinction between hard and soft deletes but
   * rather leaves that to the designer of the persistence layer.
   * 
   * @param id The ScimResource's identifier.
   * @throws UnableToDeleteResourceException When the specified ScimResource
   *         cannot be deleted.
   */
  void delete(String id) throws UnableToDeleteResourceException;

  /**
   * Returns a list of the SCIM Extensions that this provider considers to be
   * associated with the ScimResource of type T.
   * 
   * @return A list of ScimExtension classes.
   * @throws UnableToRetrieveExtensionsException If the provider cannot return
   *         the appropriate list.
   */
  List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException;
  
}
