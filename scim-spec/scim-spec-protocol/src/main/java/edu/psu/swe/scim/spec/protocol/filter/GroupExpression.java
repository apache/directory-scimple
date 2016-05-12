package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;

@Value
public class GroupExpression implements FilterExpression, ValueFilterExpression {

  boolean not;
  FilterExpression filterExpression;
}
