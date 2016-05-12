package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;

@Value
public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;
}
