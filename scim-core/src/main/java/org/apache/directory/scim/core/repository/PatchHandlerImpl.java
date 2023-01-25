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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.directory.scim.core.json.ObjectMapperFactory;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.filter.FilterExpressions;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.filter.ValuePathExpression;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unchecked")
@Slf4j
public class PatchHandlerImpl implements PatchHandler {

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final Map<PatchOperation.Type, PatchOperationHandler> patchOperationHandlers = Map.of(
    PatchOperation.Type.ADD, new AddOperationHandler(),
    PatchOperation.Type.REPLACE, new ReplaceOperationHandler(),
    PatchOperation.Type.REMOVE, new RemoveOperationHandler()
  );

  private final ObjectMapper objectMapper;

  private final SchemaRegistry schemaRegistry;

  public PatchHandlerImpl(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
    this.objectMapper = ObjectMapperFactory.createObjectMapper(this.schemaRegistry);
  }

  public <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations) {
    if (original == null) {
      throw new IllegalArgumentException("Original resource is null. Cannot apply patch.");
    }
    if (patchOperations == null) {
      throw new IllegalArgumentException("patchOperations is null. Cannot apply patch.");
    }

    T updatedScimResource = SerializationUtils.clone(original);
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

  private <T extends ScimResource> T apply(T source, final PatchOperation patchOperation) {
    Map<String, Object> sourceAsMap = objectAsMap(source);

    final ValuePathExpression valuePathExpression = valuePathExpression(patchOperation);
    final AttributeReference attributeReference = attributeReference(valuePathExpression);

    PatchOperationHandler patchOperationHandler = patchOperationHandlers.get(patchOperation.getOperation());

    // if the attribute has a URN, assume it's an extension that URN does not match the baseUrn
    if (attributeReference.hasUrn() && !attributeReference.getUrn().equals(source.getBaseUrn())) {
      Schema schema = this.schemaRegistry.getSchema(attributeReference.getUrn());
      Attribute attribute = schema.getAttribute(attributeReference.getAttributeName());
      checkMutability(schema.getAttributeFromPath(attributeReference.getFullAttributeName()));

      patchOperationHandler.applyExtensionValue(source, sourceAsMap, schema, attribute, valuePathExpression, attributeReference.getUrn(), patchOperation.getValue());
    } else {
      Schema schema = this.schemaRegistry.getSchema(source.getBaseUrn());
      Attribute attribute = schema.getAttribute(attributeReference.getAttributeName());
      checkMutability(schema.getAttributeFromPath(attributeReference.getFullAttributeName()));

      patchOperationHandler.applyValue(source, sourceAsMap, schema, attribute, valuePathExpression, patchOperation.getValue());
    }
    return (T) objectMapper.convertValue(sourceAsMap, source.getClass());
  }

  private PatchOperationPath tryGetOperationPath(String key) {
    try {
      return new PatchOperationPath(key);
    } catch (FilterParseException e) {
      log.warn("Parsing path failed with exception.", e);
      throw new IllegalArgumentException("Cannot parse path expression: " + e.getMessage());
    }
  }

  private Map<String, Object> objectAsMap(final Object object) {
    return objectMapper.convertValue(object, MAP_TYPE);
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

  private static void checkMutability(Attribute attribute) throws IllegalArgumentException {
    if (attribute.getMutability().equals(Attribute.Mutability.READ_ONLY) ||
      attribute.getMutability().equals(Attribute.Mutability.IMMUTABLE)) {
      String message = "Can not update a immutable attribute or a read-only attribute '" + attribute.getName() + "'";
      log.error(message);
      throw new IllegalArgumentException(message);
    }
  }

  private interface PatchOperationHandler {

    default <T extends ScimResource> void applyValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      if (attribute.isMultiValued()) {
        if (valuePathExpression.getAttributeExpression() != null) {
          applyMultiValue(source, sourceAsMap, schema, attribute, valuePathExpression, value);
        } else {
          this.applyMultiValue(source, sourceAsMap, schema, attribute, valuePathExpression.getAttributePath(), value);
        }
      } else {
        // no filter expression
        applySingleValue(sourceAsMap, attribute, valuePathExpression.getAttributePath(), value);
      }
    }

    default <T extends ScimResource> void applyExtensionValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, String urn, Object value) {

      Map<String, Object> data = (Map<String, Object>) sourceAsMap.get(urn);

      if (attribute.isMultiValued()) {
        if (valuePathExpression.getAttributeExpression() != null) {
          this.applyMultiValue(source, data, schema, attribute, valuePathExpression, value);
        } else {
          this.applyMultiValue(source, data, schema, attribute, valuePathExpression.getAttributePath(), value);
        }
      } else {
        this.applySingleValue(data, attribute, valuePathExpression.getAttributePath(), value);
      }
    }

    <T extends ScimResource> void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value);
    <T extends ScimResource> void applyMultiValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value);
    <T extends ScimResource> void applyMultiValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value);
  }

  private class AddOperationHandler implements PatchOperationHandler {

    @Override
    public <T extends ScimResource> void applyExtensionValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, String urn, Object value) {

      // add the extension URN
      Collection<String> schemas = (Collection<String>) sourceAsMap.get("schemas");
      schemas.add(urn);

      // if the extension object does not yet exist, create it
      if (!sourceAsMap.containsKey(urn)) {
        sourceAsMap.put(urn, new HashMap<>());
      }

      PatchOperationHandler.super.applyExtensionValue(source, sourceAsMap, schema, attribute, valuePathExpression, urn, value);
    }

    @Override
    public <T extends ScimResource> void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      String attributeName = attribute.getName();
      if (sourceAsMap.get(attributeName) == null) {
        sourceAsMap.put(attributeName, value);
      } else {
        log.debug("Resource '{}' with attribute '{}' already contains value and cannot be patched with an ADD operation", sourceAsMap.get("id"), attribute.getUrn());
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {

      Collection<Object> items = (Collection<Object>) sourceAsMap.get(attributeReference.getAttributeName());
      if (items == null) {
        items = new ArrayList<>();
        sourceAsMap.put(attributeReference.getAttributeName(), items);
      }

      if (value instanceof Collection) {
        items.addAll((Collection<Object>) value);
      } else {
        items.add(value);
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      String attributeName = valuePathExpression.getAttributePath().getAttributeName();

      if (!valuePathExpression.getAttributePath().hasSubAttribute()) {
        throw new IllegalArgumentException("Invalid filter, expecting patch filter with expression to have a sub-attribute.");
      }

      // apply expression filter
      Collection<Object> items = attribute.getAccessor().get(source);
      Predicate<Object> pred = FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), schema);
      String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();

      Collection<Object> updatedCollection = new ArrayList<>(items.size());
      boolean matchFound = false;
      for (Object item: items) {
        Map<String, Object> resourceAsMap = objectAsMap(item);
        if (pred.test(item)) {
          matchFound = true;
          resourceAsMap.put(subAttributeName, value);
        }
        updatedCollection.add(resourceAsMap);
      }

      if (!matchFound) {

        if (!(valuePathExpression.getAttributeExpression() instanceof AttributeComparisonExpression)) {
          throw new IllegalArgumentException("Attribute cannot be added, only comparison expressions are supported when the existing item does not exist.");
        }
        AttributeComparisonExpression comparisonExpression = (AttributeComparisonExpression) valuePathExpression.getAttributeExpression();

        updatedCollection.add(Map.of(
          comparisonExpression.getAttributePath().getSubAttributeName(), comparisonExpression.getCompareValue(),
          subAttributeName, value));
      }
      sourceAsMap.put(attributeName, updatedCollection);
    }
  }

  /*
   * 3.5.2.1.  Add Operation
   *  o If the target location exists, the value is replaced.
   *
   * 3.5.2.3.  Replace Operation
   *  o If the target location is a multi-valued attribute and no filter is specified, the attribute and
   *    all values are replaced.
   */
  private class ReplaceOperationHandler implements PatchOperationHandler {

    @Override
    public <T extends ScimResource> void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      if (attributeReference.hasSubAttribute()) {
        Map<String, Object> parentValue = (Map<String, Object>) sourceAsMap.get(attributeReference.getAttributeName());
        parentValue.put(attributeReference.getSubAttributeName(), value);
      } else {
        sourceAsMap.put(attribute.getName(), value);
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {
      // replace the collection
      sourceAsMap.put(attribute.getName(), value);
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      // apply expression filter
      Collection<Object> items = attribute.getAccessor().get(source);
      Predicate<Object> pred = FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), schema);

      Collection<Object> updatedCollection = items.stream()
        .map(item -> {
          Map<String, Object> resourceAsMap = objectAsMap(item);
          // find items that need to be updated
          if (pred.test(item)) {
            String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();
            if (resourceAsMap.get(subAttributeName) == null) {
              resourceAsMap = (Map<String, Object>) value;
            } else {
              resourceAsMap.put(subAttributeName, value);
            }
            return resourceAsMap;
          } else {
            // filter does not apply
            return item;
          }
        }).collect(toList());
      sourceAsMap.put(attribute.getName(), updatedCollection);
    }
  }

  private class RemoveOperationHandler implements PatchOperationHandler {

    @Override
    public <T extends ScimResource> void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      if (attributeReference.hasSubAttribute()) {
        Map<String, Object> child = (Map<String, Object>) sourceAsMap.get(attributeReference.getAttributeName());
        child.remove(attributeReference.getSubAttributeName());
      } else {
        sourceAsMap.remove(attributeReference.getAttributeName());
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {
      // remove the collection
      sourceAsMap.remove(attribute.getName());
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      AttributeReference attributeReference = valuePathExpression.getAttributePath();
      Collection<Object> items = attribute.getAccessor().get(source);
      Predicate<Object> pred = FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), schema);

      // if there is a sub-attribute in the filter, only that sub-attribute is removed, otherwise the whole item is
      // removed from the collection
      Collection<Object> updatedCollection;
      if(attributeReference.hasSubAttribute()) {
        updatedCollection = items.stream().map(item -> {
            Map<String, Object> resourceAsMap = objectAsMap(item);
            if (pred.test(item)) {
              resourceAsMap.remove(attributeReference.getSubAttributeName());
            }
            return resourceAsMap;
          })
          .collect(toList());
      } else {
        // filter out any items that match
        updatedCollection = items.stream()
          .filter(item -> !pred.test(item))
          .collect(toList());
      }
      sourceAsMap.put(attribute.getName(), updatedCollection);
    }
  }
}
