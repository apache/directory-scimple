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
package org.apache.directory.scim.ws.common.exception;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import org.apache.directory.scim.ws.common.ErrorMessage;

public class BackingStoreChangedException extends Exception {
  private static final long serialVersionUID = 7360783673606191576L;

  private int statusCode;
  private ErrorMessage errorMessage;

  public BackingStoreChangedException(Response response) {
    statusCode = response.getStatus();
    try {
      errorMessage = response.readEntity(ErrorMessage.class);
    } catch (ProcessingException e) {
      errorMessage = null;
    }
  }

  public BackingStoreChangedException(int statusCode, ErrorMessage errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public ErrorMessage getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String getMessage() {
    String message = "Rest Client Exception: Status Code: " + statusCode + " ";
    if (errorMessage != null) {
      message += "Error Messages: " + errorMessage.getErrorMessageList();
    }

    return message;

  }

}
