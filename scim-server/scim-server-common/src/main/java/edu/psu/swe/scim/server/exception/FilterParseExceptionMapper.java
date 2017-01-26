package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;

public class FilterParseExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    ErrorResponse er = new ErrorResponse();
    er.setStatus("400");
    er.setScimType(ErrorMessageType.INVALID_FILTER);
    er.setDetail(exception.getLocalizedMessage());
    return Response.status(Status.BAD_REQUEST).entity(er).build();
  }

}
