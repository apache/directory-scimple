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

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

/**
 * The {@code BaseFilterExpressionMapper} is a utility class to aid in conversion of a {@link FilterExpression} into a
 * class that can be used to look up or search for SCIM Resources. For example, convert a
 * FilterExpression into a SQL query.
 * @param <R>
 */
public abstract class BaseFilterExpressionMapper<R> implements BiFunction<FilterExpression, AttributeContainer, R> {

  private static final Logger log = LoggerFactory.getLogger(BaseFilterExpressionMapper.class);

  @Override
  public R apply(FilterExpression expression, AttributeContainer attributeContainer) {

    // attribute EQ "something"
    if (expression instanceof AttributeComparisonExpression) {
      return apply((AttributeComparisonExpression) expression, attributeContainer);
    }
    // (attribute EQ "something") AND (otherAttribute EQ "something else")
    else if (expression instanceof LogicalExpression) {
      return apply((LogicalExpression) expression, attributeContainer);
    }
    // NOT (attribute EQ "something")
    else if (expression instanceof GroupExpression) {
      return apply((GroupExpression) expression, attributeContainer);
    }
    // attribute PR
    else if (expression instanceof AttributePresentExpression) {
      return apply((AttributePresentExpression) expression, attributeContainer);
    }
    // addresses[type EQ "work"]
    else if (expression instanceof ValuePathExpression) {
      return apply((ValuePathExpression) expression, attributeContainer);
    }
    return unhandledExpression(expression, attributeContainer);
  }

  protected abstract R apply(AttributeComparisonExpression expression, AttributeContainer attributeContainer);

  protected R apply(LogicalExpression expression, AttributeContainer attributeContainer) {
    R left = apply(expression.getLeft(), attributeContainer);
    R right = apply(expression.getRight(), attributeContainer);

    LogicalOperator op = expression.getOperator();
    return apply(op, left, right);
  }

  protected abstract R apply(LogicalOperator op, R left, R right);

  protected R apply(GroupExpression expression, AttributeContainer attributeContainer) {
    R result = apply(expression.getFilterExpression(), attributeContainer);
    return expression.isNot()
      ? negate(result)
      : result;
  }

  protected abstract R negate(R expression);

  protected abstract R apply(AttributePresentExpression expression, AttributeContainer attributeContainer);

  protected abstract R apply(ValuePathExpression expression, AttributeContainer attributeContainer);

  protected R unhandledExpression(FilterExpression expression, AttributeContainer attributeContainer) {
    throw new IllegalArgumentException("FilterExpression '" + expression + "' is not supported");
  }

  protected static Schema.Attribute attribute(AttributeContainer attributeContainer, AttributeReference attributeReference) {
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

  protected static boolean isStringExpression(Schema.Attribute attribute, Object compareValue) {
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
}
