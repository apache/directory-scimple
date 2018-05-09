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

  @Override
  public String toUnqualifiedFilter() {
    return attributePath.getAttributeName() + " PR";
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    this.attributePath.setParent(parentAttributeName);
  }
}
