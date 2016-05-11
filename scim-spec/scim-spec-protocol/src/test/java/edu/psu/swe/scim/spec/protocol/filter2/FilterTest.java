package edu.psu.swe.scim.spec.protocol.filter2;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.filter2.FilterLexer;
import edu.psu.swe.scim.server.filter2.FilterParser;
import edu.psu.swe.scim.spec.protocol.filter.AbstractLexerParserTest;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
@Ignore
public class FilterTest extends AbstractLexerParserTest {

  private static final Logger LOG = LoggerFactory.getLogger(FilterTest.class);

  @SuppressWarnings("unused")
  private String[] getAllFilters() {
    return ALL;
  }
  
  @Test
  @Parameters(method = "getAllFilters")
  public void test(String filter) throws Exception {
    LOG.info("Running Filter Parser test on input: " + filter);
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
  }
}
