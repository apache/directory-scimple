package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;

@Value
public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;
  
  @Override
  public String toFilter() {
    boolean leftParens = left instanceof LogicalExpression;
    boolean rightParens = right instanceof LogicalExpression;

    String leftString = (leftParens ? "(" : "") + left.toFilter() + (leftParens ? ")" : "");
    String rightString = (rightParens ? "(" : "") + right.toFilter() + (rightParens ? ")" : "");
    
    return leftString + " " + operator + " " + rightString;
  }
}
