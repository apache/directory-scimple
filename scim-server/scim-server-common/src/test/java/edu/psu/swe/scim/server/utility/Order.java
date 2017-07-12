package edu.psu.swe.scim.server.utility;

public enum Order {
  
  FIRST("first"),
  SECOND("second"),
  THIRD("third"),
  FOURTH("fourth");
  
  Order(String value) {
    this.value = value;
  }
  
  private final String value;
  
  public String getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return value;
  }

}
