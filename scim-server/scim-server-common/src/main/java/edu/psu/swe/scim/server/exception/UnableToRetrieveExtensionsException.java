package edu.psu.swe.scim.server.exception;

public class UnableToRetrieveExtensionsException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToRetrieveExtensionsException(String what) {
    super(what);
  }
  
  public UnableToRetrieveExtensionsException(String what, Throwable why) {
    super(what, why);
  }
}
