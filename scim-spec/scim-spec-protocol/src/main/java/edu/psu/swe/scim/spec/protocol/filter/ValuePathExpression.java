package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class ValuePathExpression implements FilterExpression {
  AttributeReference attributePath;
  ValueFilterExpression valueFilter;
  
  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + "[" + valueFilter.toFilter() + "]";
  }
}
