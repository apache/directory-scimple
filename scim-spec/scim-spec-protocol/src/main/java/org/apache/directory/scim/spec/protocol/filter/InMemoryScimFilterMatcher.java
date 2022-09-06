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

package org.apache.directory.scim.spec.protocol.filter;

import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

final class InMemoryScimFilterMatcher {

  private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

  private InMemoryScimFilterMatcher() {}

  /**
   * Converts a FilterExpression to a Predicate that can be used to later test a ScimResource (or child attribute).
   * <p>
   * The {@code attributeContainer} should be a top level Schema for the initial call, but recursive calls to this method
   * will pass the AttributeContainer (a child-schema).  For example, if given a
   * {@link org.apache.directory.scim.spec.resources.ScimUser ScimUser} expression of {@code emails.value co "example.org"},
   * The initial call to this method pass the ScimUser Schema object.  When this method is called recursively, the
   * {@link org.apache.directory.scim.spec.resources.Email Email}'s AttributeContainer will be used.
   *
   * @param expression the FilterExpression to build a predicate from.
   * @param attributeContainer the Schema (or other sub attribute container) to build the predicate from.
   * @return A Predicate that can be used to later test a ScimResource (or child attribute).
   */
  static <R> Predicate<R> toPredicate(FilterExpression expression, AttributeContainer attributeContainer) {

    // attribute EQ "something"
    if (expression instanceof AttributeComparisonExpression) {
      return new AttributeComparisonPredicate<>((AttributeComparisonExpression) expression, attributeContainer);
    }
    // (attribute EQ "something") AND (otherAttribute EQ "something else")
    else if (expression instanceof LogicalExpression) {
      LogicalExpression logicalExpression = (LogicalExpression) expression;
      Predicate<R> left = toPredicate(logicalExpression.getLeft(), attributeContainer);
      Predicate<R> right = toPredicate(logicalExpression.getRight(), attributeContainer);

      LogicalOperator op = logicalExpression.getOperator();
      if (op == LogicalOperator.AND) {
        return left.and(right);
      } else {
        return left.or(right);
      }
    }
    // NOT (attribute EQ "something")
    else if (expression instanceof GroupExpression) {
      GroupExpression groupExpression = (GroupExpression) expression;
      Predicate<R> predicate = toPredicate(groupExpression.getFilterExpression(), attributeContainer);
      return groupExpression.isNot()
        ? predicate.negate()
        : predicate;
    }
    // attribute PR
    else if (expression instanceof AttributePresentExpression) {
      return new AttributePresentPredicate<>((AttributePresentExpression) expression, attributeContainer);
    }
    // addresses[type EQ "work"]
    else if (expression instanceof ValuePathExpression) {
      ValuePathExpression valuePathExpression = (ValuePathExpression) expression;
      Predicate<Object> nestedPredicate = toPredicate(valuePathExpression.getAttributeExpression(), attribute(attributeContainer, valuePathExpression.getAttributePath()));
      return new ValuePathPredicate<>(valuePathExpression, attributeContainer, nestedPredicate);
    }
    log.debug("Unsupported Filter expression of type: " + expression.getClass());
    return scimResource -> false;
  }


  private static boolean isStringExpression(Schema.Attribute attribute, Object compareValue) {
    if (attribute.getType() != Schema.Attribute.Type.STRING) {
      log.debug("Invalid query, non String value for expression : " + attribute.getType());
      return false;
    }
    if (compareValue == null) {
      log.debug("Invalid query, empty value for expression : " + attribute.getType());
      return false;
    }
    return true;
  }

  private static Schema.Attribute attribute(AttributeContainer attributeContainer, AttributeReference attributeReference) {
    String baseAttributeName = attributeReference.getAttributeName();

    Schema.Attribute schemaAttribute = attributeContainer.getAttribute(baseAttributeName);
    if (schemaAttribute == null) {
      log.warn("Invalid filter: attribute '" + baseAttributeName + "' is NOT a valid SCIM attribute.");
      return null;
    }

    String subAttribute = attributeReference.getSubAttributeName();
    if (subAttribute != null) {
      schemaAttribute = schemaAttribute.getAttribute(subAttribute);
      if (schemaAttribute == null) {
        log.warn("Invalid filter: attribute '" + attributeReference.getFullyQualifiedAttributeName() + "' is NOT a valid SCIM attribute.");
        return null;
      }
    }

    // filter out fields like passwords that should not be queried against
    if (schemaAttribute.getReturned() == Schema.Attribute.Returned.NEVER) {
      log.warn("Invalid filter: attribute '" + attributeReference.getAttributeName() + "' is not filterable.");
      return null;
    }

    return schemaAttribute;
  }

  private static abstract class AbstractAttributePredicate<T extends FilterExpression, R> implements Predicate<R> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

    final T expression;

    final AttributeContainer attributeContainer;

    final AttributeReference attributeReference;

    private AbstractAttributePredicate(T expression, AttributeContainer attributeContainer, AttributeReference attributeReference) {
      this.expression = expression;
      this.attributeContainer = attributeContainer;
      this.attributeReference = attributeReference;
    }

    protected abstract boolean test(Schema.Attribute attribute, Object actualValue);

    @Override
    public boolean test(R actual) {

      try {
        // get and validate attribute
        Schema.Attribute resolvedAttribute = attribute(attributeContainer, attributeReference);
        if (resolvedAttribute != null) {

          // now walk the attribute path again to get the accessor and value
          Schema.Attribute schemaAttribute = attributeContainer.getAttribute(attributeReference.getAttributeName());
          actual = schemaAttribute.getAccessor().get(actual);

          // if the attribute has a sub-level, continue on
          String subAttribute = attributeReference.getSubAttributeName();
          if (subAttribute != null) {
            schemaAttribute = schemaAttribute.getAttribute(subAttribute);
            actual = schemaAttribute.getAccessor().get(actual);
          }
          return test(schemaAttribute, actual);
        }
      } catch (Exception e) {
        // The SCIM spec states to ignore the query instead of rejecting it - rfc7644 - 3.4.2
        log.debug("Invalid SCIM filter received", e);
      }

      return false;
    }
  }

  private static class ValuePathPredicate<R> extends AbstractAttributePredicate<ValuePathExpression, R> {

    final private Predicate<Object> nestedPredicate;

    ValuePathPredicate(ValuePathExpression expression, AttributeContainer attributeContainer, Predicate<Object> nestedPredicate) {
      super(expression, attributeContainer, expression.getAttributePath());
      this.nestedPredicate = nestedPredicate;
    }

    @Override
    protected boolean test(Schema.Attribute attribute, Object actualValue) {
      // actualValue must be a Collection
      if (attribute.isMultiValued()) {
        return ((Collection<?>) actualValue).stream().anyMatch(nestedPredicate);
      }

      return false;
    }
  }

  private static class AttributeComparisonPredicate<R> extends AbstractAttributePredicate<AttributeComparisonExpression, R> {

    private AttributeComparisonPredicate(AttributeComparisonExpression expression, AttributeContainer attributeContainer) {
      super(expression, attributeContainer, expression.getAttributePath());
    }

    @Override
    protected boolean test(Schema.Attribute attribute, Object actualValue) {

      if (actualValue == null) {
        return false;
      }

      if (attribute.isMultiValued()) {
        log.warn("Invalid expression, target is collection");
        return false;
      }

      CompareOperator op = expression.getOperation();
      Object compareValue = expression.getCompareValue();

      if (op == CompareOperator.EQ) {

        if (isStringExpression(attribute, compareValue) && !attribute.isCaseExact()) {
          return actualValue.toString().equalsIgnoreCase(compareValue.toString());
        }
        return compareValue.equals(actualValue);
      }
      if (op == CompareOperator.NE) {
        if (isStringExpression(attribute, compareValue) && !attribute.isCaseExact()) {
          return !actualValue.toString().equalsIgnoreCase(compareValue.toString());
        }
        return !compareValue.equals(actualValue);
      }
      if (op == CompareOperator.SW) {
        return isStringExpression(attribute, compareValue)
          && actualValue.toString().startsWith(compareValue.toString());
      }
      if (op == CompareOperator.EW) {
        return isStringExpression(attribute, compareValue)
          && actualValue.toString().endsWith(compareValue.toString());
      }
      if (op == CompareOperator.CO) {
        return isStringExpression(attribute, compareValue)
          && actualValue.toString().contains(compareValue.toString());
      }

      if (actualValue instanceof Comparable) {
        Comparable actual = (Comparable) actualValue;
        Comparable compare = (Comparable) compareValue;
        return CompareOperatorPredicate.naturalOrder(op, compare).test(actual);
      }

      throw new ScimResourceInvalidException("Unsupported operation in filter: " + op.name());
    }
  }

  private static class CompareOperatorPredicate<T> implements Predicate<T> {
    private final CompareOperator op;

    private final Comparator<T> comparator;

    private final T comparedValue;


    private CompareOperatorPredicate(CompareOperator op, T comparedValue, Comparator<T> comparator) {
      this.op = op;
      this.comparator = comparator;
      this.comparedValue = comparedValue;
    }

    @Override
    public boolean test(T actualValue) {

      int compareResult = comparator.compare(actualValue, comparedValue);

      if (op == CompareOperator.LT) {
        return compareResult < 0;
      } else if (op == CompareOperator.GT) {
        return compareResult > 0;
      } else if (op == CompareOperator.LE) {
        return compareResult <= 0;
      } else if (op == CompareOperator.GE) {
        return compareResult >= 0;
      }
      return false;
    }

    static <T extends Comparable<T>> CompareOperatorPredicate<T> naturalOrder(CompareOperator op, T comparedValue) {
      return new CompareOperatorPredicate<>(op, comparedValue, Comparator.naturalOrder());
    }
  }

  private static class AttributePresentPredicate<R> extends AbstractAttributePredicate<AttributePresentExpression, R> {
    private AttributePresentPredicate(AttributePresentExpression expression, AttributeContainer attributeContainer) {
      super(expression, attributeContainer, expression.getAttributePath());
    }

    @Override
    protected boolean test(Schema.Attribute attribute, Object actualValue) {
      if (attribute.isMultiValued()) {
        log.debug("Invalid expression, target is collection");
        return false;
      }
      return actualValue != null;
    }
  }
}
