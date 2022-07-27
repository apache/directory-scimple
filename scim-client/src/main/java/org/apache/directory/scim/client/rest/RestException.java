/*
 * The Pennsylvania State University © 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.client.rest;

/*
 * The Pennsylvania State University © 2016
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;

public class RestException extends Exception {
  private static final long serialVersionUID = 7360783673606191577L;

  private final int statusCode;
  private final Response.Status status;
  private ErrorResponse errorResponse;

  public RestException(Response response) {
    statusCode = response.getStatus();
    status = response.getStatusInfo().toEnum();
    try {
      errorResponse = response.readEntity(ErrorResponse.class);
    } catch (ProcessingException e) {
      errorResponse = null;
    }
  }

  public RestException(int statusCode, ErrorResponse errorResponse) {
    this.statusCode = statusCode;
    this.status = Response.Status.fromStatusCode(statusCode);
    this.errorResponse = errorResponse;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Response.Status getStatus() {
    return status;
  }

  public ErrorResponse getError() {
    return errorResponse;
  }

  @Override
  public String getMessage() {
    String message = "Rest Client Exception: Status Code: " + statusCode + " ";
    if (errorResponse != null) {
      message += "Error Message: " + errorResponse.getDetail();
    }

    return message;

  }

}
