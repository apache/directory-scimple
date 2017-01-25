package edu.psu.swe.scim.spec.phonenumber;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreePrintingListener extends PhoneNumberBaseListener {

  private int indent = -1;
  
  @Override
  public void enterPhoneNumber(PhoneNumberContext ctx) {
    log.info(indent("--- Enter PhoneNumber -->"));
  }

  @Override
  public void exitPhoneNumber(PhoneNumberContext ctx) {
    log.info(indent("<-- Exit PhoneNumber ---"));
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
      log.info(indent(text));
    }
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    log.error(indent(node.getText()));
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
