package edu.psu.swe.scim.spec.protocol.filter2;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

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

public class ExpressionBuildingListener extends FilterBaseListener {

  @Override
  public void enterFilterLogicExp(FilterLogicExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterFilterLogicExp(ctx);
  }

  @Override
  public void exitFilterLogicExp(FilterLogicExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitFilterLogicExp(ctx);
  }

  @Override
  public void enterFilterValuePath(FilterValuePathContext ctx) {
    // TODO Auto-generated method stub
    super.enterFilterValuePath(ctx);
  }

  @Override
  public void exitFilterValuePath(FilterValuePathContext ctx) {
    // TODO Auto-generated method stub
    super.exitFilterValuePath(ctx);
  }

  @Override
  public void enterFilterAttrExp(FilterAttrExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterFilterAttrExp(ctx);
  }

  @Override
  public void exitFilterAttrExp(FilterAttrExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitFilterAttrExp(ctx);
  }

  @Override
  public void enterFilterGroupExp(FilterGroupExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterFilterGroupExp(ctx);
  }

  @Override
  public void exitFilterGroupExp(FilterGroupExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitFilterGroupExp(ctx);
  }

  @Override
  public void enterValuePath(ValuePathContext ctx) {
    // TODO Auto-generated method stub
    super.enterValuePath(ctx);
  }

  @Override
  public void exitValuePath(ValuePathContext ctx) {
    // TODO Auto-generated method stub
    super.exitValuePath(ctx);
  }

  @Override
  public void enterValFilterAttrExp(ValFilterAttrExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterValFilterAttrExp(ctx);
  }

  @Override
  public void exitValFilterAttrExp(ValFilterAttrExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitValFilterAttrExp(ctx);
  }

  @Override
  public void enterValFilterLogicExp(ValFilterLogicExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterValFilterLogicExp(ctx);
  }

  @Override
  public void exitValFilterLogicExp(ValFilterLogicExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitValFilterLogicExp(ctx);
  }

  @Override
  public void enterValFilterGroupExp(ValFilterGroupExpContext ctx) {
    // TODO Auto-generated method stub
    super.enterValFilterGroupExp(ctx);
  }

  @Override
  public void exitValFilterGroupExp(ValFilterGroupExpContext ctx) {
    // TODO Auto-generated method stub
    super.exitValFilterGroupExp(ctx);
  }

  @Override
  public void enterAttrExpPresent(AttrExpPresentContext ctx) {
    // TODO Auto-generated method stub
    super.enterAttrExpPresent(ctx);
  }

  @Override
  public void exitAttrExpPresent(AttrExpPresentContext ctx) {
    // TODO Auto-generated method stub
    super.exitAttrExpPresent(ctx);
  }

  @Override
  public void enterAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    // TODO Auto-generated method stub
    super.enterAttrExpCompareOp(ctx);
  }

  @Override
  public void exitAttrExpCompareOp(AttrExpCompareOpContext ctx) {
    // TODO Auto-generated method stub
    super.exitAttrExpCompareOp(ctx);
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    // TODO Auto-generated method stub
    super.enterEveryRule(ctx);
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    // TODO Auto-generated method stub
    super.exitEveryRule(ctx);
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    // TODO Auto-generated method stub
    super.visitTerminal(node);
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    // TODO Auto-generated method stub
    super.visitErrorNode(node);
  }

}
