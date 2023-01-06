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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.*;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
@Slf4j
public class PatchHandlerImpl implements PatchHandler {
  private final SchemaRegistry schemaRegistry;

  public PatchHandlerImpl(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations) {
    if (original == null) {
      throw new IllegalArgumentException("Original resource is null. Cannot apply patch.");
    }
    if (patchOperations == null) {
      throw new IllegalArgumentException("patchOperations is null. Cannot apply patch.");
    }

    T updatedScimResource;
    updatedScimResource = SerializationUtils.clone(original);
    for (PatchOperation patchOperation : patchOperations) {
      if (patchOperation.getPath() == null) {
        if (!(patchOperation.getValue() instanceof Map)) {
          throw new IllegalArgumentException("Cannot apply patch. value is required");
        }
        Map<String, Object> properties = (Map<String, Object>) patchOperation.getValue();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
          // convert SCIM patch to RFC-6902 patch
          PatchOperation newPatchOperation = new PatchOperation();
          newPatchOperation.setOperation(patchOperation.getOperation());
          newPatchOperation.setPath(tryGetOperationPath(entry.getKey()));
          newPatchOperation.setValue(entry.getValue());

          updatedScimResource = apply(updatedScimResource, newPatchOperation);
        }
      } else {
        updatedScimResource = apply(updatedScimResource, patchOperation);
      }

    }
    return updatedScimResource;
  }

  private <T extends ScimResource> T apply(final T source, final PatchOperation patchOperation) {

    final ValuePathExpression valuePathExpression = valuePathExpression(patchOperation);
    final AttributeReference attributeReference = attributeReference(valuePathExpression);

    Schema baseSchema = this.schemaRegistry.getSchema(source.getBaseUrn());
    Attribute attribute = baseSchema.getAttribute(attributeReference.getAttributeName());

    Object attributeObject = attribute.getAccessor().get(source);
    if (attributeObject == null && patchOperation.getOperation().equals(PatchOperation.Type.REPLACE)) {
      throw new IllegalArgumentException("Cannot apply patch replace on missing property: " + attribute.getName());
    }

    if (valuePathExpression.getAttributeExpression() != null && attributeObject instanceof Collection<?>) {
      // apply expression filter
      Collection<Object> items = (Collection<Object>) attributeObject;
      items.forEach(item -> {
        Predicate<Object> pred = FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), baseSchema);
        if (pred.test(item)) {
          String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();
          Schema.AttributeAccessor subAttributeAccessor = attribute.getAttribute(subAttributeName).getAccessor();
          subAttributeAccessor.set(item, patchOperation.getValue());
        }
      });
    } else {
      // no filter expression, just set the value
      attribute.getAccessor().set(source, patchOperation.getValue());
    }
    return source;
  }

  private PatchOperationPath tryGetOperationPath(String key) {
    try {
      return new PatchOperationPath(key);
    } catch (FilterParseException e) {
      log.warn("Parsing path failed with exception.", e);
      throw new IllegalArgumentException("Cannot parse path expression: " + e.getMessage());
    }
  }

  public static ValuePathExpression valuePathExpression(final PatchOperation operation) {
    return Optional.ofNullable(operation.getPath())
      .map(PatchOperationPath::getValuePathExpression)
      .orElseThrow(() -> new IllegalArgumentException("Patch operation must have a value path expression"));
  }

  public static AttributeReference attributeReference(final ValuePathExpression expression) {
    return Optional.ofNullable(expression.getAttributePath())
      .orElseThrow(() -> new IllegalArgumentException("Patch operation must have an expression with a valid attribute path"));
  }
}
