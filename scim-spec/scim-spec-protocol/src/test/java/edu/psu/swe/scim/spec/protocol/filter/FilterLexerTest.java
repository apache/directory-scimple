/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class FilterLexerTest extends AbstractLexerParserTest {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterLexerTest.class);

  private FilterLexer lexer_;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    lexer_ = new FilterLexer(EXTRA_2);
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
  
  @SuppressWarnings("unused")
  private String[] getAttributeValuesWithSpacesNewLinesAndCarriageReturns() {
    return INPUT_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS;
  }
  
  @Test
  @Parameters(method = "getExamplesAndExtras")
  public void testFilterLexer(String filterString) {
    LOGGER.info("Lexing filter string: " + filterString);
    FilterLexerSubclass lexer = new FilterLexerSubclass(filterString);
    int index = Arrays.asList(EXAMPLES_AND_EXTRAS).indexOf(filterString);
    assertTrue("Update the test class so that INPUTS and EXPECTEDS have the same number of elements", index > -1 && index < EXAMPLES_AND_EXTRAS.length);
    assertArrayEquals(EXAMPLE_AND_EXTRA_INFIX_TOKENS[index], lexer.getTokens().toArray());
  }
  
  @Test
  @Parameters(method = "getNots")
  public void testFilterLexerWithNot(String filterString) {
    LOGGER.info("Lexing filter string: " + filterString);
    FilterLexerSubclass lexer = new FilterLexerSubclass(filterString);
    if(LOGGER.isInfoEnabled()) {
      lexer.logTokens(lexer.getTokens());
    }
    int index = Arrays.asList(NOTS).indexOf(filterString);
    assertTrue("Update the test class so that EXAMPLES and TOKENS have the same number of elements", index > -1 && index < NOT_INFIX_TOKENS.length);
    assertArrayEquals(NOT_INFIX_TOKENS[index], lexer.getTokens().toArray());
  }
  
  @Test
  @Parameters(method = "getAttributeValuesWithSpacesNewLinesAndCarriageReturns")
  public void testFilterLexerWithSpacesNewLinesAndCarriageReturnsInAttributeValues(String filterString) {
    LOGGER.info("Lexing filter string: " + filterString);
    FilterLexerSubclass lexer = new FilterLexerSubclass(filterString);
    int index = Arrays.asList(INPUT_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS).indexOf(filterString);
    assertTrue("Update the test class so that EXAMPLES and TOKENS have the same number of elements", index > -1 && index < INPUT_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS.length);
    assertArrayEquals(EXPECTED_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS[index], lexer.getTokens().toArray());
  }
  
  @Test
  public void testHasNext() {
    assertTrue(lexer_.hasNext());
    FilterLexer emptyLexer = new FilterLexer(NO_FILTER);
    assertFalse(emptyLexer.hasNext());
  }
  
  @Test
  public void testNext() {
    List<String> tokens = new ArrayList<String>(lexer_.getTokens());
    if(LOGGER.isDebugEnabled()) {
      lexer_.logTokens(tokens);
    }
    int tokenCount = tokens.size();
    LOGGER.debug("Tokens: " + tokenCount);
    for(int i = 0; i < tokenCount; i++) {
      assertTrue(lexer_.hasNext());
      String token = lexer_.next();
      LOGGER.debug("Expected: " + tokens.get(i) + ", Actual: " + token);
      assertEquals(tokens.get(i), token);
    }
    assertFalse(lexer_.hasNext());
  }
  
  @Test
  public void testRemove() {
    try {
      lexer_.remove();
      fail("This should have thrown an UnsupportedOperationException");
    } catch(UnsupportedOperationException e) {
      // This is the expected result
    } catch(Exception e) {
      fail("This should have thrown an UnsupportedOperationException");
    }
  }

}
