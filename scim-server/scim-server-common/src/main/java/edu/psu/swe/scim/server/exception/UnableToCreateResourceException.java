package edu.psu.swe.scim.server.exception;

public class UnableToCreateResourceException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToCreateResourceException(String what) {
    super(what);
  }
  
  public UnableToCreateResourceException(String what, Throwable why) {
    super(what, why);
  }
}
