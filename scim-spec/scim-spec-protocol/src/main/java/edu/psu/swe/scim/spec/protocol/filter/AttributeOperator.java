package edu.psu.swe.scim.spec.protocol.filter;

public enum AttributeOperator implements Operator {
  
  EQ,
  NE,
  CO,
  SW,
  EW,
  PR(true),
  GT,
  GE,
  LT,
  LE;
  
  private boolean oneOperand;
  
  private AttributeOperator() {
    this(false);
  }
  
  private AttributeOperator(boolean oneOperand) {
    this.oneOperand = oneOperand;
  }

  @Override
  public boolean isAttributeOperator() {
    return true;
  }
  
  public static boolean isAttributeOperator(String value) {
    boolean attributeOperator = false;
    try {
      AttributeOperator.valueOf(value.toUpperCase());
      attributeOperator = true;
    } catch(IllegalArgumentException e) {
      // attributeOperator remains false if this exception is thrown
    }
    return attributeOperator;
  }

  @Override
  public boolean isGroupingOperator() {
    return false;
  }

  @Override
  public boolean isLogicalOperator() {
    return false;
  }
  
  public boolean isOneOperand() {
    return oneOperand;
  }
  
}
