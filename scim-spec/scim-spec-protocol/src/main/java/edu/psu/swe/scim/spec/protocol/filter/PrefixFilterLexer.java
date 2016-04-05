/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stevemoyer
 *
 */
public class PrefixFilterLexer extends FilterLexer {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(PrefixFilterLexer.class);

  /**
   * @param filter
   */
  public PrefixFilterLexer(String filter) {
    super(filter);
    setTokens(convertFromInfixToPrefix(getTokens()));
    // TODO Auto-generated constructor stub
  }

  protected List<String> convertFromInfixToPrefix(List<String> tokens) {
    LOGGER.debug("Converting to prefix form");
    logTokens(tokens);
    // Loop through tokens to find attribute operators
    for(int i = 0; i < tokens.size(); i++) {
      String token = tokens.get(i);
      AttributeOperator operator = null;
      try {
        operator = AttributeOperator.valueOf(token.toUpperCase());
        LOGGER.debug("    Token: " + token + ", AttributeOperator: true");
        LOGGER.debug("    AttributeOperator is one-operand: " + operator.isOneOperand());
        if(i > 0) {
          tokens.set(i, tokens.get(i - 1));
          tokens.set(i - 1, operator.name());
        }
      } catch(IllegalArgumentException e) {
        LOGGER.debug("    Token: " + token + ", AttributeOperator: false");
      }
    }
    logTokens(tokens);
    return tokens;
  }
  
}
