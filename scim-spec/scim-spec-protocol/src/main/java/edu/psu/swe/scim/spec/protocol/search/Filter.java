package edu.psu.swe.scim.spec.protocol.search;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.filter.FilterLexer;
import edu.psu.swe.scim.server.filter.FilterParser;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.filter.TreePrintingListener;

/**
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
@Data
public class Filter {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Filter.class);

  @Setter(AccessLevel.NONE)
  private FilterExpression expression;
  private String filter;
  
  protected Filter() {
  }

  public Filter(String filter) throws FilterParseException {
    setFilter(filter);
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
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });
    

    ParseTree tree = p.filter();
    ParseTreeListener listener = new TreePrintingListener();
    ParseTreeWalker.DEFAULT.walk(listener, tree);
    
    return null;
  }
}
