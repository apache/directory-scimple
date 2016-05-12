package edu.psu.swe.scim.spec.protocol.filter;

import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.filter.FilterBaseListener;
import edu.psu.swe.scim.server.filter.FilterParser.AttrExpCompareOpContext;
import edu.psu.swe.scim.server.filter.FilterParser.AttrExpPresentContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterAttrExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterGroupExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterLogicExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.FilterValuePathContext;
import edu.psu.swe.scim.server.filter.FilterParser.ValFilterAttrExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.ValFilterGroupExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.ValFilterLogicExpContext;
import edu.psu.swe.scim.server.filter.FilterParser.ValuePathContext;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

public class ExpressionBuildingListener extends FilterBaseListener {

  private static final Logger LOG = LoggerFactory.getLogger(ExpressionBuildingListener.class);

  private Stack<FilterExpression> expressionStack = new Stack<>();

  private int indent = -1;

  @Override
  public void enterFilterLogicExp(FilterLogicExpContext ctx) {
    LOG.debug(indent("--- Enter FilterLogicExp -->"));
  }

  @Override
  public void exitFilterLogicExp(FilterLogicExpContext ctx) {
    LOG.debug(indent("<-- Exit FilterLogicExp ---"));
    
    LogicalOperator logicalOperator = LogicalOperator.valueOf(ctx.op.getText().toUpperCase());
    FilterExpression right = expressionStack.pop();
    FilterExpression left = expressionStack.pop();

    LogicalExpression expression = new LogicalExpression(left, logicalOperator, right);
    expressionStack.push(expression);
  }

  @Override
  public void enterFilterValuePath(FilterValuePathContext ctx) {
    LOG.debug(indent("--- Enter FilterValuePath -->"));
  }

  @Override
  public void exitFilterValuePath(FilterValuePathContext ctx) {
    LOG.debug(indent("<-- Exit FilterValuePath ---"));
  }

  @Override
  public void enterFilterAttrExp(FilterAttrExpContext ctx) {
    LOG.debug(indent("--- Enter FilterAttrExp -->"));
  }

  @Override
  public void exitFilterAttrExp(FilterAttrExpContext ctx) {
    LOG.debug(indent("<-- Exit FilterAttrExp ---"));
  }

  @Override
  public void enterFilterGroupExp(FilterGroupExpContext ctx) {
    LOG.debug(indent("--- Enter FilterGroupExp -->"));
  }

  @Override
  public void exitFilterGroupExp(FilterGroupExpContext ctx) {
    LOG.debug(indent("<-- Exit FilterGroupExp ---"));
    if (ctx.not != null) {
      FilterExpression pop = expressionStack.pop();
      
      GroupExpression expression = new GroupExpression(true, pop);
      expressionStack.push(expression);
    }
    
  }

  @Override
  public void enterValuePath(ValuePathContext ctx) {
    LOG.debug(indent("--- Enter ValuePath -->"));
  }

  @Override
  public void exitValuePath(ValuePathContext ctx) {
    LOG.debug(indent("<-- Exit ValuePath ---"));

    String attrPath = ctx.attrPath.getText();
    AttributeReference attrRef = new AttributeReference(attrPath);
    ValueFilterExpression valueFilter = (ValueFilterExpression) expressionStack.pop();
        
    ValuePathExpression expression = new ValuePathExpression(attrRef, valueFilter);
    expressionStack.push(expression);
  }

  @Override
  public void enterValFilterAttrExp(ValFilterAttrExpContext ctx) {
    LOG.debug(indent("--- Enter ValFilterAttrExp -->"));
  }

  @Override
  public void exitValFilterAttrExp(ValFilterAttrExpContext ctx) {
    LOG.debug(indent("<-- Exit ValFilterAttrExp ---"));
  }

  @Override
  public void enterValFilterLogicExp(ValFilterLogicExpContext ctx) {
    LOG.debug(indent("--- Enter ValFilterLogicExp -->"));
  }

  @Override
  public void exitValFilterLogicExp(ValFilterLogicExpContext ctx) {
    LOG.debug(indent("<-- Exit ValFilterLogicExp ---"));
    
    LogicalOperator logicalOperator = LogicalOperator.valueOf(ctx.op.getText().toUpperCase());
    FilterExpression right = expressionStack.pop();
    FilterExpression left = expressionStack.pop();

    LogicalExpression expression = new LogicalExpression(left, logicalOperator, right);
    expressionStack.push(expression);
  }

  @Override
  public void enterValFilterGroupExp(ValFilterGroupExpContext ctx) {
    LOG.debug(indent("--- Enter ValFilterGroupExp -->"));
  }

  @Override
  public void exitValFilterGroupExp(ValFilterGroupExpContext ctx) {
    LOG.debug(indent("<-- Exit ValFilterGroupExp ---"));
    if (ctx.not != null) {
      FilterExpression pop = expressionStack.pop();

      GroupExpression expression = new GroupExpression(true, pop);
      expressionStack.push(expression);
    }
  }

  @Override
  public void enterAttrExpPresent(AttrExpPresentContext ctx) {
    LOG.debug(indent("--- Enter AttrExpPresent -->"));
  }

  @Override
  public void exitAttrExpPresent(AttrExpPresentContext ctx) {
    LOG.debug(indent("<-- Exit AttrExpPresent ---"));

    String attrPath = ctx.attrPath.getText();
    AttributeReference attrRef = new AttributeReference(attrPath);

    AttributePresentExpression expression = new AttributePresentExpression(attrRef);
    expressionStack.push(expression);
  }

  @Override
  public void enterAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    LOG.debug(indent("--- Enter AttrExpCompareOp -->"));
  }

  @Override
  public void exitAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    LOG.debug(indent("<-- Exit AttrExpCompareOp ---"));

    String attrPath = ctx.attrPath.getText();
    AttributeReference attrRef = new AttributeReference(attrPath);
    CompareOperator compareOperator = CompareOperator.valueOf(ctx.op.getText().toUpperCase());
    Object value = parseJsonType(ctx.compValue.getText());

    AttributeComparisonExpression expression = new AttributeComparisonExpression(attrRef, compareOperator, value);
    expressionStack.push(expression);
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
      LOG.debug(indent(text));
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

  public FilterExpression getFilterExpression() {
    return expressionStack.peek();
  }

  private Object parseJsonType(String jsonValue) {
    if (jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
      return jsonValue.substring(1, jsonValue.length() - 1);
    } else if ("null".equals(jsonValue)) {
      return null;
    } else if ("true".equals(jsonValue)) {
      return true;
    } else if ("false".equals(jsonValue)) {
      return false;
    } else {
      try {
        Double d = Double.parseDouble(jsonValue);
        return d;
      } catch (NumberFormatException e) {
        LOG.warn("Unable to parse a json number: " + jsonValue);
      }
    }

    throw new IllegalStateException("Unable to parse JSON Value");
  }

}
