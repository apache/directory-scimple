package edu.psu.swe.scim.spec.protocol.filter;

public interface AttributeExpression extends FilterExpression {

  void setAttributePath(String urn, String parentAttributeName);

  String toUnqualifiedFilter();
}
