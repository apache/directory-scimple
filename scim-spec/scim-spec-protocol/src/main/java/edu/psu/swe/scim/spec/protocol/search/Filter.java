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

package edu.psu.swe.scim.spec.protocol.search;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import edu.psu.swe.scim.server.filter.FilterLexer;
import edu.psu.swe.scim.server.filter.FilterParser;
import edu.psu.swe.scim.spec.protocol.filter.ExpressionBuildingListener;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
@Data
@Slf4j
public class Filter {
  
  @Setter(AccessLevel.NONE)
  private FilterExpression expression;
  private String filter;
  
  protected Filter() {
  }

  public Filter(String filter) throws FilterParseException {
    log.debug("Creating a filter - {}", filter);
    setFilter(filter);
  }
  
  public Filter(FilterExpression filterExpression) {
    log.debug("Creating a filter - {}", filterExpression);
    expression = filterExpression;
    this.filter = filterExpression.toString();
  }
  
  /**
   * @param filter the filter to set
   * @throws FilterParseException 
   */
  public void setFilter(String filter) throws FilterParseException {
    this.filter = filter;
    this.expression = parseFilter(filter);
  }

  protected FilterExpression parseFilter(String filter) throws FilterParseException {
    FilterLexer l = new FilterLexer(new ANTLRInputStream(filter));
    FilterParser p = new FilterParser(new CommonTokenStream(l));
    p.setBuildParseTree(true);

    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e);
      }
    });

    try {
      ParseTree tree = p.filter();
      ExpressionBuildingListener expListener = new ExpressionBuildingListener();
      ParseTreeWalker.DEFAULT.walk(expListener, tree);
      
      return expListener.getFilterExpression();
    } catch (IllegalStateException e) {
      throw new FilterParseException("Failed to parse filter: " + filter, e);
    }
  }
  
  @Override
  public String toString() {
    return expression.toFilter();
  }
}
