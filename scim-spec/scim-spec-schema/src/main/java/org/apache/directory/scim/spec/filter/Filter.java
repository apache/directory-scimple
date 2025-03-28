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

package org.apache.directory.scim.spec.filter;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 
 * @author Steve Moyer {@literal <smoyer@psu.edu>}
 */
public class Filter implements Serializable {
    /** A logger for this class */
    private static final Logger log = LoggerFactory.getLogger(Filter.class);

  private static final long serialVersionUID = -363511683199922297L;

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
    this.filter = filterExpression.toFilter();
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
    FilterLexer l = new FilterLexer(CharStreams.fromString(filter));
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


  public String encode() {
    String filterString = expression.toFilter();
    return URLEncoder.encode(filterString, UTF_8).replace("+", "%20");
  }

  public static Filter decode(String encodedExpression) throws FilterParseException {
    String decoded = URLDecoder.decode(encodedExpression, UTF_8).replace("%20", " ");
    return new Filter(decoded);
  }

  public FilterExpression getExpression() {
    return this.expression;
  }

  public String getFilter() {
    return this.filter;
  }


  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Filter)) return false;
    final Filter other = (Filter) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$expression = this.getExpression();
    final Object other$expression = other.getExpression();
    if (this$expression == null ? other$expression != null : !this$expression.equals(other$expression)) return false;
    final Object this$filter = this.getFilter();
    final Object other$filter = other.getFilter();
    if (this$filter == null ? other$filter != null : !this$filter.equals(other$filter)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Filter;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $expression = this.getExpression();
    result = result * PRIME + ($expression == null ? 43 : $expression.hashCode());
    final Object $filter = this.getFilter();
    result = result * PRIME + ($filter == null ? 43 : $filter.hashCode());
    return result;
  }
}
