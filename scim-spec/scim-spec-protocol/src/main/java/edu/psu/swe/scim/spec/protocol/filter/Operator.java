package edu.psu.swe.scim.spec.protocol.filter;

public interface Operator {

  boolean isAttributeOperator();
  boolean isGroupingOperator();
  boolean isLogicalOperator();
  String name();
  
}
