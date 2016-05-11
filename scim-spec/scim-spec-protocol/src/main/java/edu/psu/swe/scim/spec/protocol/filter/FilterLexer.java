/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an iterator that returns the filter's tokens in their original order
 * for consumption by a parser.  This process cleans up the input String, but
 * maintains the quoting around the attribute values so they can be later
 * distinguished from attribute names.
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
public class FilterLexer implements Iterator<String> {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterLexer.class);
  
  private static final String EXCEPTION_REMOVE_NOT_SUPPORTED = "The FilterLexer does not support remove as next destroys the String it returns";
  
  private String filter_;
  
  private LinkedList<String> tokens_ = new LinkedList<String>();

  /**
   * 
   */
  public FilterLexer(String filter) {
    filter_ = filter;
    LOGGER.debug("Tokenizing filter string:" + filter);
    if(!filter.isEmpty()) {
      String cleanString = filter;
      
      // Make sure there are spaces around parenthesis
      cleanString = cleanString.replaceAll("\\(", " ( ");
      cleanString = cleanString.replaceAll("\\)", " ) ");
      LOGGER.debug("Padded parenthesis with spaces (maybe extra): " + cleanString);
      
      // Make sure there are spaces around brackets
      cleanString = cleanString.replaceAll("\\[", " [ ");
      cleanString = cleanString.replaceAll("\\]", " ] ");
      LOGGER.debug("Padded brackets with spaces (maybe extra): " + cleanString);
      
      // Convert multiple consecutive spaces to single spaces
      cleanString = cleanString.replaceAll(" +", " ");
      LOGGER.debug("Padded parenthesis with single spaces: " + cleanString);
      
      // Eliminate the space between NOT and ( and capitalize NOT
      cleanString = cleanString.replaceAll("(?i)not *\\(", "NOT(");
      
      // Trim leading and trailing spaces
      cleanString = cleanString.trim();
      LOGGER.debug("Cleaned leading and trailing spaces: " + cleanString);
      
      // Convert the string to tokens
      String[] tokens = cleanString.split(" ");
      
      // Convert operators to uppercase
      for(int i = 0; i < tokens.length; i++) {
        String token = tokens[i];
        if(AttributeOperator.isAttributeOperator(token) || LogicalOperator.isLogicalOperator(token)) {
          tokens[i] = token.toUpperCase();
        }
      }
            
      tokens_ = new LinkedList<String>(Arrays.asList(tokens));
      
      // Reconcatenate value attributes with spaces (so that they both start
      // and end with double-quotes
      //
      // DANGER - This loop relies on the size of the token list being changed
      //          inside the loop.  Don't optimize it by looking up the list
      //          size once before the loop starts.
      for(int i = 0; i < tokens_.size(); i++) {
        if(tokens_.get(i).startsWith("\"")) {
          while(!tokens_.get(i).endsWith("\"")) {
            tokens_.set(i, tokens_.get(i) + " " + tokens_.get(i + 1));
            tokens_.remove(i + 1);
          }
        }
      }
    }
    
    if(LOGGER.isDebugEnabled()) {
      logTokens(tokens_);
    }
  }
  
  public String getFilter() {
    return filter_;
  }
  
  protected List<String> getTokens() {
    return tokens_;
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return !tokens_.isEmpty();
  }

  protected void logTokens(List<String> tokens) {
    LOGGER.debug("Tokens: ");
    for(String token: tokens) {
      LOGGER.debug("    " + token);
    }    
  }
  
  /* (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public String next() {
    return tokens_.removeFirst();
  }

  /* (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException(EXCEPTION_REMOVE_NOT_SUPPORTED);
  }
  
  protected void setTokens(List<String> tokens) {
    tokens_ = new LinkedList<String>(tokens);
  }

}
