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

package org.apache.directory.scim.spec.phonenumber;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.GlobalNumberContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.LocalNumberContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.LocalNumberDigitsContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.ParameterContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.PhoneContextContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.PhoneNumber.GlobalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.PhoneNumber.LocalPhoneNumberBuilder;
import org.apache.directory.scim.spec.resources.PhoneNumber.PhoneNumberBuilder;

public class PhoneNumberParseTreeListener extends PhoneNumberParserBaseListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberParserBaseListener.class);

  private PhoneNumberBuilder phoneNumberBuilder;

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
    phoneNumberBuilder = new LocalPhoneNumberBuilder();
  }

  @Override
  public void exitLocalNumber(LocalNumberContext ctx) {
    LOGGER.debug(indent("<-- Exit LocalNumber ---"));
    ((LocalPhoneNumberBuilder) phoneNumberBuilder).subscriberNumber(ctx.localDigits.getText());
    
    if (ctx.Ext() != null && !StringUtils.isBlank(ctx.Ext().getText())) {
      ((LocalPhoneNumberBuilder) phoneNumberBuilder).extension(ctx.Ext().getText());
    }
    
    if (ctx.Isub() != null && !StringUtils.isBlank(ctx.Isub().getText())) {
      ((LocalPhoneNumberBuilder) phoneNumberBuilder).subAddress(ctx.Isub().getText());
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
        ((LocalPhoneNumberBuilder) phoneNumberBuilder).isDomainPhoneContext(false);
        phoneNumberBuilder.phoneContext("+"+ctx.dig.getText());
      } else if (ctx.dn != null) {
        ((LocalPhoneNumberBuilder) phoneNumberBuilder).isDomainPhoneContext(true);
        phoneNumberBuilder.phoneContext(ctx.dn.getText());
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
      phoneNumberBuilder.param(ctx.ParamName().getText(), ctx.ParamValue().getText());
    }
  }

  @Override
  public void enterGlobalNumber(GlobalNumberContext ctx) {
    LOGGER.debug(indent("--- Enter GlobalNumber -->"));
    phoneNumberBuilder = new GlobalPhoneNumberBuilder();
  }

  @Override
  public void exitGlobalNumber(GlobalNumberContext ctx) {
    LOGGER.debug(indent("<-- Exit GlobalNumber ---"));
    ((GlobalPhoneNumberBuilder) phoneNumberBuilder).globalNumber(ctx.globalDigits.getText() + ctx.GlobalNumberDigits().getText());

    if (ctx.Ext() != null && !StringUtils.isBlank(ctx.Ext().getText())) {
      phoneNumberBuilder.extension(ctx.Ext().getText());
    }
    
    if (ctx.Isub() != null && !StringUtils.isBlank(ctx.Isub().getText())) {
      phoneNumberBuilder.subAddress(ctx.Isub().getText());
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

  public PhoneNumber getPhoneNumber() throws PhoneNumberParseException {
    return phoneNumberBuilder.build(false);
  }
	
}
