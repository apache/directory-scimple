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

package org.apache.directory.scim.protocol.data;

import java.util.List;

import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.directory.scim.spec.resources.BaseResource;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkResponse extends BaseResource<BulkResponse> {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

  @XmlElement(name = "Operations")
  List<BulkOperation> operations;

  @XmlElement(name="status")
  @XmlJavaTypeAdapter(StatusAdapter.class)
  Status status;
  
  @XmlElement(name="response")
  ErrorResponse errorResponse;
  
  public BulkResponse() {
    super(SCHEMA_URI);
  }

  public List<BulkOperation> getOperations() {
    return this.operations;
  }

  public BulkResponse setOperations(List<BulkOperation> operations) {
    this.operations = operations;
    return this;
  }

  public Status getStatus() {
    return this.status;
  }

  public BulkResponse setStatus(Status status) {
    this.status = status;
    return this;
  }

  public ErrorResponse getErrorResponse() {
    return this.errorResponse;
  }

  public BulkResponse setErrorResponse(ErrorResponse errorResponse) {
    this.errorResponse = errorResponse;
    return this;
  }

  public String toString() {
    return "BulkResponse(operations=" + this.getOperations() + ", status=" + this.getStatus() + ", errorResponse=" + this.getErrorResponse() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BulkResponse)) return false;
    final BulkResponse other = (BulkResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$operations = this.getOperations();
    final Object other$operations = other.getOperations();
    if (this$operations == null ? other$operations != null : !this$operations.equals(other$operations)) return false;
    final Object this$status = this.getStatus();
    final Object other$status = other.getStatus();
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    final Object this$errorResponse = this.getErrorResponse();
    final Object other$errorResponse = other.getErrorResponse();
    if (this$errorResponse == null ? other$errorResponse != null : !this$errorResponse.equals(other$errorResponse))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof BulkResponse;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $operations = this.getOperations();
    result = result * PRIME + ($operations == null ? 43 : $operations.hashCode());
    final Object $status = this.getStatus();
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    final Object $errorResponse = this.getErrorResponse();
    result = result * PRIME + ($errorResponse == null ? 43 : $errorResponse.hashCode());
    return result;
  }
}
