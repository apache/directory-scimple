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
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorResponse extends BaseResource {

  private static final long serialVersionUID = 9045421198080348116L;

  public static String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

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
}
