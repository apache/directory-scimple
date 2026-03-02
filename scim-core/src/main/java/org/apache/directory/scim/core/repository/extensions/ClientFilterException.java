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

package org.apache.directory.scim.core.repository.extensions;

import java.io.Serial;

public class ClientFilterException extends Exception {

  @Serial
  private static final long serialVersionUID = 3308947684934769952L;

  private final int status;

  public ClientFilterException(int statusCode, String message) {
    super(message);
    this.status = statusCode;
  }

  public int getStatus() {
    return this.status;
  }

  public String toString() {
    return "ClientFilterException(status=" + this.getStatus() + ", " + getMessage() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ClientFilterException other)) return false;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    if (this.getStatus() != other.getStatus()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ClientFilterException;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getStatus();
    return result;
  }
}
