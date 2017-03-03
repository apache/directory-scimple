package edu.psu.swe.scim.spec.adapter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FilterWrapper {

  public Filter filter;
  
  public FilterWrapper(String string) {
    
    try {
      filter = new Filter(string);
    } catch (FilterParseException e) {
      log.error("Invalid Filter: {}", string);
      ErrorResponse er = new ErrorResponse(Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER.getDetail());
      er.setScimType(ErrorMessageType.INVALID_FILTER);
      Response response = er.toResponse();
      throw new WebApplicationException(e, response);
    }
  }

  public FilterWrapper(Filter filter) {
    this.filter = filter;
  }
}
