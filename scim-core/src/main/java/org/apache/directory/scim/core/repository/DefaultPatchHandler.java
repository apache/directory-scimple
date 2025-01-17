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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.core.json.ObjectMapperFactory;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.exception.MutabilityException;
import org.apache.directory.scim.spec.exception.UnsupportedFilterException;
import org.apache.directory.scim.spec.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.filter.CompareOperator;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Type;

import static java.util.stream.Collectors.toList;

/**
 * The default implementation of a PatchHandler that applies PatchOperations by walking a map equivalent
 * of ScimResource.
 */
@SuppressWarnings("unchecked")
@Slf4j
@ApplicationScoped
public class DefaultPatchHandler implements PatchHandler {

  public static final String PRIMARY = "primary";

  private static final String VALUE_ATTRIBUTE_NAME = "value";

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final Map<PatchOperation.Type, PatchOperationHandler> patchOperationHandlers = Map.of(
    PatchOperation.Type.ADD, new AddOperationHandler(),
    PatchOperation.Type.REPLACE, new ReplaceOperationHandler(),
    PatchOperation.Type.REMOVE, new RemoveOperationHandler()
  );

  private final ObjectMapper objectMapper;

  private final SchemaRegistry schemaRegistry;

  @Inject
  public DefaultPatchHandler(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
    this.objectMapper = ObjectMapperFactory.createObjectMapper(this.schemaRegistry);
  }

  // For CDI
  protected DefaultPatchHandler() {
    this.objectMapper = null;
    this.schemaRegistry = null;
  }

  @Override
  public <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations) {
    if (original == null) {
      throw new UnsupportedFilterException("Original resource is null. Cannot apply patch.");
    }
    if (patchOperations == null) {
      throw new UnsupportedFilterException("patchOperations is null. Cannot apply patch.");
    }

    Map<String, Object> sourceAsMap = objectAsMap(original);
    for (PatchOperation patchOperation : patchOperations) {
      if (patchOperation.getPath() == null) {
        if (!(patchOperation.getValue() instanceof Map)) {
          throw new UnsupportedFilterException("Cannot apply patch. value is required");
        }
        Map<String, Object> properties = (Map<String, Object>) patchOperation.getValue();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
          // convert SCIM patch to RFC-6902 patch
          PatchOperation newPatchOperation = new PatchOperation();
          newPatchOperation.setOperation(patchOperation.getOperation());
          newPatchOperation.setPath(tryGetOperationPath(entry.getKey()));
          newPatchOperation.setValue(entry.getValue());

          apply(original, sourceAsMap, newPatchOperation);
        }
      } else {
        apply(original, sourceAsMap, patchOperation);
      }

    }
    return (T) objectMapper.convertValue(sourceAsMap, original.getClass());
  }

  private <T extends ScimResource> void apply(T source, Map<String, Object> sourceAsMap, final PatchOperation patchOperation) {

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
  }

  private PatchOperationPath tryGetOperationPath(String key) {
    try {
      return PatchOperationPath.fromString(key);
    } catch (FilterParseException e) {
      log.warn("Parsing path failed with exception.", e);
      throw new UnsupportedFilterException("Cannot parse path expression: " + e.getMessage());
    }
  }

  private Map<String, Object> objectAsMap(final Object object) {
    return objectMapper.convertValue(object, MAP_TYPE);
  }

  public static ValuePathExpression valuePathExpression(final PatchOperation operation) {
    return Optional.ofNullable(operation.getPath())
      .map(PatchOperationPath::getValuePathExpression)
      .orElseThrow(() -> new UnsupportedFilterException("Patch operation must have a value path expression"));
  }

  public static AttributeReference attributeReference(final ValuePathExpression expression) {
    return Optional.ofNullable(expression.getAttributePath())
      .orElseThrow(() -> new UnsupportedFilterException("Patch operation must have an expression with a valid attribute path"));
  }

  private static void checkMutability(Attribute attribute) throws MutabilityException {
    if (attribute.getMutability().equals(Attribute.Mutability.READ_ONLY)) {
      String message = "Can not update a read-only attribute '" + attribute.getName() + "'";
      log.error(message);
      throw new MutabilityException(message);
    }
  }

  private static void checkMutability(Attribute attribute, Object currentValue) throws MutabilityException {
    checkMutability(attribute);
    if (attribute.getMutability().equals(Attribute.Mutability.IMMUTABLE) && currentValue != null) {
      String message = "Can not update a immutable attribute that contains a value '" + attribute.getName() + "'";
      log.error(message);
      throw new MutabilityException(message);
    }
  }

  private static void checkPrimary(String subAttributeName, Collection<Map<String, Object>> items, Object value)
  {
    if (subAttributeName.equals(PRIMARY) && value.equals(true)) {
      // reset all other values with primary -> false
      items.forEach(item -> {
        if (item.containsKey(PRIMARY)) {
          item.put(PRIMARY, false);
        }
      });
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

    void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value);
    <T extends ScimResource> void applyMultiValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value);
    <T extends ScimResource> void applyMultiValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value);
  }

  private static class AddOperationHandler implements PatchOperationHandler {

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
    public void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      String attributeName = attribute.getName();
      checkMutability(attribute, sourceAsMap.get(attributeName));
      if (attributeReference.hasSubAttribute()) {
        Map<String, Object> parentValue = (Map<String, Object>) sourceAsMap.getOrDefault(attributeName, new HashMap<String, Object>());
        String subAttributeName = attributeReference.getSubAttributeName();
        checkMutability(attribute.getAttribute(subAttributeName), parentValue.get(subAttributeName));
        parentValue.put(subAttributeName, value);
        sourceAsMap.put(attributeName, parentValue);
      } else {
        sourceAsMap.put(attributeName, value);
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {

      Collection<Object> items = (Collection<Object>) sourceAsMap.get(attributeReference.getAttributeName());
      checkMutability(attribute, items);
      if (items == null) {
        items = new ArrayList<>();
      }

      if (value instanceof Collection) {
        items.addAll((Collection<Object>) value);
      } else {
        items.add(value);
      }
      sourceAsMap.put(attributeReference.getAttributeName(), items);
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      String attributeName = valuePathExpression.getAttributePath().getAttributeName();

      if (!valuePathExpression.getAttributePath().hasSubAttribute()) {
        throw new UnsupportedFilterException("Invalid filter, expecting patch filter with expression to have a sub-attribute.");
      }

      // apply expression filter
      Collection<Map<String, Object>> items = (Collection<Map<String, Object>>) sourceAsMap.getOrDefault(attributeName, new ArrayList<Map<String, Object>>());
      Predicate<Object> pred = FilterExpressions.inMemoryMap(valuePathExpression.getAttributeExpression(), schema);
      String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();

      boolean matchFound = false;
      for (Map<String, Object> item : items) {
        if (pred.test(item)) {
          matchFound = true;
          checkMutability(attribute, item.get(subAttributeName));
          checkPrimary(subAttributeName, items, value);
          item.put(subAttributeName, value);
        }
      }

      // if the value was not added to an existing item, create a new value and add the patch value and the expression
      // values for example in the expression `emails[type eq "work"].value` with a patch value of `foo@example.com`
      // the map will contain `type: "work", value: "foo@example.com"`
      if (!matchFound) {
        if (!(valuePathExpression.getAttributeExpression() instanceof AttributeComparisonExpression)) {
          throw new UnsupportedFilterException("Attribute cannot be added, only comparison expressions are supported when the existing item does not exist.");
        }
        AttributeComparisonExpression comparisonExpression = (AttributeComparisonExpression) valuePathExpression.getAttributeExpression();
        checkPrimary(subAttributeName, items, value);

        // Add a new mutable map
        items.add(new HashMap<>(Map.of(
          comparisonExpression.getAttributePath().getSubAttributeName(), comparisonExpression.getCompareValue(),
          subAttributeName, value)));
      }

      sourceAsMap.put(attributeName, items);
    }
  }

  private static class ReplaceOperationHandler implements PatchOperationHandler {

    @Override
    public void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      if (attributeReference.hasSubAttribute()) {
        Map<String, Object> parentValue = (Map<String, Object>) sourceAsMap.get(attributeReference.getAttributeName());
        String subAttributeName = attributeReference.getSubAttributeName();
        checkMutability(attribute.getAttribute(subAttributeName), parentValue.get(subAttributeName));
        parentValue.put(subAttributeName, value);
      } else {
        Object currentValue = sourceAsMap.get(attribute.getName());
        checkMutability(attribute, currentValue);
        // PATCH on complex attributes should only replace the values given, leaving any not provided in the request untouched
        if (attribute.getType() == Type.COMPLEX) {
          Map<String, Object> newValue = ((Map<String, Object>) currentValue);
          newValue.putAll((Map<String, Object>) value);
          sourceAsMap.put(attribute.getName(), newValue);
        } else {
          sourceAsMap.put(attribute.getName(), value);
        }
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {
      checkMutability(attribute, sourceAsMap.get(attribute.getName()));
      // replace the collection
      sourceAsMap.put(attribute.getName(), value);
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      String attributeName = attribute.getName();
      checkMutability(attribute, sourceAsMap.get(attributeName));

      // apply expression filter
      Collection<Map<String, Object>> items = (Collection<Map<String, Object>>) sourceAsMap.get(attributeName);
      Predicate<Object> pred = FilterExpressions.inMemoryMap(valuePathExpression.getAttributeExpression(), schema);

      Collection<Object> updatedCollection = items.stream()
        .map(item -> {
          // find items that need to be updated
          if (pred.test(item)) {
            String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();
            // if there is a sub-attribute set it, otherwise replace the whole item
            if (item.containsKey(subAttributeName)) {
              checkMutability(attribute.getAttribute(subAttributeName), item.get(subAttributeName));
              checkPrimary(subAttributeName, items, value);
              item.put(subAttributeName, value);
            } else {
              item = (Map<String, Object>) value;
            }
          }
          return item;
        }).collect(toList());
      sourceAsMap.put(attribute.getName(), updatedCollection);
    }
  }

  private static class RemoveOperationHandler implements PatchOperationHandler {

    public <T extends ScimResource> void applyValue(final T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {
      // detect Azure off-spec request
      if (isAzureRemoveQuirk(attribute, valuePathExpression, value)) {
        Collection<?> valuesToRemove = (Collection<?>) value;
        AttributeReference valueAttributeRef = new AttributeReference(attribute.getSchemaUrn(), attribute.getName(), VALUE_ATTRIBUTE_NAME);

        // map the Azure formatted examples in to a _normal_ scim filter expression
        azureQuirkValuesToRemove(valuesToRemove, attribute).forEach(itemToRemove -> {
          ValuePathExpression adjustedValuePathExpression = new ValuePathExpression(valuePathExpression.getAttributePath(), new AttributeComparisonExpression(valueAttributeRef, CompareOperator.EQ, itemToRemove));
          applyMultiValue(source, sourceAsMap, schema, attribute, adjustedValuePathExpression, itemToRemove);
        });

      } else {
        // call super (default method of interface)
        PatchOperationHandler.super.applyValue(source, sourceAsMap, schema, attribute, valuePathExpression, value);
      }
    }

    @Override
    public void applySingleValue(Map<String, Object> sourceAsMap, Attribute attribute, AttributeReference attributeReference, Object value) {
      if (attributeReference.hasSubAttribute()) {
        Map<String, Object> child = (Map<String, Object>) sourceAsMap.get(attributeReference.getAttributeName());
        String subAttributeName = attributeReference.getSubAttributeName();
        checkMutability(attribute.getAttribute(subAttributeName), child.get(subAttributeName));
        child.remove(attributeReference.getSubAttributeName());
      } else {
        checkMutability(attribute, sourceAsMap.get(attributeReference.getAttributeName()));
        sourceAsMap.remove(attributeReference.getAttributeName());
      }
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, AttributeReference attributeReference, Object value) {
      checkMutability(attribute, sourceAsMap.get(attribute.getName()));
      // remove the collection
      sourceAsMap.remove(attribute.getName());
    }

    @Override
    public <T extends ScimResource> void applyMultiValue(T source, Map<String, Object> sourceAsMap, Schema schema, Attribute attribute, ValuePathExpression valuePathExpression, Object value) {

      AttributeReference attributeReference = valuePathExpression.getAttributePath();
      Collection<Map<String, Object>> items = (Collection<Map<String, Object>>) sourceAsMap.get(attributeReference.getAttributeName());
      Predicate<Object> pred = FilterExpressions.inMemoryMap(valuePathExpression.getAttributeExpression(), schema);

      // if there is a sub-attribute in the filter, only that sub-attribute is removed, otherwise the whole item is
      // removed from the collection
      if(attributeReference.hasSubAttribute()) {
        items.forEach(item -> {
          if (pred.test(item)) {
            String subAttributeName = attributeReference.getSubAttributeName();
            checkMutability(attribute.getAttribute(subAttributeName), item.get(subAttributeName));
            item.remove(subAttributeName);
          }
        });
      } else {
        // remove any items that match
        for (Iterator<Map<String, Object>> iter = items.iterator(); iter.hasNext();) {
          Map<String, Object> item = iter.next();
          if (pred.test(item)) {
            checkMutability(attribute, item);
            iter.remove();
          }
        }
      }
    }

    /**
     *  Detects Azure Quirk mode.
     *  Azure uses an out of spec patch operation that does not use an express,
     *  but instead uses a remove with a value.  Detect this and convert it to an expression
     *  <pre><code>
     *   {
     *     "op":"remove",
     *     "path":"members",
     *     "value":[{
     *         "value":"<id>"
     *     }]
     *   }
     *   </code></pre>
     * @param valuePathExpression The valuePathExpression to check if it has a null attribute
     * @return true, if Azure patch REMOVE detected.
     */
    private static boolean isAzureRemoveQuirk(Attribute attribute, ValuePathExpression valuePathExpression, Object value) {
      return attribute.isMultiValued()
        && attribute.getAttribute(VALUE_ATTRIBUTE_NAME) != null
        && valuePathExpression.getAttributeExpression() == null
        && value instanceof Collection;
    }

    private static List<String> azureQuirkValuesToRemove(Collection<?> listOfMaps, Attribute attribute) {
      return listOfMaps.stream()
        .map(item -> {
          if (!(item instanceof Map)) {
            throw new IllegalArgumentException("Azure Remove Patch request quirk detected, but 'value' is not a list of maps");
          }
          return (Map<?,?>) item;
        })
        .map(item -> {
          Attribute valueAttribute = attribute.getAttribute(VALUE_ATTRIBUTE_NAME);
          Object itemValue = item.get(valueAttribute.getName());
          if (!(itemValue instanceof String)) {
            throw new IllegalArgumentException("Azure Remove Patch request quirk detected, but item 'value' is not a string");
          }

          return (String) itemValue;
        })
        .collect(toList());
    }
  }
}
