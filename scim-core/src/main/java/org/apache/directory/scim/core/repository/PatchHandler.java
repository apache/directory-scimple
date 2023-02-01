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

import org.apache.directory.scim.spec.exception.UnsupportedFilterException;
import org.apache.directory.scim.spec.exception.MutabilityException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.util.List;

/**
 *  A PatchHandler applies PatchOperations to a ScimResource. PatchOperations are a payload in a PATCH REST request.
 */
public interface PatchHandler {

  /**
   * Applies patch operations to a ScimResource.
   *
   * @param original The source ScimResource to apply patches to.
   * @param patchOperations The list of patch operations to apply.
   * @return An updated ScimResource with all patches applied
   * @param <T> The type of ScimResource.
   * @throws UnsupportedFilterException if the patch operations are invalid.
   * @throws MutabilityException if an attribute is not allowed to be updated.
   */
  <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations);
}
