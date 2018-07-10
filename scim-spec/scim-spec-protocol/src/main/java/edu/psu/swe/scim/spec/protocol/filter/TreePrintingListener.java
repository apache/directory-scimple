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

package edu.psu.swe.scim.spec.protocol.filter;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.filter.FilterParser.AttributeCompareExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.AttributeGroupExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.AttributeLogicExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.AttributePresentExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterAttributeCompareExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterAttributeExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterAttributePresentExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterGroupExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterLogicExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterValuePathExpressionContext;
import edu.psu.swe.scim.server.filter.FilterParser.FullAttributePathContext;
import edu.psu.swe.scim.server.filter.FilterParser.PartialAttributePathContext;

public class TreePrintingListener extends ExpressionBuildingListener {

  private static final Logger LOG = LoggerFactory.getLogger(TreePrintingListener.class);

  private int indent = -1;

  @Override
  public void enterFilter(FilterContext ctx) {
    LOG.info(indent("--- Filter -->"));
    super.enterFilter(ctx);
  }

  @Override
  public void exitFilter(FilterContext ctx) {
    LOG.info(indent("<-- Filter ---"));
    super.exitFilter(ctx);
  }

  @Override
  public void enterFilterLogicExpression(FilterLogicExpressionContext ctx) {
    LOG.info(indent("--- FilterLogicExpression -->"));
    super.enterFilterLogicExpression(ctx);
  }

  @Override
  public void exitFilterLogicExpression(FilterLogicExpressionContext ctx) {
    LOG.info(indent("<-- FilterLogicExpression ---"));
    super.exitFilterLogicExpression(ctx);
  }

  @Override
  public void enterFilterGroupExpression(FilterGroupExpressionContext ctx) {
    LOG.info(indent("--- FilterGroupExpression -->"));
    super.enterFilterGroupExpression(ctx);
  }

  @Override
  public void exitFilterGroupExpression(FilterGroupExpressionContext ctx) {
    LOG.info(indent("<-- FilterGroupExpression ---"));
    super.exitFilterGroupExpression(ctx);
  }

  @Override
  public void enterFilterValuePathExpression(FilterValuePathExpressionContext ctx) {
    LOG.info(indent("--- FilterValuePathContext -->"));
    super.enterFilterValuePathExpression(ctx);
  }

  @Override
  public void exitFilterValuePathExpression(FilterValuePathExpressionContext ctx) {
    LOG.info(indent("<-- FilterValuePath ---"));
    super.exitFilterValuePathExpression(ctx);
  }

  @Override
  public void enterFilterAttributePresentExpression(FilterAttributePresentExpressionContext ctx) {
    LOG.info(indent("--- FilterAttributePresentExpression -->"));
    super.enterFilterAttributePresentExpression(ctx);
  }

  @Override
  public void exitFilterAttributePresentExpression(FilterAttributePresentExpressionContext ctx) {
    LOG.info(indent("<-- FilterAttributePresentExpression ---"));
    super.exitFilterAttributePresentExpression(ctx);
  }

  @Override
  public void enterFilterAttributeCompareExpression(FilterAttributeCompareExpressionContext ctx) {
    LOG.info(indent("--- FilterAttributeCompareExpression -->"));
    super.enterFilterAttributeCompareExpression(ctx);
  }

  @Override
  public void exitFilterAttributeCompareExpression(FilterAttributeCompareExpressionContext ctx) {
    LOG.info(indent("<-- FilterAttributeCompareExpression ---"));
    super.exitFilterAttributeCompareExpression(ctx);
  }

  @Override
  public void enterFilterAttributeExpression(FilterAttributeExpressionContext ctx) {
    LOG.info(indent("--- FilterAttributeExpression -->"));
    super.enterFilterAttributeExpression(ctx);
  }

  @Override
  public void exitFilterAttributeExpression(FilterAttributeExpressionContext ctx) {
    LOG.info(indent("<-- FilterAttributeExpression ---"));
    super.exitFilterAttributeExpression(ctx);
  }

  @Override
  public void enterAttributeLogicExpression(AttributeLogicExpressionContext ctx) {
    LOG.info(indent("--- AttributeLogicExpression -->"));
    super.enterAttributeLogicExpression(ctx);
  }

  @Override
  public void exitAttributeLogicExpression(AttributeLogicExpressionContext ctx) {
    LOG.info(indent("<-- AttributeLogicExpression ---"));
    super.exitAttributeLogicExpression(ctx);
  }

  @Override
  public void enterAttributeGroupExpression(AttributeGroupExpressionContext ctx) {
    LOG.info(indent("--- AttributeGroupExpression -->"));
    super.enterAttributeGroupExpression(ctx);
  }

  @Override
  public void exitAttributeGroupExpression(AttributeGroupExpressionContext ctx) {
    LOG.info(indent("<-- AttributeGroupExpression ---"));
    super.exitAttributeGroupExpression(ctx);
  }

  @Override
  public void enterAttributeCompareExpression(AttributeCompareExpressionContext ctx) {
    LOG.info(indent("--- AttributeCompareExpression -->"));
    super.enterAttributeCompareExpression(ctx);
  }

  @Override
  public void exitAttributeCompareExpression(AttributeCompareExpressionContext ctx) {
    LOG.info(indent("<-- AttributeCompareExpression ---"));
    super.exitAttributeCompareExpression(ctx);
  }

  @Override
  public void enterAttributePresentExpression(AttributePresentExpressionContext ctx) {
    LOG.info(indent("--- AttributePresentExpression -->"));
    super.enterAttributePresentExpression(ctx);
  }

  @Override
  public void exitAttributePresentExpression(AttributePresentExpressionContext ctx) {
    LOG.info(indent("<-- AttributePresentExpression ---"));
    super.exitAttributePresentExpression(ctx);
  }

  @Override
  public void enterFullAttributePath(FullAttributePathContext ctx) {
    LOG.info(indent("--- FullAttributePath -->"));
    super.enterFullAttributePath(ctx);
  }

  @Override
  public void exitFullAttributePath(FullAttributePathContext ctx) {
    LOG.info(indent("<-- FullAttributePath ---"));
    super.exitFullAttributePath(ctx);
  }

  @Override
  public void enterPartialAttributePath(PartialAttributePathContext ctx) {
    LOG.info(indent("--- PartialAttributePath -->"));
    super.enterPartialAttributePath(ctx);
  }

  @Override
  public void exitPartialAttributePath(PartialAttributePathContext ctx) {
    LOG.info(indent("<-- PartialAttributePath ---"));
    super.exitPartialAttributePath(ctx);
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    indent++;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    indent--;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    String text = node.getText();
    if (StringUtils.isNotEmpty(text.trim())) {
      LOG.info(indent(text));
    }
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    LOG.error(indent(node.getText()));
  }
  
  private String indent(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("    ");
    }
    sb.append(s);
    return sb.toString();
  }

}
