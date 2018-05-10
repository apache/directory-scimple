package edu.psu.swe.scim.spec.protocol.filter;

public interface FilterExpression {
  
  public String toFilter();

  void setAttributePath(String urn, String parentAttributeName);

  String toUnqualifiedFilter();
}
