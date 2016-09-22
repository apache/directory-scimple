package edu.psu.swe.scim.server.provider.extensions.exceptions;

import javax.ws.rs.core.Response.Status;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ClientFilterException extends Exception {
  
  private static final long serialVersionUID = 3308947684934769952L;
  
  Status status;
  
  public ClientFilterException(Status status, String message) {
    super(message);
    this.status = status;
  }

}
