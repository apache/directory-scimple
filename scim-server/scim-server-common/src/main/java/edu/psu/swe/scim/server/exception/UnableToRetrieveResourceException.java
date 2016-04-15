package edu.psu.swe.scim.server.exception;

public class UnableToRetrieveResourceException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToRetrieveResourceException(String what) {
    super(what);
  }
  
  public UnableToRetrieveResourceException(String what, Throwable why) {
    super(what, why);
  }
}
