package edu.psu.swe.scim.spec.protocol.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;
}
