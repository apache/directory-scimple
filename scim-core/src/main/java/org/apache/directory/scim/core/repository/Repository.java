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
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.patch.PatchOperation;
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
   * @param requestContext the context object holding additional information about the request.
   * @return The newly created ScimResource.
   * @throws ResourceException When the ScimResource cannot be
   *         created.
   */
  T create(T resource, ScimRequestContext requestContext) throws ResourceException;

  /**
   * Allows the SCIM server's REST implementation to update an existing
   * resource via a PUT to a valid end-point.
   * <br>
   * <b>SCIM Implementation NOTE:</b> SCIM supports <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.14">versioning of resources via HTTP ETags</a>, if the (optional) {@code version} parameter is present, (and supported by the server),
   * it can be used as a mechanism for caching and to ensure clients do not inadvertently overwrite other changes.
   *
   *
   * @param id the identifier of the ScimResource to update and persist.
   * @param resource an updated resource to persist.
   * @param requestContext the context object holding additional information about the request.
   * @return The newly updated ScimResource.
   * @throws ResourceException When the ScimResource cannot be updated.
   */
  T update(String id, T resource, ScimRequestContext requestContext) throws ResourceException;

  /**
   * Allows the SCIM server's REST implementation to update an existing
   * resource via a PATCH to a valid end-point.
   * <br>
   * <b>SCIM Implementation NOTE:</b> SCIM supports <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.14">versioning of resources via HTTP ETags</a>, if the (optional) {@code version} parameter is present, (and supported by the server),
   * it can be used as a mechanism for caching and to ensure clients do not inadvertently overwrite other changes.
   *
   * @param id the identifier of the ScimResource to update and persist.
   * @param patchOperations a list of patch operations to apply to an existing resource.
   * @param requestContext the context object holding additional information about the request.
   * @return The newly updated ScimResource.
   * @throws ResourceException When the ScimResource cannot be updated.
   */
  T patch(String id, List<PatchOperation> patchOperations, ScimRequestContext requestContext) throws ResourceException;

  /**
   * Retrieves the ScimResource associated with the provided identifier.
   * @param id The identifier of the target ScimResource.
   * @param requestContext the context object holding additional information about the request.
   * @return The requested ScimResource.
   * @throws ResourceException When the ScimResource cannot be
   *         retrieved.
   */
  T get(String id, ScimRequestContext requestContext) throws ResourceException;
  
  /**
   * Finds and retrieves all ScimResource objects known to the persistence
   * layer that match the criteria specified by the passed Filter.  The results
   * may be truncated by the scope specified by the passed PageRequest and
   * the order of the returned resources may be controlled by the passed
   * SortRequest.
   *
   * <p><b>Sorting:</b> If the request context contains a {@link org.apache.directory.scim.spec.filter.SortRequest},
   * the repository is responsible for applying it if the backend supports server-side
   * sorting. The SCIM server layer does NOT apply post-retrieval sorting as a fallback.
   * If the requested sort attribute is not supported, the repository should silently ignore
   * the sort request and return results in the backend's natural order, per
   * <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.3">RFC 7644 §3.4.2.3</a>.</p>
   *
   * @param filter The filter that determines the ScimResources that will be
   *        part of the ResultList.
   * @param requestContext the context object holding additional information about the request.
   * @return A list of the ScimResources that pass the filter criteria,
   *         truncated to match the requested "page" and sorted according
   *         to the provided requirements.
   * @throws ResourceException If one or more ScimResources
   *         cannot be retrieved.
   */
  FilterResponse<T> find(Filter filter, ScimRequestContext requestContext) throws ResourceException;
  
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
