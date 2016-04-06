package edu.psu.swe.scim.server.exception;

public class AttributeDoesNotExistException extends Exception {

  private static final long serialVersionUID = 547510233114396694L;

  public AttributeDoesNotExistException() {
  }

  public AttributeDoesNotExistException(String message) {
    super(message);
  }

  public AttributeDoesNotExistException(Throwable cause) {
    super(cause);
  }

  public AttributeDoesNotExistException(String message, Throwable cause) {
    super(message, cause);
  }

  public AttributeDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
