package edu.psu.swe.scim.spec.protocol.filter;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValuePathExpression implements FilterExpression {
  AttributeReference attributePath;
  ValueFilterExpression valueFilter;
  
  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + "[" + valueFilter.toFilter() + "]";
  }
}
