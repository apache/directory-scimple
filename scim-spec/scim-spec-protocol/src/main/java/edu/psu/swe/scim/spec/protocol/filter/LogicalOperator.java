package edu.psu.swe.scim.spec.protocol.filter;

public enum LogicalOperator implements Operator {
  
  AND,
  OR;

  @Override
  public boolean isAttributeOperator() {
    return false;
  }

  @Override
  public boolean isGroupingOperator() {
    return false;
  }

  @Override
  public boolean isLogicalOperator() {
    return true;
  }

  public static boolean isLogicalOperator(String value) {
    boolean logicalOperator = false;
    try {
      LogicalOperator.valueOf(value.toUpperCase());
      logicalOperator = true;
    } catch(IllegalArgumentException e) {
      // logicalOperator remains false if this exception is thrown
    }
    return logicalOperator;
  }
  
}
