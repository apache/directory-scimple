package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class UnableToResolveIdException extends Exception {

  private static final long serialVersionUID = -7401709416973728017L;

  private Status status;
  
  public UnableToResolveIdException(Status status, String what) {
    super(what);
    
    this.status = status;
  }
  
  public UnableToResolveIdException(Status status, String what, Throwable why) {
    super(what, why);
    
    this.status = status;
  }

}
