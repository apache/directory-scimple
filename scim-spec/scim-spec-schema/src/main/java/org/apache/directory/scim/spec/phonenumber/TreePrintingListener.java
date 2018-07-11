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

import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.GlobalNumberContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.LocalNumberContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.LocalNumberDigitsContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.ParameterContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.PhoneContextContext;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreePrintingListener extends PhoneNumberParserBaseListener {

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
  public void enterGlobalNumber(GlobalNumberContext ctx) {
    log.info(indent("<-- Enter GlobalNumber ---"));
  }

  @Override
  public void exitGlobalNumber(GlobalNumberContext ctx) {
    log.info(indent("<-- Exit GlobalNumber ---"));
  }

  @Override
  public void enterLocalNumber(LocalNumberContext ctx) {
    log.info(indent("<-- Enter LocalNumber ---"));
  }

  @Override
  public void exitLocalNumber(LocalNumberContext ctx) {
    log.info(indent("<-- Exit LocalNumber ---"));
  }
  
  @Override
  public void enterPhoneContext(PhoneContextContext ctx) {
	log.info(indent("<-- Enter PhoneContext "));
  }

  @Override
  public void exitPhoneContext(PhoneContextContext ctx) {
	log.info(indent("<-- Exit PhoneContext"));
  }

  @Override
  public void enterParameter(ParameterContext ctx) {
	log.info(indent("<-- Enter Parameter"));
  }

  @Override
  public void exitParameter(ParameterContext ctx) {
	log.info(indent("<-- Exit Parameter"));
  }

  @Override
  public void enterLocalNumberDigits(LocalNumberDigitsContext ctx) {
    log.info(indent("<-- Enter LocalNumberDigits"));
  }

  @Override
  public void exitLocalNumberDigits(LocalNumberDigitsContext ctx) {
    log.info(indent("<-- Exit LocalNumberDigits"));
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
