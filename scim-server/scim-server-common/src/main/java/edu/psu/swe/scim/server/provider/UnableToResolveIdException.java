package edu.psu.swe.scim.server.provider;

public class UnableToResolveIdException extends Exception {

  private static final long serialVersionUID = -7401709416973728017L;

  public UnableToResolveIdException() {
  }

  public UnableToResolveIdException(String message) {
    super(message);
  }

  public UnableToResolveIdException(Throwable cause) {
    super(cause);
  }

  public UnableToResolveIdException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnableToResolveIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
