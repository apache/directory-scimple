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

package org.apache.directory.scim.test.assertj;

import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.assertj.core.api.Condition;

public final class ScimpleAssertions {

  public static IterablePatchOperationAssert scimAssertThat(Iterable<PatchOperation> actual) {
    return new IterablePatchOperationAssert(actual);
  }

  public static PatchOperationAssert scimAssertThat(PatchOperation actual) {
    return new PatchOperationAssert(actual);
  }

  public static ScimGroupAssert scimAssertThat(ScimGroup actual) {
    return new ScimGroupAssert(actual);
  }

  public static Condition<PatchOperation> patchOpMatching(PatchOperation.Type type, String path) {
    return patchOpMatching(type, path, null);
  }

  public static Condition<PatchOperation> patchOpMatching(PatchOperation.Type type, String path, Object value) {
    return new PatchOperationAssert.PatchOperationCondition(type, path, value);
  }

}
