package edu.psu.swe.scim.spec.protocol.filter2;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.filter2.FilterBaseListener;
import edu.psu.swe.scim.server.filter2.FilterParser.AttrExpCompareOpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.AttrExpPresentContext;
import edu.psu.swe.scim.server.filter2.FilterParser.FilterAttrExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.FilterGroupExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.FilterLogicExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.FilterValuePathContext;
import edu.psu.swe.scim.server.filter2.FilterParser.ValFilterAttrExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.ValFilterGroupExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.ValFilterLogicExpContext;
import edu.psu.swe.scim.server.filter2.FilterParser.ValuePathContext;

public class TreePrintingListener extends FilterBaseListener {

  private static final Logger LOG = LoggerFactory.getLogger(TreePrintingListener.class);

  private int indent = -1;
  
  @Override
  public void enterFilterLogicExp(FilterLogicExpContext ctx) {
    LOG.info(indent("--- Enter FilterLogicExp -->"));
  }

  @Override
  public void exitFilterLogicExp(FilterLogicExpContext ctx) {
    LOG.info(indent("<-- Exit FilterLogicExp ---"));
  }

  @Override
  public void enterFilterValuePath(FilterValuePathContext ctx) {
    LOG.info(indent("--- Enter FilterValuePath -->"));
  }

  @Override
  public void exitFilterValuePath(FilterValuePathContext ctx) {
    LOG.info(indent("<-- Exit FilterValuePath ---"));
  }

  @Override
  public void enterFilterAttrExp(FilterAttrExpContext ctx) {
    LOG.info(indent("--- Enter FilterAttrExp -->"));
  }

  @Override
  public void exitFilterAttrExp(FilterAttrExpContext ctx) {
    LOG.info(indent("<-- Exit FilterAttrExp ---"));
  }

  @Override
  public void enterFilterGroupExp(FilterGroupExpContext ctx) {
    LOG.info(indent("--- Enter FilterGroupExp -->"));
  }

  @Override
  public void exitFilterGroupExp(FilterGroupExpContext ctx) {
    LOG.info(indent("<-- Exit FilterGroupExp ---"));
  }

  @Override
  public void enterValuePath(ValuePathContext ctx) {
    LOG.info(indent("--- Enter ValuePath -->"));
  }

  @Override
  public void exitValuePath(ValuePathContext ctx) {
    LOG.info(indent("<-- Exit ValuePath ---"));
  }

  @Override
  public void enterValFilterAttrExp(ValFilterAttrExpContext ctx) {
    LOG.info(indent("--- Enter ValFilterAttrExp -->"));
  }

  @Override
  public void exitValFilterAttrExp(ValFilterAttrExpContext ctx) {
    LOG.info(indent("<-- Exit ValFilterAttrExp ---"));
  }

  @Override
  public void enterValFilterLogicExp(ValFilterLogicExpContext ctx) {
    LOG.info(indent("--- Enter ValFilterLogicExp -->"));
  }

  @Override
  public void exitValFilterLogicExp(ValFilterLogicExpContext ctx) {
    LOG.info(indent("<-- Exit ValFilterLogicExp ---"));
  }

  @Override
  public void enterValFilterGroupExp(ValFilterGroupExpContext ctx) {
    LOG.info(indent("--- Enter ValFilterGroupExp -->"));
  }

  @Override
  public void exitValFilterGroupExp(ValFilterGroupExpContext ctx) {
    LOG.info(indent("<-- Exit ValFilterGroupExp ---"));
  }

  @Override
  public void enterAttrExpPresent(AttrExpPresentContext ctx) {
    LOG.info(indent("--- Enter AttrExpPresent -->"));
  }

  @Override
  public void exitAttrExpPresent(AttrExpPresentContext ctx) {
    LOG.info(indent("<-- Exit AttrExpPresent ---"));
  }

  @Override
  public void enterAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    LOG.info(indent("--- Enter AttrExpCompareOp -->"));
  }

  @Override
  public void exitAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    LOG.info(indent("<-- Exit AttrExpCompareOp ---"));
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
