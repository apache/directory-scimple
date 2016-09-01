package edu.psu.swe.scim.spec.exception;

import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.schema.ErrorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ScimException extends Exception {

  private static final long serialVersionUID = 3643485564325176463L;
  private ErrorResponse error;
  private Status status;

  public ScimException(ErrorResponse error, Status status) {
    this.error = error;
    this.status = status;
  }
}
