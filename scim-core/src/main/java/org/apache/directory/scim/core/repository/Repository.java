/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.core.repository;

import java.util.Collections;
import java.util.List;

import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;

/**
 * Defines the interface between the SCIM protocol implementation and the
 * Repository implementation for type T.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 *
 * @param <T> a SCIM ResourceType that extends ScimResource
 */
public interface Repository<T extends ScimResource> {

  /**
   * Returns the type of ScimResource this repository manages.
   * @return The type of resource this repository manages.
   */
  Class<T> getResourceClass();

  /**
   * Allows the SCIM server's REST implementation to create a resource via
   * a POST to a valid end-point.
   * 
   * @param resource The ScimResource to create and persist.
   * @return The newly created ScimResource.
   * @throws ResourceException When the ScimResource cannot be
   *         created.
   */
  T create(T resource) throws ResourceException;
  
  /**
   * Allows the SCIM server's REST implementation to update and existing
   * resource via a PUT to a valid end-point.
   * 
   * @param updateRequest The ScimResource to update and persist.
   * @return The newly updated ScimResource.
   * @throws ResourceException When the ScimResource cannot be
   *         updated.
   */
  T update(UpdateRequest<T> updateRequest) throws ResourceException;
  
  /**
   * Retrieves the ScimResource associated with the provided identifier.
   * @param id The identifier of the target ScimResource.
   * @return The requested ScimResource.
   * @throws ResourceException When the ScimResource cannot be
   *         retrieved.
   */
  T get(String id) throws ResourceException;
  
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
   * @throws ResourceException If one or more ScimResources
   *         cannot be retrieved.
   */
  FilterResponse<T> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException;
  
  /**
   * Deletes the ScimResource with the provided identifier (if it exists).
   * This interface makes no distinction between hard and soft deletes but
   * rather leaves that to the designer of the persistence layer.
   * 
   * @param id The ScimResource's identifier.
   * @throws ResourceException When the specified ScimResource
   *         cannot be deleted.
   */
  void delete(String id) throws ResourceException;

  /**
   * Returns a list of the SCIM Extensions that this repository considers to be
   * associated with the ScimResource of type T.
   * 
   * @return A list of ScimExtension classes.
   * @throws InvalidRepositoryException If the repository cannot return
   *         the appropriate list.
   */
  default List<Class<? extends ScimExtension>> getExtensionList() throws InvalidRepositoryException {
    return Collections.emptyList();
  }
}
