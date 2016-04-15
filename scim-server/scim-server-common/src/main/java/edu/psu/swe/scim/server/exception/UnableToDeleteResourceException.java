package edu.psu.swe.scim.server.exception;

public class UnableToDeleteResourceException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToDeleteResourceException(String what) {
    super(what);
  }
  
  public UnableToDeleteResourceException(String what, Throwable why) {
    super(what, why);
  }
}
