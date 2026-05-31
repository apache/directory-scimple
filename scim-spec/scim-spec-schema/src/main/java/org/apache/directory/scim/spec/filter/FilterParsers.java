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

import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Parses SCIM filter expressions and PATCH operation paths, applying a nesting-depth limit
 * so that pathologically nested input is rejected as an ordinary parse error rather than
 * exhausting the thread stack.
 *
 * <p>Callers supply their own {@link ParseTreeListener} and read the result from it after
 * the call returns; the underlying ANTLR lexer/parser and the depth check are internal.
 */
public final class FilterParsers {

  /**
   * Maximum nesting depth accepted while parsing. One unit is counted for each entry into
   * the recursive {@code filterExpression} or {@code attributeExpression} grammar rule
   * (roughly one level of {@code ( )} grouping); non-recursive rules are not counted.
   *
   * <p>This is a safety bound that keeps recursive-descent parsing from overflowing the
   * stack — not a promise about an exact number of parentheses. The usable group count is
   * slightly lower than this value and differs a little between filter and PATCH-path
   * inputs because of fixed grammar overhead.
   */
  public static final int MAX_NESTING_DEPTH = 40;

  private FilterParsers() {
  }

  /**
   * Parses a SCIM filter expression, walking the supplied listener over the parse tree.
   *
   * @param filter   the raw filter string; must not be {@code null}
   * @param listener the listener to walk over the resulting parse tree
   * @throws FilterParseException if the input is {@code null}, malformed, or more deeply
   *         nested than {@link #MAX_NESTING_DEPTH}
   */
  public static void parseFilter(String filter, ParseTreeListener listener)
      throws FilterParseException {
    parse(filter, FilterParser::filter, listener, "filter expression");
  }

  /**
   * Parses a SCIM PATCH operation path, walking the supplied listener over the parse tree.
   *
   * @param patchPath the raw PATCH path string; must not be {@code null}
   * @param listener  the listener to walk over the resulting parse tree
   * @throws FilterParseException if the input is {@code null}, malformed, or more deeply
   *         nested than {@link #MAX_NESTING_DEPTH}
   */
  public static void parsePatchPath(String patchPath, ParseTreeListener listener)
      throws FilterParseException {
    parse(patchPath, FilterParser::patchPath, listener, "patch path expression");
  }

  private static void parse(String input, Function<FilterParser, ParseTree> rule,
      ParseTreeListener listener, String what) throws FilterParseException {
    if (input == null) {
      throw new FilterParseException("Filter input must not be null");
    }

    FilterLexer lexer = new FilterLexer(CharStreams.fromString(input));
    lexer.removeErrorListeners();
    lexer.addErrorListener(SyntaxErrorListener.INSTANCE);

    CommonTokenStream tokens = new CommonTokenStream(lexer);
    FilterParser parser = new FilterParser(tokens);
    parser.setBuildParseTree(true);
    parser.removeErrorListeners();
    parser.addErrorListener(SyntaxErrorListener.INSTANCE);
    parser.addParseListener(new DepthCountingListener());

    try {
      ParseTree tree = rule.apply(parser);
      ParseTreeWalker.DEFAULT.walk(listener, tree);
    } catch (DepthLimitExceededException e) {
      // Keep the depth message (no raw input); preserve the cause for diagnostics.
      throw new FilterParseException(e.getMessage(), e);
    } catch (IllegalStateException e) {
      // Static message — never echo ANTLR token text back to the caller.
      throw new FilterParseException("Failed to parse " + what, e);
    }
  }

  /**
   * Turns ANTLR syntax errors into an {@link IllegalStateException} so they can be wrapped
   * in a {@link FilterParseException} with a static, leak-free message. Stateless; shared.
   */
  private static final class SyntaxErrorListener extends BaseErrorListener {

    static final SyntaxErrorListener INSTANCE = new SyntaxErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
        int line, int charPositionInLine, String msg, RecognitionException e) {
      throw new IllegalStateException(
          "failed to parse at line " + line + ":" + charPositionInLine, e);
    }
  }

  /**
   * Counts entries into the two recursive grammar rules during parsing and aborts the parse
   * (before the stack can overflow) once {@link #MAX_NESTING_DEPTH} is exceeded. One instance
   * per parse, as it holds mutable depth state.
   */
  private static final class DepthCountingListener implements ParseTreeListener {

    private int depth;

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
      if (isNestingRule(ctx) && ++depth > MAX_NESTING_DEPTH) {
        throw new DepthLimitExceededException(MAX_NESTING_DEPTH);
      }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
      if (isNestingRule(ctx)) {
        depth--;
      }
    }

    private static boolean isNestingRule(ParserRuleContext ctx) {
      int ruleIndex = ctx.getRuleIndex();
      return ruleIndex == FilterParser.RULE_filterExpression
          || ruleIndex == FilterParser.RULE_attributeExpression;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
    }
  }

  /** Internal sentinel thrown when {@link #MAX_NESTING_DEPTH} is exceeded mid-parse. */
  private static final class DepthLimitExceededException extends RuntimeException {

    DepthLimitExceededException(int limit) {
      super("Filter nesting depth exceeds maximum of " + limit + " levels");
    }
  }
}
