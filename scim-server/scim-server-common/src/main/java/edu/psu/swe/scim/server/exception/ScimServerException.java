package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import lombok.Getter;

public class ScimServerException extends Exception {

  private static final long serialVersionUID = -3803568677019909403L;

  @Getter
  private final ErrorResponse errorResponse;

  public ScimServerException(Status status, String detail) {
    super(formatMessage(status, null, detail));
    this.errorResponse = new ErrorResponse(status, detail);
  }

  public ScimServerException(Status status, String detail, Exception e) {
    super(formatMessage(status, null, detail), e);
    this.errorResponse = new ErrorResponse(status, detail);
  }

  public ScimServerException(Status status, ErrorMessageType errorMessageType, String detail) {
    super(formatMessage(status, errorMessageType, detail));
    this.errorResponse = new ErrorResponse(status, detail);
    this.errorResponse.setScimType(errorMessageType);
  }
  
  public ScimServerException(Status status, ErrorMessageType errorMessageType, String detail, Exception e) {
    super(formatMessage(status, errorMessageType, detail), e);
    this.errorResponse = new ErrorResponse(status, detail);
    this.errorResponse.setScimType(errorMessageType);
  }

  private static String formatMessage(Status status, ErrorMessageType errorMessageType, String detail) {
    return "Scim Error: " + status + (errorMessageType != null ? " (" + errorMessageType + ")" : "") + ", " + detail;
  }

}
