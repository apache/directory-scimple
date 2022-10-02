package org.apache.directory.scim.server.exception;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.data.ErrorResponse;

@Slf4j
abstract class BaseScimExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

  protected abstract ErrorResponse errorResponse(E throwable);

  @Override
  public Response toResponse(E throwable) {
    Response response = errorResponse(throwable).toResponse();
    log.warn("Returning error status: {}", response.getStatus(), throwable);
    response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, Constants.SCIM_CONTENT_TYPE);
    return response;
  }
}
