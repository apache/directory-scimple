package edu.psu.swe.scim.spec.protocol.filter2;

import lombok.Data;

@Data
public class GroupExpression implements FilterExpression, ValueFilterExpression {

  boolean negative = false;
  FilterExpression filterExpression;
}
