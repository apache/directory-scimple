/**
 * 
 */
package edu.psu.swe.scim.spec.protocol.filter;

/**
 * @author stevemoyer
 *
 */
public class FilterParseException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public FilterParseException() {
  }

  /**
   * @param message
   */
  public FilterParseException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public FilterParseException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public FilterParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
