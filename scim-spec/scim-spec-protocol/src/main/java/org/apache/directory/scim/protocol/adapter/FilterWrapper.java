/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.protocol.adapter;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.directory.scim.spec.filter.Filter;


final public class FilterWrapper {
    /** A logger for this class */
    private static final Logger log = LoggerFactory.getLogger(FilterWrapper.class);

  final public Filter filter;
  
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

  public Filter getFilter() {
    return this.filter;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof FilterWrapper)) return false;
    final FilterWrapper other = (FilterWrapper) o;
    final Object this$filter = this.getFilter();
    final Object other$filter = other.getFilter();
    if (this$filter == null ? other$filter != null : !this$filter.equals(other$filter)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $filter = this.getFilter();
    result = result * PRIME + ($filter == null ? 43 : $filter.hashCode());
    return result;
  }

  public String toString() {
    return "FilterWrapper(filter=" + this.getFilter() + ")";
  }
}
