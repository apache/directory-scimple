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
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchOperationAssert extends AbstractAssert<PatchOperationAssert, PatchOperation> {

  public PatchOperationAssert(PatchOperation patchOperation) {
    super(patchOperation, PatchOperationAssert.class);
  }

  public PatchOperationAssert matches(PatchOperation.Type type, String path, Object value) {
    return isNotNull()
      .isType(type)
      .isPath(path)
      .isValue(value)
      .describedAs("wtf");
  }

  public PatchOperationAssert matches(PatchOperation.Type type, PatchOperationPath path, Object value) {
    return isNotNull()
      .isType(type)
      .isPath(path)
      .isValue(value);
  }

  public PatchOperationAssert isType(PatchOperation.Type expected) {
    isNotNull();
    assertThat(actual.getOperation())
      .describedAs("Operation type")
      .isEqualTo(expected);
    return this;
  }

  public PatchOperationAssert isPath(PatchOperationPath expected) {
    isNotNull();
    assertThat(actual.getPath())
      .describedAs("Operation path")
      .isEqualTo(expected);
    return this;
  }

  public PatchOperationAssert isPath(String expected) {
    isNotNull();
    assertThat(actual.getPath().toString())
      .describedAs("Operation path")
      .isEqualTo(expected);
    return this;
  }

  public PatchOperationAssert isValue(Object expected) {
    isNotNull();
    assertThat(actual.getValue())
      .describedAs("Operation value")
      .isEqualTo(expected);
    return this;
  }

  static class PatchOperationCondition extends Condition<PatchOperation> {

    private final PatchOperation.Type type;
    private final String path;
    private final Object value;

    public PatchOperationCondition(PatchOperation.Type type, String path, Object value) {
      this.type = type;
      this.path = path;
      this.value = value;
      this.describedAs("PatchOperation[operation=%s, path='%s', value=%s]", type, path, value);
    }

    @Override
    public boolean matches(PatchOperation patchOperation){
      return Objects.equals(type, patchOperation.getOperation()) &&
        Objects.equals(path, Objects.toString(patchOperation.getPath().toString())) &&
        Objects.equals(value, patchOperation.getValue());
    }

    static PatchOperationCondition op(PatchOperation.Type type, String path, Object value) {
      return new PatchOperationCondition(type, path, value);
    }

    static PatchOperationCondition op(PatchOperation.Type type, String path) {
      return new PatchOperationCondition(type, path, null);
    }
  }
}
