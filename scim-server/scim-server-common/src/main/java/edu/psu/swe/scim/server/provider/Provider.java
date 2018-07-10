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

package edu.psu.swe.scim.server.provider;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.rest.BaseResourceTypeResourceImpl;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;

/**
 * Defines the interface between the SCIM protocol implementation and the
 * Provider implementation for type T.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 *
 * @param <T> a SCIM ResourceType that extends ScimResource
 */
public interface Provider<T extends ScimResource> {
  
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
  T update(UpdateRequest<T> updateRequest) throws UnableToUpdateResourceException;
  
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
  FilterResponse<T> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException;
  
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

  /**
   * <p>In the case where the provider throws an unhandled exception, this
   * method will be passed that exception in order for the provider to convert
   * it into the desired response.</p>
   * <p>The returned response SHOULD fulfill the requirements for SCIM error
   * responses as defined in <a
   * href="https://tools.ietf.org/html/rfc7644#section-3.12">3.12. HTTP Status
   * and Error Response Handling</a> of the SCIM specification.</p>
   * <p>By default, exceptions are converted into a <code>500 Internal Server
   * Error</code>.</p>
   * @param unhandled
   * @return
   */
  default Response handleException(Throwable unhandled) {
    return BaseResourceTypeResourceImpl.createGenericExceptionResponse(unhandled, Status.INTERNAL_SERVER_ERROR);
  }
}
