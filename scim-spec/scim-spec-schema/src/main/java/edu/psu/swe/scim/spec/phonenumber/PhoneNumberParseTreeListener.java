package edu.psu.swe.scim.spec.phonenumber;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.GlobalNumberContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.LocalNumberContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.LocalNumberDigitsContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.ParameterContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.PhoneContextContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;
import edu.psu.swe.scim.spec.resources.PhoneNumber;

public class PhoneNumberParseTreeListener extends PhoneNumberParserBaseListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberParserBaseListener.class);

  private PhoneNumber phoneNumber = new PhoneNumber();

  private int indent = -1;

  @Override
  public void enterPhoneNumber(PhoneNumberContext ctx) {
    LOGGER.debug(indent("--- Enter PhoneNumber -->"));
  }

  @Override
  public void exitPhoneNumber(PhoneNumberContext ctx) {
    LOGGER.debug(indent("<-- Exit PhoneNumber ---"));
  }
  
  @Override
  public void enterLocalNumber(LocalNumberContext ctx) {
    LOGGER.debug(indent("--- Enter LocalNumber -->"));
  }

  @Override
  public void exitLocalNumber(LocalNumberContext ctx) {
    LOGGER.debug(indent("<-- Exit LocalNumber ---"));
    phoneNumber.setNumber(ctx.localDigits.getText());
    phoneNumber.setGlobalNumber(false);
    
    if (ctx.Ext() != null && !StringUtils.isBlank(ctx.Ext().getText())) {
      phoneNumber.setExtension(ctx.Ext().getText());
    }
    
    if (ctx.Isub() != null && !StringUtils.isBlank(ctx.Isub().getText())) {
      phoneNumber.setSubAddress(ctx.Isub().getText());
    }
  }

  @Override
  public void enterLocalNumberDigits(LocalNumberDigitsContext ctx) {
    LOGGER.debug(indent("--- Enter LocalNumberDigits -->"));
  }

  @Override
  public void exitLocalNumberDigits(LocalNumberDigitsContext ctx) {
    LOGGER.debug(indent("<-- Exit LocalNumberDigits ---"));
  }

  @Override
  public void enterPhoneContext(PhoneContextContext ctx) {
    LOGGER.debug(indent("--- Enter PhoneContext -->")); 
  }

  @Override
  public void exitPhoneContext(PhoneContextContext ctx) {
    LOGGER.debug(indent("<-- Exit PhoneContext ---"));
    if (!ctx.isEmpty()) {
      if (ctx.dig != null) {
        phoneNumber.setDomainPhoneContext(false);
        phoneNumber.setPhoneContext("+"+ctx.dig.getText());
      } else if (ctx.dn != null) {
        phoneNumber.setDomainPhoneContext(true);
        phoneNumber.setPhoneContext(ctx.dn.getText());
      }
    }
  }

  @Override
  public void enterParameter(ParameterContext ctx) {
    LOGGER.debug(indent("--- Enter Parameter -->"));
  }

  @Override
  public void exitParameter(ParameterContext ctx) {
    LOGGER.debug(indent("<-- Exit Parameter ---"));
    if (!ctx.isEmpty()) {
      phoneNumber.addParam(ctx.ParamName().getText(), ctx.ParamValue().getText());
    }
  }

  @Override
  public void enterGlobalNumber(GlobalNumberContext ctx) {
    LOGGER.debug(indent("--- Enter GlobalNumber -->"));
  }

  @Override
  public void exitGlobalNumber(GlobalNumberContext ctx) {
    LOGGER.debug(indent("<-- Exit GlobalNumber ---"));
    phoneNumber.setNumber(ctx.globalDigits.getText() + ctx.GlobalNumberDigits().getText());
    phoneNumber.setGlobalNumber(true);

    if (ctx.Ext() != null && !StringUtils.isBlank(ctx.Ext().getText())) {
      phoneNumber.setExtension(ctx.Ext().getText());
    }
    
    if (ctx.Isub() != null && !StringUtils.isBlank(ctx.Isub().getText())) {
      phoneNumber.setSubAddress(ctx.Isub().getText());
    }
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
      LOGGER.debug(indent(text));
    }
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    LOGGER.error(indent(node.getText()));
  }

  private String indent(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("    ");
    }
    sb.append(s);
    return sb.toString();
  }

  public PhoneNumber getPhoneNumber() {
    return phoneNumber;
  }
	
}
