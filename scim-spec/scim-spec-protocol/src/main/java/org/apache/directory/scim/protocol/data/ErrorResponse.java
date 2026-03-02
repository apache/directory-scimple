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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.resources.BaseResource;

import java.io.Serial;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorResponse extends BaseResource<ErrorResponse> {

  @Serial
  private static final long serialVersionUID = 9045421198080348116L;

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

  @XmlElement(nillable = true)
  private String detail;

  @XmlElement
  @XmlJavaTypeAdapter(StatusAdapter.class)
  private Status status;

  @XmlElement
  private ErrorMessageType scimType;

  protected ErrorResponse() {
    super(SCHEMA_URI);
  }

  public ErrorResponse(int statusCode, String detail) {
    this(Status.fromStatusCode(statusCode), detail);
  }

  public ErrorResponse(Status status, String detail) {
    this();
    this.status = status;
    this.detail = detail;
  }

  public Response toResponse() {
    return toResponse(this);
  }

  public static Response toResponse(ErrorResponse error) {
    return Response.status(error.status).entity(error).build();
  }

  public String getDetail() {
    return this.detail;
  }

  public ErrorResponse setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  public Status getStatus() {
    return this.status;
  }

  public ErrorResponse setStatus(Status status) {
    this.status = status;
    return this;
  }

  public ErrorMessageType getScimType() {
    return this.scimType;
  }

  public ErrorResponse setScimType(ErrorMessageType scimType) {
    this.scimType = scimType;
    return this;
  }

  public String toString() {
    return "ErrorResponse(detail=" + this.getDetail() + ", status=" + this.getStatus() + ", scimType=" + this.getScimType() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ErrorResponse)) return false;
    final ErrorResponse other = (ErrorResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$detail = this.getDetail();
    final Object other$detail = other.getDetail();
    if (this$detail == null ? other$detail != null : !this$detail.equals(other$detail)) return false;
    final Object this$status = this.getStatus();
    final Object other$status = other.getStatus();
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    final Object this$scimType = this.getScimType();
    final Object other$scimType = other.getScimType();
    if (this$scimType == null ? other$scimType != null : !this$scimType.equals(other$scimType)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ErrorResponse;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $detail = this.getDetail();
    result = result * PRIME + ($detail == null ? 43 : $detail.hashCode());
    final Object $status = this.getStatus();
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    final Object $scimType = this.getScimType();
    result = result * PRIME + ($scimType == null ? 43 : $scimType.hashCode());
    return result;
  }
}
