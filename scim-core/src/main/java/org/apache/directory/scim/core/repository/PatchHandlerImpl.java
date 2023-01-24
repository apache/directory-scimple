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
import org.apache.directory.scim.spec.filter.CompareOperator;
import org.apache.directory.scim.spec.filter.FilterExpression;
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
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Slf4j
public class PatchHandlerImpl implements PatchHandler {

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

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

    Schema baseSchema = this.schemaRegistry.getSchema(source.getBaseUrn());
    if (attributeReference.getUrn() != null) {
      applyScimExtension(attributeReference, sourceAsMap, patchOperation);
    }
    else {
      Attribute attribute = baseSchema.getAttribute(attributeReference.getAttributeName());
      checkMutability(attribute);

      Object attributeValueObject = attribute.getAccessor().get(source);
      FilterExpression filterExpression = valuePathExpression.getAttributeExpression();

      if (filterExpression != null && isMultiValuedComplexAttribute(attribute)) {
        // try applying filter
        boolean attributeObjectExists = attributeValueObject != null && ((Collection<Object>) attributeValueObject).stream().anyMatch(item -> {
          return FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), baseSchema).test(item);
        });

        if (!attributeObjectExists && filterExpression instanceof AttributeComparisonExpression) {
          AttributeComparisonExpression comparisonExpression = (AttributeComparisonExpression) filterExpression;
          if (comparisonExpression.getOperation() != CompareOperator.EQ) {
            throw new IllegalArgumentException("Cannot apply filter expression " + comparisonExpression + ". Only operator EQ is allowed");
          }
          // need to first apply patch from the filter expression and then filter again
          source = apply(source, toPatchOperation(comparisonExpression));
          attributeValueObject = attribute.getAccessor().get(source);
        }
        // apply filter expression
        Collection<Object> updatedCollection = applyWithExpressionFilter(attributeValueObject, valuePathExpression, baseSchema, patchOperation);

        sourceAsMap.put(attribute.getName(), updatedCollection);
      } else {
        // no filter expression
        Attribute subAttribute = Optional.ofNullable(attributeReference.getSubAttributeName())
          .map(attribute::getAttribute).orElse(null);

        if (isMultiValuedComplexAttribute(attribute)){
          applyMultiValuedComplexAttribute(attribute, subAttribute, sourceAsMap, patchOperation);
        }
        else if (isComplexValuedAttribute(attribute)) {
          applyComplexValuedAttribute(attribute, subAttribute, sourceAsMap, patchOperation);
        }
        else {
          applySingularValuedAttribute(attribute, sourceAsMap, patchOperation);
        }
      }
    }

    return (T) objectMapper.convertValue(sourceAsMap, source.getClass());
  }

  private PatchOperation toPatchOperation(AttributeComparisonExpression comparisonExpression) {

    PatchOperation patch = new PatchOperation();
    patch.setOperation(PatchOperation.Type.ADD);
    patch.setPath(tryGetOperationPath(comparisonExpression.getAttributePath().getFullAttributeName()));
    patch.setValue(comparisonExpression.getCompareValue());
    return patch;
  }

  private void applyScimExtension(AttributeReference attributeReference, Map<String, Object> sourceAsMap,
                                  PatchOperation patchOperation) {
    Schema extensionSchema = this.schemaRegistry.getSchema(attributeReference.getUrn());

    final Attribute attribute = extensionSchema.getAttribute(attributeReference.getAttributeName());
    Attribute subAttribute = Optional.ofNullable(attributeReference.getSubAttributeName())
      .map(attribute::getAttribute).orElse(null);

    Map<String, Object> extensionMap;
    if(sourceAsMap.containsKey(attributeReference.getUrn())) {
      extensionMap = (Map<String, Object>) sourceAsMap.get(attributeReference.getUrn());
    } else {
      if(patchOperation.getValue() instanceof Map) {
        extensionMap = (Map<String, Object>) patchOperation.getValue();
      } else {
        extensionMap = new HashMap<>();
      }
    }

    if(isSingularAttribute(attribute)) {
      applySingularValuedAttribute(attribute, extensionMap, patchOperation);
    } else if(isComplexValuedAttribute(attribute)) {
      applyComplexValuedAttribute(attribute, subAttribute, extensionMap,patchOperation);
    } else {
      applyMultiValuedComplexAttribute(attribute, subAttribute, extensionMap,patchOperation);
    }

    // add schemas if necessary
    Object schemaAttributeValue = sourceAsMap.get("schemas");
    if(schemaAttributeValue instanceof List) {
      final List<String> schemaUrns = (List<String>) schemaAttributeValue;
      if (!schemaUrns.contains(attributeReference.getUrn())) {
        schemaUrns.add(attributeReference.getUrn());
        sourceAsMap.replace("schemas", schemaUrns);
      }
    }

    sourceAsMap.put(attributeReference.getUrn(), extensionMap);
  }

  private Collection<Object> applyWithExpressionFilter(Object attributeObject, ValuePathExpression valuePathExpression,
                                                       Schema baseSchema, PatchOperation patchOperation) {
    if (attributeObject == null) {
      // nothing to filter
      return new ArrayList<>();
    }
    // apply expression filter
    Collection<Object> items = (Collection<Object>) attributeObject;
    return items.stream().map(item -> {
      Map<String, Object> resourceAsMap = objectAsMap(item);
      if (FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), baseSchema).test(item)) {
        String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();
        resourceAsMap.put(subAttributeName, patchOperation.getValue());
        return resourceAsMap;
      } else {
        // filter does not apply
        return item;
      }
    }).collect(Collectors.toList());
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

  private boolean isSingularAttribute(final Attribute attribute) {
    return attribute!=null && !attribute.isMultiValued() && !attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  private boolean isComplexValuedAttribute(final Attribute attribute) {
    return attribute!=null && !attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  private boolean isMultiValuedComplexAttribute(final Attribute attribute) {
    return attribute!=null && attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  private void applyMultiValuedComplexAttribute(final Attribute attribute, final Attribute subAttribute,
                                                Map<String, Object> source, final PatchOperation patchOperation) {

    switch (patchOperation.getOperation()) {
      case ADD:
        if(!source.containsKey(attribute.getName())) {
          source.put(attribute.getName(), new ArrayList<Map<String,Object>>());
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) source.get(attribute.getName());
        Map<String, Object> newElement = new HashMap<>();
        applyComplexValuedAttribute(subAttribute, null, newElement, patchOperation);
        items.add(newElement);
        source.put(attribute.getName(), items);
        break;
      case REPLACE:
        if(!source.containsKey(attribute.getName())) {
          source.put(attribute.getName(), new ArrayList<Map<String,Object>>());
          ((List<Map<String,Object>>) source.get(attribute.getName())).add(new HashMap<>());
        }

        List<Map<String,Object>> list = (List<Map<String,Object>>) source.get(attribute.getName());
        if(!list.isEmpty()) {
          if (list.size() > 1) {
            /*
             * 3.5.2.1.  Add Operation
             *  o If the target location exists, the value is replaced.
             *
             * 3.5.2.3.  Replace Operation
             *  o If the target location is a multi-valued attribute and no filter is specified, the attribute and
             *    all values are replaced.
             */
            if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String,Object>>) patchOperation.getValue();
              source.replace(attribute.getName(), list);
              return;
            }
          }

          // replace the sub-attributes
          if(subAttribute != null) {
            Map<String, Object> element = list.get(0);
            applyComplexValuedAttribute(subAttribute, null, element, patchOperation);
          } else {
            if(patchOperation.getValue() instanceof Map) {
              list.remove(0);
              list.add((Map<String, Object>) patchOperation.getValue());
            } else if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String, Object>>) patchOperation.getValue();
            }

            source.replace(attribute.getName(), list);
          }
        }
        break;
      case REMOVE:
        if(subAttribute == null) {
          source.remove(attribute.getName());
        } else {
          if(source.get(attribute.getName()) instanceof List) {
            List<Map<String,Object>> removeList = (List<Map<String,Object>>) source.get(attribute.getName());
            if(!removeList.isEmpty()) {
              if (removeList.size() > 1) {
                throw new IllegalArgumentException("There are " + removeList.size() + " existing entries for " +
                                                     patchOperation.getPath() + ", use a filter to narrow the results.");
              }
              Map<String, Object> map = removeList.get(0);
              map.remove(subAttribute.getName());
            }
          }
        }
        break;
    }
  }

  private void applyComplexValuedAttribute(final Attribute attribute, final Attribute subAttribute,
                                           Map<String, Object> sourceAsMap, final PatchOperation patchOperation) {

    switch (patchOperation.getOperation()) {
      case ADD:
        // same as REPLACE
      case REPLACE:
        if (subAttribute != null) {
          Object complexSource = sourceAsMap.getOrDefault(attribute.getName(), new HashMap<String,Object>());
          Map<String,Object> complexSourceMap = (Map<String,Object>) complexSource;
          applySingularValuedAttribute(subAttribute, complexSourceMap, patchOperation);
          sourceAsMap.put(attribute.getName(), complexSourceMap);
        }
        else {
          applySingularValuedAttribute(attribute, sourceAsMap, patchOperation);
        }
        break;
      case REMOVE:
        if(subAttribute == null) {
          sourceAsMap.remove(attribute.getName());
        } else {
          if(sourceAsMap.get(attribute.getName()) instanceof Map) {
            ((Map<String,Object>)sourceAsMap.get(attribute.getName())).remove(subAttribute.getName());
          }
        }
        break;
    }
  }

  private void applySingularValuedAttribute(final Attribute attribute, Map<String, Object> singularAttributeSource,
                                            final PatchOperation patchOperation) {
    switch (patchOperation.getOperation()) {
      case ADD:
        // same as REPLACE
      case REPLACE:
        singularAttributeSource.put(attribute.getName(), patchOperation.getValue());
        break;
      case REMOVE:
        singularAttributeSource.remove(attribute.getName());
        break;
    }
  }

  private static void checkMutability(Attribute attribute) throws IllegalArgumentException {
    if (attribute.getMutability().equals(Attribute.Mutability.READ_ONLY) ||
      attribute.getMutability().equals(Attribute.Mutability.IMMUTABLE)) {
      String message = "Can not update a immutable attribute or a read-only attribute '" + attribute.getName() + "'";
      log.error(message);
      throw new IllegalArgumentException(message);
    }
  }
}
