package edu.psu.swe.scim.server.exception;

public class UnableToUpdateResourceException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToUpdateResourceException(String what) {
    super(what);
  }
  
  public UnableToUpdateResourceException(String what, Throwable why) {
    super(what, why);
  }
}
