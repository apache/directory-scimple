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

package edu.psu.swe.scim.server.exception;

import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.protocol.data.ErrorResponse;
import lombok.Getter;

public class ScimServerException extends Exception {

  private static final long serialVersionUID = -3803568677019909403L;

  @Getter
  private final ErrorResponse errorResponse;

  public ScimServerException(Status status, String detail) {
    super(formatMessage(status, null, detail));
    this.errorResponse = new ErrorResponse(status, detail);
  }

  public ScimServerException(Status status, String detail, Exception e) {
    super(formatMessage(status, null, detail), e);
    this.errorResponse = new ErrorResponse(status, detail);
  }

  public ScimServerException(Status status, ErrorMessageType errorMessageType, String detail) {
    super(formatMessage(status, errorMessageType, detail));
    this.errorResponse = new ErrorResponse(status, detail);
    this.errorResponse.setScimType(errorMessageType);
  }
  
  public ScimServerException(Status status, ErrorMessageType errorMessageType, String detail, Exception e) {
    super(formatMessage(status, errorMessageType, detail), e);
    this.errorResponse = new ErrorResponse(status, detail);
    this.errorResponse.setScimType(errorMessageType);
  }

  private static String formatMessage(Status status, ErrorMessageType errorMessageType, String detail) {
    return "Scim Error: " + status + (errorMessageType != null ? " (" + errorMessageType + ")" : "") + ", " + detail;
  }

}
