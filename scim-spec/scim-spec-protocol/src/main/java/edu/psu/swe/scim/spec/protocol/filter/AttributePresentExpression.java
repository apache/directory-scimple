package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class AttributePresentExpression implements AttributeExpression, ValueFilterExpression {
  AttributeReference attributePath;

  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + " PR";
  }
}
