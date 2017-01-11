package edu.psu.swe.scim.spec.exception;

public class InvalidScimResourceException extends RuntimeException {
  
  private static final long serialVersionUID = -3378968149599082798L;

  public InvalidScimResourceException(String what) {
    super(what);
  }
}
