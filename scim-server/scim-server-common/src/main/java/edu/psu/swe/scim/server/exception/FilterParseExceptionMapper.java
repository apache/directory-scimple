package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.schema.ErrorResponse;

public class FilterParseExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("400");
    er.setDetail(exception.getLocalizedMessage());
    return Response.status(Status.BAD_REQUEST).entity(er).build();
  }

}
