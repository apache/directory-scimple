/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stevemoyer
 *
 */
public class FilterParser {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterParser.class);
  
  private FilterLexer lexer_;

  /**
   * 
   */
  public FilterParser(FilterLexer lexer) {
    lexer_ = lexer;
  }
  
  public Expression<?> parse() throws FilterParseException {
    LOGGER.debug("Filter string: " + lexer_.getFilter());
    //Expression<?> expression = recursiveParse(null);
    Expression<?> expression = leftToRightParse(null);
    logExpression("Filter expression", expression);
    return expression;
  }
  
  public Expression<?> parse(String filterString) throws FilterParseException {
    lexer_ = new FilterLexer(filterString);
    return parse();
  }
  
  public Expression<?> parse(FilterLexer filterLexer) throws FilterParseException {
    lexer_ = filterLexer;
    return parse();
  }
  
  private Expression<?> leftToRightParse(Expression<?> expressionIn) throws FilterParseException {
    Expression<?> expressionOut = expressionIn;
    while(lexer_.hasNext()) {
    	expressionOut = recursiveParse(expressionOut);
    }
    return expressionOut;
  }
  
  private Expression<?> recursiveParse(Expression<?> expressionIn) throws FilterParseException {
    Expression<?> expressionOut = expressionIn;
    logExpression("Expression in", expressionIn);
    if(lexer_.hasNext()) {
      String token = lexer_.next();
      LOGGER.debug("Current token: " + token);
      if(isAttributeName(token)) {
        SimpleExpression attributeExpression = parseAttributeExpression(token);
        expressionOut = recursiveParse(attributeExpression);
        //expressionOut = attributeExpression;
      } else if(LogicalOperator.isLogicalOperator(token)) {
        CompoundExpression logicalExpression = parseCompoundExpression(expressionIn, token);
        expressionOut = recursiveParse(logicalExpression);
        //expressionOut = logicalExpression;
      } else if(GroupingOperator.isGroupingOperator(token)) {
        GroupingOperator groupingOperator = GroupingOperator.fromSymbol(token);
        if(groupingOperator == GroupingOperator.OP) {
          expressionOut = parseGroupedExpression(false);
        } else if(groupingOperator == GroupingOperator.NP) {
          expressionOut = parseGroupedExpression(true);
        } else if(groupingOperator == GroupingOperator.CP) {
//          expressionOut = expressionIn;
          return expressionIn;
        }
      }
      return recursiveParse(expressionOut);
      //expressionOut = recursiveParse(expressionOut);
    }
//    logExpression("Expression out:", expressionOut);
//    return expressionOut;
    return expressionIn;
  }
  
  private String getAttributeValue(String attributeValue) {
    return StringUtils.strip(attributeValue, "\"");
  }
  
  private boolean isAttributeName(String token) {
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("isAttributeName() - starts with \" : " + token.startsWith("\""));
      LOGGER.debug("isAttributeName() - ends with \" : " + token.endsWith("\""));
      LOGGER.debug("isAttributeName() - is operator: " + isOperator(token));
    }
    return (!token.startsWith("\"") || !token.endsWith("\"")) && !isOperator(token);
  }
  
  private boolean isOperator(String token) {
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("isOperator() - isAttributeOperator: " + AttributeOperator.isAttributeOperator(token));
      LOGGER.debug("isOperator() - isGroupingOperator: " + GroupingOperator.isGroupingOperator(token));
      LOGGER.debug("isOperator() - isLogicalOperator: " + LogicalOperator.isLogicalOperator(token));
    }
    return AttributeOperator.isAttributeOperator(token) ||
           GroupingOperator.isGroupingOperator(token) ||
           LogicalOperator.isLogicalOperator(token);
  }
  
  protected void logExpression(Expression<?> expression) {
    logExpression("Expression", expression);
  }
  
  protected void logExpression(String title, Expression<?> expression) {
    String expressionString = "<null>";
    if(expression != null) {
      expressionString = expression.toString("");
    }
    LOGGER.debug(title + ":\n" + expressionString);
  }
  
  private SimpleExpression parseAttributeExpression(String attributeName) throws FilterParseException {
    LOGGER.debug("Parsing attribute expression for: " + attributeName);
    // Create a simple expression with the left value set to the attribute name
    SimpleExpression attributeExpression = new SimpleExpression();
    attributeExpression.setLeft(attributeName);
    
    // If the next token is an operator, add it to the simple expression
    if(!lexer_.hasNext()) {
      throw new FilterParseException("An attribute name was found without an operator: " + attributeName);
    }
    String operatorString = lexer_.next();
    if(!AttributeOperator.isAttributeOperator(operatorString)) {
      throw new FilterParseException("An operator was expected at this position: " + operatorString);
    }
    AttributeOperator operator = AttributeOperator.valueOf(operatorString);
    attributeExpression.setOperator(operator);
    
    // If the operator requires an attribute value, add it to the simple expression
    if(!operator.isOneOperand()) {
      if(!lexer_.hasNext()) {
        throw new FilterParseException("An operator was found without an attribute value: " + operator.name());
      }
      attributeExpression.setRight(getAttributeValue(lexer_.next()));
    }
    
    // If we actually make it here without an exception, return the result!
    if(LOGGER.isDebugEnabled()) {
      logExpression("Attribute expression", attributeExpression);
    }
    return attributeExpression;
  }
  
  private Expression<?> parseGroupedExpression(boolean negativeGroup) throws FilterParseException {
    Expression<?> expressionOut;
    //Expression<?> groupedExpression = recursiveParse(null);
    Expression<?> groupedExpression = leftToRightParse(null);
    logExpression("Grouped expression", groupedExpression);
    if(negativeGroup) {
      LOGGER.debug("NP");
      CompoundExpression notExpression = new CompoundExpression();
      notExpression.setOperator(GroupingOperator.NP);
      notExpression.setRight(groupedExpression);
      expressionOut = notExpression;
    } else {
      LOGGER.debug("OP");
      expressionOut = groupedExpression;
    }
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("***** Group start *****");
      logExpression("Grouped expression", expressionOut);
      LOGGER.debug("***** Group end *****");
    }   
    logExpression("Grouped expression", expressionOut);
    return expressionOut;
  }
  
  private CompoundExpression parseCompoundExpression(Expression<?> leftExpression, String operatorString) throws FilterParseException {
    CompoundExpression compoundExpression = new CompoundExpression();
    LogicalOperator operator = LogicalOperator.valueOf(operatorString);
    LOGGER.debug(operator.name());
    compoundExpression.setOperator(operator);
    compoundExpression.setLeft(leftExpression);
    
    //Expression<?> rightExpression = recursiveParse(null);
    Expression<?> rightExpression = leftToRightParse(null);
    compoundExpression.setRight(rightExpression);
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("***** LogicalOperator start *****");
      LOGGER.debug("LogicalOperator: " + operator.name());
      logExpression("LeftExpression", leftExpression);
      logExpression("Right expression", rightExpression);
      logExpression("Logical expression", compoundExpression);
      LOGGER.debug("***** LogicalOperator end *****");
    }
    return compoundExpression;
  }

}
