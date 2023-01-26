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

package org.apache.directory.scim.spec.filter;

import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

class InMemoryScimFilterMatcher<R> extends BaseFilterExpressionMapper<Predicate<R>> {

  private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

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
    return new InMemoryScimFilterMatcher<R>().apply(expression, attributeContainer);
  }

  @Override
  protected Predicate<R> apply(AttributeComparisonExpression expression, AttributeContainer attributeContainer) {
    return new AttributeComparisonPredicate<>(expression, attributeContainer);
  }

  @Override
  protected Predicate<R> apply(LogicalOperator op, Predicate<R> left, Predicate<R> right) {
    if (op == LogicalOperator.AND) {
      return left.and(right);
    } else {
      return left.or(right);
    }
  }

  @Override
  protected Predicate<R> negate(Predicate<R> expression) {
    return expression.negate();
  }

  @Override
  protected Predicate<R> apply(AttributePresentExpression expression, AttributeContainer attributeContainer) {
    return new AttributePresentPredicate<>(expression, attributeContainer);
  }

  @Override
  protected Predicate<R> apply(ValuePathExpression expression, AttributeContainer attributeContainer) {
    Predicate<Object> nestedPredicate = new InMemoryScimFilterMatcher<>().apply(expression.getAttributeExpression(), attribute(attributeContainer, expression.getAttributePath()));
    return new ValuePathPredicate<>(expression, attributeContainer, nestedPredicate);
  }

  @Override
  protected Predicate<R> unhandledExpression(FilterExpression expression, AttributeContainer attributeContainer) {
    log.debug("Unsupported Filter expression of type: " + expression.getClass());
    return scimResource -> false;
  }

  public <A> A get(Schema.Attribute attribute, A actual) {
    return attribute.getAccessor().get(actual);
  }

  private abstract class AbstractAttributePredicate<T extends FilterExpression, R> implements Predicate<R> {

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

          // check if the filter is nested such as: `emails[type eq "work"].value`
          if (!(attributeReference.hasSubAttribute() && schemaAttribute.isMultiValued())) {
            actual = get(schemaAttribute, actual);
          }
          // if the attribute has a sub-level, continue on
          String subAttribute = attributeReference.getSubAttributeName();
          if (subAttribute != null) {
            schemaAttribute = schemaAttribute.getAttribute(subAttribute);
            actual = get(schemaAttribute, actual);
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

  private class ValuePathPredicate<R> extends AbstractAttributePredicate<ValuePathExpression, R> {

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

  private class AttributeComparisonPredicate<R> extends AbstractAttributePredicate<AttributeComparisonExpression, R> {

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

  private class AttributePresentPredicate<R> extends AbstractAttributePredicate<AttributePresentExpression, R> {
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
