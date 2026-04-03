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

import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.util.List;

/**
 * Optional base class for {@link Repository} implementations that provides default
 * implementations for common boilerplate methods.
 *
 * <p>Subclasses must implement {@link #create}, {@link #update}, {@link #get},
 * {@link #find}, and {@link #delete}. The following are provided automatically:</p>
 * <ul>
 *   <li>{@link #getResourceClass()} — returns the class passed to the constructor</li>
 *   <li>{@link #getExtensionList()} — returns the extension classes passed to the constructor</li>
 *   <li>{@link #patch(String, List, ScimRequestContext)} — fetches the current resource
 *       via {@link #get}, applies patch operations via {@link PatchHandler}, and persists
 *       via {@link #update}</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * &#64;Named
 * &#64;ApplicationScoped
 * public class MyUserRepository extends BaseRepository&lt;ScimUser&gt; {
 *   &#64;Inject
 *   public MyUserRepository(PatchHandler patchHandler) {
 *     super(ScimUser.class, patchHandler, MyExtension.class);
 *   }
 *   // implement create, update, get, find, delete
 * }
 * </pre>
 *
 * @param <T> the SCIM resource type this repository manages
 */
public abstract class BaseRepository<T extends ScimResource> implements Repository<T> {

  private final Class<T> resourceClass;
  private final PatchHandler patchHandler;
  private final List<Class<? extends ScimExtension>> extensionList;

  /**
   * Creates a new base repository.
   *
   * @param resourceClass the SCIM resource class this repository manages
   * @param patchHandler  the handler used to apply SCIM PATCH operations
   * @param extensions    SCIM extension classes supported by this repository (may be empty)
   */
  @SafeVarargs
  protected BaseRepository(Class<T> resourceClass, PatchHandler patchHandler,
      Class<? extends ScimExtension>... extensions) {
    this.resourceClass = resourceClass;
    this.patchHandler = patchHandler;
    this.extensionList = extensions != null ? List.of(extensions) : List.of();
  }

  /**
   * No-arg constructor for CDI proxy creation. Subclasses using CDI must also
   * provide a no-arg constructor that calls {@code super()}.
   */
  protected BaseRepository() {
    this.resourceClass = null;
    this.patchHandler = null;
    this.extensionList = List.of();
  }

  @Override
  public Class<T> getResourceClass() {
    return resourceClass;
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return extensionList;
  }

  /**
   * Default PATCH implementation: fetches the current resource, applies the patch
   * operations, and persists the result via {@link #update}.
   *
   * <p>Subclasses may override this method if their backend supports more efficient
   * partial-update semantics.</p>
   *
   * {@inheritDoc}
   */
  @Override
  public T patch(String id, List<PatchOperation> patchOperations,
      ScimRequestContext requestContext) throws ResourceException {
    if (patchHandler == null) {
      throw new IllegalStateException("No PatchHandler configured; patch() is not available on this instance");
    }
    T current = get(id, requestContext);
    if (current == null) {
      throw new ResourceNotFoundException(id);
    }
    T patched = patchHandler.apply(current, patchOperations);
    return update(id, patched, requestContext);
  }
}
