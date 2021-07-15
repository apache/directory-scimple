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
package org.apache.directory.scim.test.helpers.builder;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.test.helpers.ScimTestHelper;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class PatchOperationBuilder {
  private static final Logger log = getLogger(PatchOperationBuilder.class);

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(PatchOperation data) {
    return new Builder(data);
  }

  public static final class Builder {

    private Type operation;
    private PatchOperationPath path = null;
    private Object value = null;

    private Builder() {
    }

    private Builder(PatchOperation initialData) {
      this.operation = initialData.getOperation();
      this.path = initialData.getPath();
      this.value = initialData.getValue();
    }

    public Builder operation(Type operation) {
      this.operation = operation;
      return this;
    }

    public Builder path(String path)
      throws FilterParseException {
      return this.path(new PatchOperationPath(path));
    }

    public Builder path(PatchOperationPath path) {
      this.path = path;
      return this;
    }

    public Builder value(Object value) {
      this.value = value;
      return this;
    }

    public PatchOperation build() {
      final PatchOperation patchOperation = new PatchOperation();

      patchOperation.setPath(path);
      patchOperation.setOperation(operation);
      patchOperation.setValue(value);

      return patchOperation;
    }
  }

  public static void main(String[] args)
    throws Exception {
    Map<String, Object> po1Value = ImmutableMap.of("nickName", "Babas", "userType", "CEO");
    PatchOperation po1 = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .value(po1Value)
      .build();
    Map<String, Object> po2Value = ImmutableMap.of("displayName", "patched Brava");
    PatchOperation po2 = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .value(po2Value)
      .build();

    Map<String, Object> po3Value = ImmutableMap.of("familyName", "re-patched Jensen",
      "givenName", "re-patched Barbara",
      "middleName", "re-patched Jane");
    PatchOperation po3 = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("name")
      .value(po3Value)
      .build();
    List<Map<String, Object>> po4Value = ImmutableList.of(ImmutableMap.of("value", "re-patch 555 123 4567",
      "type", "other"),
      ImmutableMap.of("value", "re-patch 666 000 1234",
        "type", "work"));
    PatchOperation po4 = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("phoneNumbers")
      .value(po4Value)
      .build();

    List<Map<String, Object>> po5Value = ImmutableList.of(ImmutableMap.of("type", "work",
      "postalCode", "16801-0385"),
      ImmutableMap.of("type", "home",
        "postalCode", "16801-5830"));
    PatchOperation po5 = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("addresses")
      .value(po5Value)
      .build();

    List<PatchOperation> operations = ImmutableList.of(po5);

    ObjectMapper objectMapper = new ObjectMapperFactory(ScimTestHelper.createRegistry()).createObjectMapper();

    log.info(objectMapper.writeValueAsString(operations));
  }
}
