package edu.psu.swe.scim.client.filter.exception;

public class InvalidValueFilterExpression extends Exception {

  private static final long serialVersionUID = -5165945768240326983L;

  public InvalidValueFilterExpression(String what) {
    super(what);
  }
}
