package edu.psu.swe.scim.spec.protocol.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttributeLogicExpression implements FilterExpression, AttributeExpression {

  AttributeExpression left;
  LogicalOperator logicalOperator;
  AttributeExpression right;

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    left.setAttributePath(urn, parentAttributeName);
    right.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toFilter() {
    return left.toFilter() + " " + logicalOperator.name() + " " + right.toFilter();
  }

  @Override
  public String toUnqualifiedFilter() {
    return left.toUnqualifiedFilter() + " " + logicalOperator.name() + " " + right.toUnqualifiedFilter();
  }
}
