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

import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schemas;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

public class PatchHandlerTest {

  SchemaRegistry mockSchemaRegistry = mock(SchemaRegistry.class);

  @Test
  public void applyReplaceUserName() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(PatchOperation.Type.REPLACE);
    op.setPath(new PatchOperationPath("userName"));
    op.setValue("testUser_updated");
    ScimUser updatedUser = performPatch(op);
    assertThat(updatedUser.getUserName()).isEqualTo("testUser_updated");
  }

  @Test
  public void applyReplaceUserNameWithMappedValue() {
    PatchOperation op = new PatchOperation();
    op.setOperation(PatchOperation.Type.REPLACE);
    op.setValue(Map.ofEntries(entry("userName", "testUser_updated")));
    ScimUser updatedUser = performPatch(op);
    assertThat(updatedUser.getUserName()).isEqualTo("testUser_updated");
  }

  private ScimUser performPatch(PatchOperation op) {
    when(mockSchemaRegistry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(Schemas.schemaFor(ScimUser.class));
    PatchHandlerImpl patchHandler = new PatchHandlerImpl(mockSchemaRegistry);
    ScimUser user = new ScimUser();
    user.setUserName("testUser");
    return patchHandler.apply(user, List.of(op));
  }
}
