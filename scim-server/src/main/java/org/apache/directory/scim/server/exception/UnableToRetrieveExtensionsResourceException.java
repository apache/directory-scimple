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

package org.apache.directory.scim.server.exception;

import javax.ws.rs.core.Response.Status;
import org.apache.directory.scim.spec.exception.ResourceException;

public class UnableToRetrieveExtensionsResourceException extends ResourceException {

  private static final long serialVersionUID = -3872700870424005641L;

  public UnableToRetrieveExtensionsResourceException(Status status, String message) {
    super(status.getStatusCode(), message);
  }

  public UnableToRetrieveExtensionsResourceException(Status status, String message, Throwable cause) {
    super(status.getStatusCode(), message, cause);
  }

  public String toString() {
    return "UnableToRetrieveExtensionsResourceException(status=" + this.getStatus() + ", " + getMessage() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof UnableToRetrieveExtensionsResourceException)) return false;
    final UnableToRetrieveExtensionsResourceException other = (UnableToRetrieveExtensionsResourceException) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof UnableToRetrieveExtensionsResourceException;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
