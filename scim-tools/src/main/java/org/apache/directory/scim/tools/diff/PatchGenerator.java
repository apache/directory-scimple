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

package org.apache.directory.scim.tools.diff;

import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.filter.CompareOperator;
import org.apache.directory.scim.spec.filter.ValuePathExpression;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * Generates a diff (i.e. a list of {@link PatchOperation}s) between two SCIM resources.
 * This class could be used by SCIM clients when they only want to send PATCH requests over the wire (and communicate
 * only the delta between the original and the modified resource).
 * This class can also be used when testing SCIM servers as an easy way to generate a list of {@link PatchOperation}s.
 */
public class PatchGenerator {

  private static final String TYPE = "type";
  private static final String VALUE = "value";

  private final SchemaRegistry schemaRegistry;

  public PatchGenerator(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  /**
   * Returns a list of {@link PatchOperation}s that contain the differences between two {@link ScimResource}s.
   * @param left an unmodified scim resource, as represented in a datastore.
   * @param right the modified version of the original scim resource.
   * @return a list of {@link PatchOperation}s that contain the differences between two {@link ScimResource}s.
   * @param <T> The type of the scim resource.
   */
  public <T extends ScimResource> List<PatchOperation> diff(T left, T right) {
    try {
      return new ArrayList<>(createPatchOperations(left, right));
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Error creating the patch list", e);
    }
  }

  private <T extends ScimResource> Set<PatchOperation> createPatchOperations(T left, T right) {

    Set<PatchOperation> patchOperations = new HashSet<>();

    // make sure types are the same, we could support subtypes in the future, but for now, keep it simple
    if (left.getClass() != right.getClass() || !left.getBaseUrn().equals(right.getBaseUrn())) {
      throw new IllegalArgumentException("Objects must be of the same type");
    }

    // base schema attributes
    String baseUrn = left.getBaseUrn();
    Schema baseSchema = schemaRegistry.getSchema(baseUrn);

    // for each attribute compare them
    baseSchema.getAttributes().forEach(attribute -> {
      patchOperations.addAll(processAttribute(null, emptyList(), attribute, left, right));
    });

    // Extensions attributes
    Set<String> schemas = new HashSet<>();
    schemas.addAll(left.getExtensions().keySet());
    schemas.addAll(right.getExtensions().keySet());

    schemas.forEach(urn -> {
      Schema extSchema = schemaRegistry.getSchema(urn);
      ScimExtension leftExt = left.getExtensions().get(urn);
      ScimExtension rightExt = right.getExtensions().get(urn);

      if (leftExt == null) {
        patchOperations.add(new PatchOperation()
          .setPath(new PatchOperationPath(new ValuePathExpression(new AttributeReference(attributePath(extSchema, emptyList(), null)))))
          .setOperation(PatchOperation.Type.ADD)
          .setValue(rightExt));
      } else if (rightExt == null) {
        patchOperations.add(new PatchOperation()
          .setPath(new PatchOperationPath(new ValuePathExpression(new AttributeReference(attributePath(extSchema, emptyList(), null)))))
          .setOperation(PatchOperation.Type.REMOVE));
      } else {
        // for each attribute compare them
        extSchema.getAttributes().forEach(attribute -> {
          patchOperations.addAll(processAttribute(extSchema, emptyList(), attribute, leftExt, rightExt));
        });
      }
    });
    return patchOperations;
  }

  private Set<PatchOperation> processAttribute(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute, Object left, Object right) {

    Set<PatchOperation> patchOperations = new HashSet<>();

    Object leftTemp = left != null ? attribute.getAccessor().get(left) : null;
    Object rightTemp = right != null ? attribute.getAccessor().get(right) : null;

    if (attribute.getType() == Schema.Attribute.Type.COMPLEX) {

      if (attribute.isMultiValued()) {
        patchOperations.addAll(processMultiValuedComplexAttribute(prefixSchema, parentAttributes, attribute, leftTemp, rightTemp));
      } else {
        // Check if the whole object has been removed
        if (leftTemp != null && rightTemp == null) {
          patchOperations.add(new PatchOperation()
            .setOperation(PatchOperation.Type.REMOVE)
            .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute))));
        } else {
          // loop through sub attributes
          attribute.getAttributes().forEach(subAttribute -> {
            List<Schema.Attribute> subAttributeParents = new ArrayList<>(parentAttributes);
            subAttributeParents.add(attribute);
            patchOperations.addAll(processAttribute(prefixSchema, subAttributeParents, subAttribute, leftTemp, rightTemp));
          });
        }
      }
    } else { // primitive types (and collections of primitive types)
      patchOperations.addAll(processPrimitiveAttribute(prefixSchema, parentAttributes, attribute, leftTemp, rightTemp));
    }
    return patchOperations;
  }

  private static Set<PatchOperation> processPrimitiveAttribute(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute, Object left, Object right) {

    Set<PatchOperation> patchOperations = new HashSet<>();

    if (isEmpty(left) && isNotEmpty(right)) {
      // ADD
      patchOperations.add(new PatchOperation()
        .setOperation(PatchOperation.Type.ADD)
        .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute)))
        .setValue(right));

    } else if (isNotEmpty(left) && isEmpty(right)) {
      // REMOVE attribute
      patchOperations.add(new PatchOperation()
        .setOperation(PatchOperation.Type.REMOVE)
        .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute))));
    } else if (isNotEquivalent(left, right)) {
      // REPLACE
      patchOperations.add(new PatchOperation()
        .setOperation(PatchOperation.Type.REPLACE)
        .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute)))
        .setValue(right));
    }

    return patchOperations;
  }

  @SuppressWarnings("unchecked")
  private static Set<PatchOperation> processMultiValuedComplexAttribute(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute, Object left, Object right) {

    Set<PatchOperation> patchOperations = new HashSet<>();

    if (!isCollectionOrNull(left) || !isCollectionOrNull(right)) {
      throw new IllegalArgumentException("The values of attribute '" + attributePath(prefixSchema, parentAttributes, attribute) + "' must be a Collection.");
    }

    Collection<Object> leftList = (Collection<Object>) left;
    Collection<Object> rightList = (Collection<Object>) right;

    if (isNotEmpty(leftList)) {

      // Look for items to REMOVE
      if (isNotEmpty(rightList)) {
        List<Object> removed = new ArrayList<>(leftList);
        removed.removeAll(rightList);
        removed.forEach(item -> {
          patchOperations.add(new PatchOperation()
            .setOperation(PatchOperation.Type.REMOVE)
            .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute, item))));
        });
      } else {
        // Empty List, remove attribute
        patchOperations.add(new PatchOperation()
          .setOperation(PatchOperation.Type.REMOVE)
          .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute))));
      }
    }

    if (isNotEmpty(rightList)) {
      List<Object> added = new ArrayList<>(rightList);

      // remove values already present
      if (isNotEmpty(leftList)) {
        added.removeAll(leftList);
      }

      added.forEach(item -> {
        patchOperations.add(new PatchOperation()
          .setOperation(PatchOperation.Type.ADD)
          .setPath(new PatchOperationPath(attributeExpression(prefixSchema, parentAttributes, attribute)))
          .setValue(item));
      });
    }

    return patchOperations;
  }

  private static String attributePath(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute) {

    // join any parents with attribute
    String attributePath = Stream.concat(parentAttributes.stream(), singleStream(attribute))
      .map(Schema.Attribute::getName)
      .collect(Collectors.joining("."));

    // if a prefix is set, prefix with the URN
    if (prefixSchema != null) {
      String urn = prefixSchema.getId();
      if (attributePath.isEmpty()) {
        attributePath = urn;
      } else {
        attributePath = urn + ":" + attributePath;
      }
    }

    return attributePath;
  }

  private static ValuePathExpression attributeExpression(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute) {
    return new ValuePathExpression(new AttributeReference(attributePath(prefixSchema, parentAttributes, attribute)));
  }

  private static ValuePathExpression attributeExpression(Schema prefixSchema, List<Schema.Attribute> parentAttributes, Schema.Attribute attribute, Object value) {

    Schema.Attribute typeAttribute = attribute.getAttribute(TYPE);
    Schema.Attribute valueAttribute = attribute.getAttribute(VALUE);
    Optional<Schema.Attribute> refAttribute = attribute.getSubAttributes().stream()
      .filter(attr -> attr.getType() == Schema.Attribute.Type.REFERENCE)
      .findFirst();

    // Special handling if there is this object has reference attribute and a `value`  attribute
    // if right is a ref, the expression will be: `<attribute>[value EQ <right.value>]`
    if (refAttribute.isPresent() && valueAttribute != null) {
      Object right = valueAttribute.getAccessor().get(value);

      if (right != null) {
        if (!(right instanceof String)) {
          throw new IllegalArgumentException("PatchGenerator does not support 'value' attributes that are not a String.");
        }

        AttributeReference attributeReference = new AttributeReference(attributePath(prefixSchema, parentAttributes, attribute));
        AttributeReference expressionAttributeRef = new AttributeReference(attributePath(prefixSchema, parentAttributes, valueAttribute));
        return new ValuePathExpression(attributeReference, new AttributeComparisonExpression(expressionAttributeRef, CompareOperator.EQ, right));
      }
    }

    // Next check for objects that have a `type` attribute: `<attribute>[type EQ <right.type>]`
    if (typeAttribute != null) {
      // force a string value
      Object type = typeAttribute.getAccessor().get(value);
      if (type != null) {
        type = type.toString();
      }

      AttributeReference attributeReference = new AttributeReference(attributePath(prefixSchema, parentAttributes, attribute));
      AttributeReference expressionAttributeRef = new AttributeReference(attributePath(prefixSchema, parentAttributes, typeAttribute));
      return new ValuePathExpression(attributeReference, new AttributeComparisonExpression(expressionAttributeRef, CompareOperator.EQ, type));
    }

    // fall back to an attribute with no value
    AttributeReference attributeReference = new AttributeReference(attributePath(prefixSchema, parentAttributes, attribute));
    return new ValuePathExpression(attributeReference);
  }

  private static boolean isEmpty(Object obj) {
    return obj == null || (obj instanceof Collection && ((Collection<?>) obj).isEmpty());
  }

  private static boolean isNotEmpty(Object obj) {
    return !isEmpty(obj);
  }

  private static boolean isEquivalent(Object left, Object right) {
    return Objects.equals(left, right) || (isEmpty(left) && isEmpty(right));
  }

  private static boolean isNotEquivalent(Object left, Object right) {
    return !isEquivalent(left, right);
  }

  private static boolean isCollectionOrNull(Object value) {
    return value == null || value instanceof Collection;
  }

  private static <T> Stream<T> singleStream(T item) {
    return item != null
      ? Stream.of(item)
      : Stream.of();
  }
}
