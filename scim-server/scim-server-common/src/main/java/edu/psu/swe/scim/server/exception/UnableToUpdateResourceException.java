package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class UnableToUpdateResourceException extends Exception {

  private static final long serialVersionUID = -3872700870424005641L;

  private Status status;
  
  public UnableToUpdateResourceException(Status status, String what) {
    super(what);
    
    this.status = status;
  }
  
  public UnableToUpdateResourceException(Status status, String what, Throwable why) {
    super(what, why);
    
    this.status = status;
  }
}
