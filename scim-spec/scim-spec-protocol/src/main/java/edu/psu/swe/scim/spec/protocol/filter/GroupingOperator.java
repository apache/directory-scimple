package edu.psu.swe.scim.spec.protocol.filter;

import java.util.HashMap;
import java.util.Map;

public enum GroupingOperator implements Operator {
  
  OP("("),
  CP(")"),
  NP("NOT(");
  
  private static Map<String, GroupingOperator> symbolMap;
  
  static {
    symbolMap = new HashMap<String, GroupingOperator>();
    for(GroupingOperator groupingOperator: GroupingOperator.values()) {
      symbolMap.put(groupingOperator.name(), groupingOperator);
      symbolMap.put(groupingOperator.getSymbol(), groupingOperator);
    }
  }

  private String symbol_;
  
  private GroupingOperator(String symbol) {
    symbol_ = symbol;
  }
  
  public String getSymbol() {
    return symbol_;
  }
  
  @Override
  public boolean isAttributeOperator() {
    return false;
  }

  @Override
  public boolean isGroupingOperator() {
    return true;
  }
  
  public static boolean isGroupingOperator(String value) {
    return symbolMap.containsKey(value.toUpperCase());
  }

  @Override
  public boolean isLogicalOperator() {
    return false;
  }
  
  
  public static GroupingOperator fromSymbol (String value) {
    if(!GroupingOperator.isGroupingOperator(value)) {
      throw new IllegalArgumentException("Not an attribute operator: " + value);
    }
    return symbolMap.get(value);
  }

}
