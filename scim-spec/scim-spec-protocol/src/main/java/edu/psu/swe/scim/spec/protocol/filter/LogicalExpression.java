package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Data;

@Data
public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;
}
