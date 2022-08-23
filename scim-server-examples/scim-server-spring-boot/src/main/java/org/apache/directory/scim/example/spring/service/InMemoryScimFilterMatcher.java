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

package org.apache.directory.scim.example.spring.service;

import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.protocol.filter.*;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

final class InMemoryScimFilterMatcher {

  private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

  private InMemoryScimFilterMatcher(){}

  /**
   * Basic FilterExpression support for in-memory objects, actual implementations should translate the Filter into
   * the appropriate query language.
   */
  public static boolean matches(ScimResource resource, AttributeContainer attributeContainer, Filter filter) {

    // no filter defined, return everything
    if (filter == null) {
      return true;
    }
    FilterExpression expression = filter.getExpression();
    if (expression == null) {
      return true;
    }

    ScimResourceType scimResourceType = resource.getClass().getAnnotation(ScimResourceType.class);
    if (scimResourceType == null) {
      throw new ScimResourceInvalidException("SCM resource has not been configured with a ScimResourceType annotation");
    }
    return toPredicate(expression, attributeContainer).test(resource);
  }

  static Predicate<Object> toPredicate(FilterExpression expression, AttributeContainer attributeContainer) {

    // attribute EQ "something"
    if (expression instanceof AttributeComparisonExpression) {
      return new AttributeComparisonPredicate((AttributeComparisonExpression) expression, attributeContainer);
    }
    // (attribute EQ "something") AND (otherAttribute EQ "something else")
    else if (expression instanceof LogicalExpression) {
      LogicalExpression logicalExpression = (LogicalExpression) expression;
      Predicate<Object> left = toPredicate(logicalExpression.getLeft(), attributeContainer);
      Predicate<Object> right = toPredicate(logicalExpression.getRight(), attributeContainer);

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
      Predicate<Object> predicate = toPredicate(groupExpression.getFilterExpression(), attributeContainer);
      return groupExpression.isNot()
        ? predicate.negate()
        : predicate;
    }
    // attribute PR
    else if (expression instanceof AttributePresentExpression) {
      return new AttributePresentPredicate((AttributePresentExpression) expression, attributeContainer);
    }
    // addresses[type EQ "work"]
    else if (expression instanceof ValuePathExpression) {
      ValuePathExpression valuePathExpression = (ValuePathExpression) expression;
      return new ValuePathPredicate(valuePathExpression, attributeContainer);
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

  static class ValuePathPredicate extends AbstractAttributePredicate<ValuePathExpression> {

    public ValuePathPredicate(ValuePathExpression expression, AttributeContainer attributeContainer) {
      super(expression, attributeContainer, expression.getAttributePath().getFullAttributeName());
    }

    @Override
    boolean test(Schema.Attribute attribute, Object actualValue) {
      // actualValue must be a Collection
      if (attribute.isMultiValued()) {
        Predicate<Object> nestedPredicate = toPredicate(expression.getAttributeExpression(), attribute);
        return ((Collection<?>) actualValue).stream().anyMatch(nestedPredicate);
      }

      return false;
    }
  }

  static class AttributeComparisonPredicate extends AbstractAttributePredicate<AttributeComparisonExpression> {

    public AttributeComparisonPredicate(AttributeComparisonExpression expression, AttributeContainer attributeContainer) {
      super(expression, attributeContainer, expression.getAttributePath().getFullAttributeName());
    }

    @Override
    public boolean test(Schema.Attribute attribute, Object actualValue) {

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
        return compareValue.equals(actualValue);
      }
      if (op == CompareOperator.NE) {
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

  static class CompareOperatorPredicate<T> implements Predicate<T> {
    private final CompareOperator op;

    private final Comparator<T> comparator;

    private final T comparedValue;


    public CompareOperatorPredicate(CompareOperator op, T comparedValue, Comparator<T> comparator) {
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

  static class AttributePresentPredicate extends AbstractAttributePredicate<AttributePresentExpression> {
    public AttributePresentPredicate(AttributePresentExpression expression, AttributeContainer attributeContainer) {
      super(expression, attributeContainer, expression.getAttributePath().getFullAttributeName());
    }

    @Override
    boolean test(Schema.Attribute attribute, Object actualValue) {
      if (attribute.isMultiValued()) {
        log.debug("Invalid expression, target is collection");
        return false;
      }

      return actualValue != null;
    }
  }


  static abstract class AbstractAttributePredicate<T extends FilterExpression> implements Predicate<Object> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

    final T expression;

    final AttributeContainer attributeContainer;

    final String attribute;

    public AbstractAttributePredicate(T expression, AttributeContainer attributeContainer, String attribute) {
      this.expression = expression;
      this.attributeContainer = attributeContainer;
      this.attribute = attribute;
    }

    abstract boolean test(Schema.Attribute attribute, Object actualValue);

    @Override
    public boolean test(Object actual) {

      try {
        String[] attributePaths = attribute.split("\\.");
        Schema.Attribute schemaAttribute = attributeContainer.getAttribute(attributePaths[0]);
        if (schemaAttribute == null) {
          log.warn("Invalid filter: attribute '" + attribute + "' is NOT a valid SCIM attribute.");
          return false;
        }
        actual = schemaAttribute.getAccessor().get(actual);

        if (attributePaths.length > 1) {
          for (int index = 1; index < attributePaths.length; index++) {
            String attributePath = attributePaths[index];
            schemaAttribute = schemaAttribute.getAttribute(attributePath);
            if (schemaAttribute == null) {
              log.warn("Invalid filter: attribute '" + attributePath + "' is NOT a valid SCIM attribute.");
              return false;
            }
            actual = schemaAttribute.getAccessor().get(actual);
          }
        }

        // filter out fields like passwords that should not be queried against
        if (schemaAttribute.getReturned() == Schema.Attribute.Returned.NEVER) {
          log.warn("Invalid filter: attribute '" + attribute + "' is filterable.");
          return false;
        }

        return test(schemaAttribute, actual);
      } catch (Exception e) {
        // The SCIM spec states to ignore the query instead of rejecting it - rfc7644 - 3.4.2
        log.debug("Invalid SCIM filter received", e);
        return false;
      }
    }
  }
}
