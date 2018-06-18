package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class AttributePresentExpression implements FilterExpression, ValueFilterExpression {
  AttributeReference attributePath;

  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + " PR";
  }

  @Override
  public String toUnqualifiedFilter() {
    String subAttributeName = this.attributePath.getSubAttributeName();
    String attributeName = subAttributeName != null ? subAttributeName : this.attributePath.getAttributeName();

    return attributeName + " PR";
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    String subAttributeName = this.attributePath.getAttributeName();
    this.attributePath.setAttributeName(parentAttributeName);
    this.attributePath.setSubAttributeName(subAttributeName);
  }
}
