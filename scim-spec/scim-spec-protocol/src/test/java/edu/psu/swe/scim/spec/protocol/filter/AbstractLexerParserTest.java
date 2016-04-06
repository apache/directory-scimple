/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author stevemoyer
 *
 */
public abstract class AbstractLexerParserTest {

  protected static final String NO_FILTER = "";
  
  protected static final String JOHN_FILTER_MIXED_CASE_1 = "userName Eq \"john\"";
  protected static final String JOHN_FILTER_MIXED_CASE_2 = "Username eq \"john\"";
  
  protected static final String EXAMPLE_1 = "userName eq \"bjensen\"";
  protected static final String EXAMPLE_2 = "name.familyName co \"O'Malley\"";
  protected static final String EXAMPLE_3 = "userName sw \"J\"";
  protected static final String EXAMPLE_4 = "title pr";
  protected static final String EXAMPLE_5 = "meta.lastModified gt \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_6 = "meta.lastModified ge \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_7 = "meta.lastModified lt \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_8 = "meta.lastModified le \"2011-05-13T04:42:34Z\"";
  protected static final String EXAMPLE_9 = "title pr and userType eq \"Employee\"";
  protected static final String EXAMPLE_10 = "title pr or userType eq \"Intern\"";
  protected static final String EXAMPLE_11 = "userType eq \"Employee\" and (emails co \"example.com\" or emails co \"example.org\")";
  
  protected static final String EXTRA_1 = "(emails co \"example.com\" or emails co \"example.org\")";
  protected static final String EXTRA_2 = "(emails co \"example.com\" or emails co \"example.org\") and userType eq \"Employee\"";

  protected static final String[] EXAMPLE_1_INFIX_TOKENS = {"userName", "EQ", "\"bjensen\""};
  protected static final String[] EXAMPLE_2_INFIX_TOKENS = {"name.familyName", "CO", "\"O'Malley\""};
  protected static final String[] EXAMPLE_3_INFIX_TOKENS = {"userName", "SW", "\"J\""};
  protected static final String[] EXAMPLE_4_INFIX_TOKENS = {"title", "PR"};
  protected static final String[] EXAMPLE_5_INFIX_TOKENS = {"meta.lastModified", "GT", "\"2011-05-13T04:42:34Z\""};
  protected static final String[] EXAMPLE_6_INFIX_TOKENS = {"meta.lastModified", "GE", "\"2011-05-13T04:42:34Z\""};
  protected static final String[] EXAMPLE_7_INFIX_TOKENS = {"meta.lastModified", "LT", "\"2011-05-13T04:42:34Z\""};
  protected static final String[] EXAMPLE_8_INFIX_TOKENS = {"meta.lastModified", "LE", "\"2011-05-13T04:42:34Z\""};
  protected static final String[] EXAMPLE_9_INFIX_TOKENS = {"title", "PR", "AND", "userType", "EQ", "\"Employee\""};
  protected static final String[] EXAMPLE_10_INFIX_TOKENS = {"title", "PR", "OR", "userType", "EQ", "\"Intern\""};
  protected static final String[] EXAMPLE_11_INFIX_TOKENS = {"userType", "EQ", "\"Employee\"", "AND", "(", "emails", "CO", "\"example.com\"", "OR", "emails", "CO", "\"example.org\"", ")"};
  
  protected static final String[] EXTRA_1_INFIX_TOKENS = {"(", "emails", "CO", "\"example.com\"", "OR", "emails", "CO", "\"example.org\"", ")"};
  protected static final String[] EXTRA_2_INFIX_TOKENS = {"(", "emails", "CO", "\"example.com\"", "OR", "emails", "CO", "\"example.org\"", ")", "AND", "userType", "EQ", "\"Employee\""};
  
  protected static final String NOT_EXAMPLE_1 = "not(userType eq \"Employee\")";
  protected static final String NOT_EXAMPLE_1_1 = "not userType eq \"Employee\"";
  protected static final String NOT_EXAMPLE_2 = "userType eq \"Employee\" and not(emails co \"example.com\" or emails co \"example.org\")";
  protected static final String NOT_EXAMPLE_3 = "not(userType eq \"Employee\") and not(userType eq \"Intern\")";
  protected static final String NOT_EXAMPLE_3_1 = "not userType eq \"Employee\" and not userType eq \"Intern\"";
  protected static final String NOT_EXAMPLE_4 = "not(userType eq \"Employee\" or userType eq \"Intern\")";

  protected static final String[] NOT_EXAMPLE_1_INFIX_TOKENS = {"NOT(", "userType", "EQ", "\"Employee\"", ")"};
  protected static final String[] NOT_EXAMPLE_1_1_INFIX_TOKENS = {"not", "userType", "EQ", "\"Employee\""};
  protected static final String[] NOT_EXAMPLE_2_INFIX_TOKENS = {"userType", "EQ", "\"Employee\"", "AND", "NOT(", "emails", "CO", "\"example.com\"", "OR", "emails", "CO", "\"example.org\"", ")"};
  protected static final String[] NOT_EXAMPLE_3_INFIX_TOKENS = {"NOT(", "userType", "EQ", "\"Employee\"", ")", "AND", "NOT(", "userType", "EQ", "\"Intern\"", ")"};
  protected static final String[] NOT_EXAMPLE_3_1_INFIX_TOKENS = {"not", "userType", "EQ", "\"Employee\"", "AND", "not", "userType", "EQ", "\"Intern\""};
  protected static final String[] NOT_EXAMPLE_4_INFIX_TOKENS = {"NOT(", "userType", "EQ", "\"Employee\"", "OR", "userType", "EQ", "\"Intern\"", ")"};

  protected static final String[] EXAMPLES = {
    EXAMPLE_1,
    EXAMPLE_2,
    EXAMPLE_3,
    EXAMPLE_4,
    EXAMPLE_5,
    EXAMPLE_6,
    EXAMPLE_7,
    EXAMPLE_8,
    EXAMPLE_9,
    EXAMPLE_10,
    EXAMPLE_11
  };
  
  protected static final String[] EXTRAS = {
    EXTRA_1,
    EXTRA_2
  };
  
  protected static final String[] EXAMPLES_AND_EXTRAS = ArrayUtils.addAll(EXAMPLES, EXTRAS);
  
  protected static final String[] NOTS = {
	  NOT_EXAMPLE_1,
	  NOT_EXAMPLE_1_1,
	  NOT_EXAMPLE_2,
	  NOT_EXAMPLE_3,
	  NOT_EXAMPLE_3_1,
	  NOT_EXAMPLE_4
  };
  
  protected static final String[][] EXAMPLE_INFIX_TOKENS = {
    EXAMPLE_1_INFIX_TOKENS,
    EXAMPLE_2_INFIX_TOKENS,
    EXAMPLE_3_INFIX_TOKENS,
    EXAMPLE_4_INFIX_TOKENS,
    EXAMPLE_5_INFIX_TOKENS,
    EXAMPLE_6_INFIX_TOKENS,
    EXAMPLE_7_INFIX_TOKENS,
    EXAMPLE_8_INFIX_TOKENS,
    EXAMPLE_9_INFIX_TOKENS,
    EXAMPLE_10_INFIX_TOKENS,
    EXAMPLE_11_INFIX_TOKENS
  };
  
  protected static final String[][] EXTRA_INFIX_TOKENS = {
    EXTRA_1_INFIX_TOKENS,
    EXTRA_2_INFIX_TOKENS
  };
  
  protected static final String[][] EXAMPLE_AND_EXTRA_INFIX_TOKENS = ArrayUtils.addAll(EXAMPLE_INFIX_TOKENS, EXTRA_INFIX_TOKENS);
  
  protected static final String[][] NOT_INFIX_TOKENS = {
	  NOT_EXAMPLE_1_INFIX_TOKENS,
	  NOT_EXAMPLE_1_1_INFIX_TOKENS,
	  NOT_EXAMPLE_2_INFIX_TOKENS,
	  NOT_EXAMPLE_3_INFIX_TOKENS,
	  NOT_EXAMPLE_3_1_INFIX_TOKENS,
	  NOT_EXAMPLE_4_INFIX_TOKENS
  };
  
  protected static final String[] INPUT_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS = {
    "streetAddress EQ \"111 Heritage Way\"",
    "streetAddress EQ \"111 Heritage Way\nSuite S\"",
    "streetAddress EQ \"111 Heritage Way\rSuite S\""
  };
  
  protected static final String[][] EXPECTED_ATTRIBUTE_VALUES_WITH_SPACES_NEWLINES_AND_CARRIAGE_RETURNS = {
    {"streetAddress", "EQ", "\"111 Heritage Way\""},
    {"streetAddress", "EQ", "\"111 Heritage Way\nSuite S\""},
    {"streetAddress", "EQ", "\"111 Heritage Way\rSuite S\""}
  };
  
  /**
   * 
   */
  public AbstractLexerParserTest() {
    // TODO Auto-generated constructor stub
  }

}
