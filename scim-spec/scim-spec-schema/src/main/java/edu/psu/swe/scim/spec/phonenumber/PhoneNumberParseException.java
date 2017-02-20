/**
 * 
 */
package edu.psu.swe.scim.spec.phonenumber;

/**
 * @author heidielliott
 *
 */
public class PhoneNumberParseException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public PhoneNumberParseException() {
  }

  /**
   * @param message
   */
  public PhoneNumberParseException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public PhoneNumberParseException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public PhoneNumberParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
