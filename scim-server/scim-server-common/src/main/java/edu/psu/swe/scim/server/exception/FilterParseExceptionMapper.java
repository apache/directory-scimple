package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;

public class FilterParseExceptionMapper implements ExceptionMapper<FilterParseException> {

  @Override
  public Response toResponse(FilterParseException exception) {
    ErrorResponse er = new ErrorResponse(Status.BAD_REQUEST, exception.getMessage());
    er.setScimType(ErrorMessageType.INVALID_FILTER);
    return er.toResponse();
  }

}
