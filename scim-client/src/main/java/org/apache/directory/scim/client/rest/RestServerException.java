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

import jakarta.ws.rs.core.Response;

public class RestServerException extends RuntimeException {
  private static final long serialVersionUID = 7360783673606191577L;
  
  RestClientException rce;

  public RestServerException(Response response) {
    rce = new RestClientException(response);
  }

  public RestServerException(int statusCode, ErrorMessage errorMessage) {
    rce = new RestClientException(statusCode, errorMessage);
  }

  public int getStatusCode() {
    return rce.getStatusCode();
  }

  public ErrorMessage getErrorMessage() {
    return rce.getErrorMessage();
  }

  @Override
  public String getMessage() {
    return rce.getMessage();
  }
}
