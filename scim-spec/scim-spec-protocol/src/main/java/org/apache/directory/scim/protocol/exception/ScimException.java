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

package org.apache.directory.scim.protocol.exception;

import jakarta.ws.rs.core.Response.Status;
import org.apache.directory.scim.protocol.data.ErrorResponse;

import java.io.Serial;

public class ScimException extends Exception {

  @Serial
  private static final long serialVersionUID = 3643485564325176463L;
  private final ErrorResponse error;
  private final Status status;

  public ScimException(Status status, String message, Throwable cause) {
    super(message, cause);
    this.error = new ErrorResponse(status, message);
    this.status = status;
  }

  public ScimException(Status status, String message) {
    this(new ErrorResponse(status, message), status);
  }

  public ScimException(ErrorResponse error, Status status) {
    this.error = error;
    this.status = status;
  }

  public ErrorResponse getError() {
    return this.error;
  }

  public Status getStatus() {
    return this.status;
  }

  public String toString() {
    return "ScimException(error=" + this.getError() + ", status=" + this.getStatus() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ScimException)) return false;
    final ScimException other = (ScimException) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$error = this.getError();
    final Object other$error = other.getError();
    if (this$error == null ? other$error != null : !this$error.equals(other$error)) return false;
    final Object this$status = this.getStatus();
    final Object other$status = other.getStatus();
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ScimException;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $error = this.getError();
    result = result * PRIME + ($error == null ? 43 : $error.hashCode());
    final Object $status = this.getStatus();
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    return result;
  }
}
