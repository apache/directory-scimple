package edu.psu.swe.scim.spec.protocol.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttributeGroupExpression implements ValueFilterExpression, AttributeExpression {

  boolean not;
  AttributeExpression attributeExpression;

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributeExpression.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toFilter() {
    return (not ? "NOT(" : "(") + attributeExpression.toFilter() + ")";
  }

  @Override
  public String toUnqualifiedFilter() {
    return (not ? "NOT(" : "(" + attributeExpression.toUnqualifiedFilter() + ")");
  }
}
