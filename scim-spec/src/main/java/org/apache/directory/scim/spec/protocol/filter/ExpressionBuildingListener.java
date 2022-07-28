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

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.scim.spec.protocol.filter.FilterParser.AttributeCompareExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.AttributeGroupExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.AttributeLogicExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.AttributePresentExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterAttributeCompareExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterAttributePresentExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterGroupExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterLogicExpressionContext;
import org.apache.directory.scim.spec.protocol.filter.FilterParser.FilterValuePathExpressionContext;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;

public class ExpressionBuildingListener extends FilterBaseListener {

  private static final Logger LOG = LoggerFactory.getLogger(ExpressionBuildingListener.class);

  protected Deque<FilterExpression> expressionStack = new ArrayDeque<>();

  @Override
  public void exitFilter(FilterContext ctx) {
    assert expressionStack.size() == 1 : "wrong number (" + expressionStack.size() + ") of expressions on stack, should be 1";
  }

  @Override
  public void exitFilterGroupExpression(FilterGroupExpressionContext ctx) {
    boolean not = ctx.not != null;
    FilterExpression pop = expressionStack.pop();
    GroupExpression expression = new GroupExpression(not, pop);

    expressionStack.push(expression);
  }

  @Override
  public void exitFilterValuePathExpression(FilterValuePathExpressionContext ctx) {
    String attributePath = ctx.attributePath.getText();
    AttributeReference attributeReference = new AttributeReference(attributePath);
    String urn = attributeReference.getUrn();
    String parentAttributeName = attributeReference.getAttributeName();

    if (expressionStack.size() == 0) {
      throw new IllegalStateException("Invalid Expression " + ctx.attributePath);
    }

    FilterExpression attributeExpression = (FilterExpression) expressionStack.pop();
    ValuePathExpression valuePathExpression = new ValuePathExpression(attributeReference, attributeExpression);

    attributeExpression.setAttributePath(urn, parentAttributeName);

    expressionStack.push(valuePathExpression);
  }

  @Override
  public void exitFilterAttributePresentExpression(FilterAttributePresentExpressionContext ctx) {
    AttributePresentExpression attributePresentExpression;
    String attributePathText = ctx.attributePath.getText();
    AttributeReference attributePath = new AttributeReference(attributePathText);
    attributePresentExpression = new AttributePresentExpression(attributePath);

    this.expressionStack.push(attributePresentExpression);
  }

  @Override
  public void exitFilterAttributeCompareExpression(FilterAttributeCompareExpressionContext ctx) {
    AttributeComparisonExpression attributeComparisonExpression;
    String attributePathText = ctx.attributePath.getText();
    AttributeReference attributePath = new AttributeReference(attributePathText);
    CompareOperator compareOperator = CompareOperator.valueOf(ctx.op.getText().toUpperCase());
    String compareValueText = ctx.compareValue.getText();
    Object compareValue = parseJsonType(compareValueText);
    attributeComparisonExpression = new AttributeComparisonExpression(attributePath, compareOperator, compareValue);

    this.expressionStack.push(attributeComparisonExpression);
  }

  @Override
  public void exitFilterLogicExpression(FilterLogicExpressionContext ctx) {
    String op = ctx.op.getText().toUpperCase();
    LogicalOperator logicalOperator = LogicalOperator.valueOf(op);
    FilterExpression right = expressionStack.pop();
    FilterExpression left = expressionStack.pop();
    LogicalExpression expression = new LogicalExpression(left, logicalOperator, right);

    expressionStack.push(expression);
  }

  @Override
  public void exitAttributeLogicExpression(AttributeLogicExpressionContext ctx) {
    String op = ctx.op.getText().toUpperCase();
    LogicalOperator logicalOperator = LogicalOperator.valueOf(op);
    FilterExpression right = expressionStack.pop();
    FilterExpression left = expressionStack.pop();
    LogicalExpression attributeLogicExpression = new LogicalExpression(left, logicalOperator, right);

    expressionStack.push(attributeLogicExpression);
  }

  @Override
  public void exitAttributeGroupExpression(AttributeGroupExpressionContext ctx) {
    boolean not = ctx.not != null;
    FilterExpression attributeExpression = expressionStack.pop();
    GroupExpression attributeGroupExpression = new GroupExpression(not, attributeExpression);

    expressionStack.push(attributeGroupExpression);
  }

  @Override
  public void exitAttributeCompareExpression(AttributeCompareExpressionContext ctx) {
    String attributeName = ctx.attributeName.getText();
    CompareOperator compareOperator = CompareOperator.valueOf(ctx.op.getText().toUpperCase());
    Object value = parseJsonType(ctx.compareValue.getText());
    AttributeReference attributeReference = new AttributeReference(attributeName);
    AttributeComparisonExpression expression = new AttributeComparisonExpression(attributeReference, compareOperator, value);

    expressionStack.push(expression);
  }

  @Override
  public void exitAttributePresentExpression(AttributePresentExpressionContext ctx) {
    String attributeName = ctx.attributeName.getText();
    AttributeReference attributeReference = new AttributeReference(attributeName);
    AttributePresentExpression attributePresentExpression = new AttributePresentExpression(attributeReference);

    expressionStack.push(attributePresentExpression);
  }

  public FilterExpression getFilterExpression() {
    return expressionStack.peek();
  }

  private static Object parseJsonType(String jsonValue) {
    if (jsonValue.startsWith("\"")) {
      String doubleEscaped = jsonValue.substring(1, jsonValue.length() - 1)
          // StringEscapeUtils follows the outdated JSON spec requiring "/" to be escaped, this could subtly break things
          .replaceAll("\\\\/", "\\\\\\\\/")
          // Just in case someone needs a single-quote with a backslash in front of it, this will be unnecessary with escapeJson()
          .replaceAll("\\\\'", "\\\\\\\\'");

      // TODO change this to escapeJson() when dependencies get upgraded
      return StringEscapeUtils.unescapeEcmaScript(doubleEscaped);
    } else if ("null".equals(jsonValue)) {
      return null;
    } else if ("true".equals(jsonValue)) {
      return true;
    } else if ("false".equals(jsonValue)) {
      return false;
    } else {
      try {
        if(jsonValue.contains(".")) {
          return Double.parseDouble(jsonValue);
        } else {
          return Integer.parseInt(jsonValue);
        }
      } catch (NumberFormatException e) {
        LOG.warn("Unable to parse a json number: " + jsonValue);
      }
    }

    throw new IllegalStateException("Unable to parse JSON Value");
  }

}
