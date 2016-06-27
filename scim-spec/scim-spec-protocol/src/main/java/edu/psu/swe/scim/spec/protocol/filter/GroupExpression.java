package edu.psu.swe.scim.spec.protocol.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupExpression implements FilterExpression, ValueFilterExpression {

  boolean not;
  FilterExpression filterExpression;
  
  @Override
  public String toFilter() {
    return (not ? "NOT" : "") + "(" + filterExpression.toFilter() + ")";
  }
}
