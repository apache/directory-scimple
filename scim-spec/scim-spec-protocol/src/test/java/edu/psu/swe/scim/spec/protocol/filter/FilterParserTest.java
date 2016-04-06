/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stevemoyer
 *
 */
@RunWith(JUnitParamsRunner.class)
public class FilterParserTest extends AbstractLexerParserTest {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterParserTest.class);

  private FilterLexer lexer_;
  private FilterParserSubclass parser_;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    lexer_ = new FilterLexer(EXTRA_2);
    lexer_ = new FilterLexer(EXAMPLE_11);
    parser_ = new FilterParserSubclass(lexer_);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  private String[] getExamplesAndExtras() {
    return EXAMPLES_AND_EXTRAS;
  }
  
  @SuppressWarnings("unused")
  private String[] getNots() {
    return NOTS;
  }
  
  @Test
  @Parameters(method = "getExamplesAndExtras")
  public void testParse(String filter) {
    Expression<?> expression = null;
    try {
      expression = parser_.parse(filter);
    } catch (FilterParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  @Parameters(method = "getNots")
  public void testParseWithNots(String filter) {
    Expression<?> expression = null;
    try {
      expression = parser_.parse(filter);
    } catch (FilterParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
