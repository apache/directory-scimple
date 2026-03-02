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

import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.io.Serial;
import java.io.Serializable;

@XmlType(propOrder = {"method","path","bulkId","data"})
@XmlAccessorType(XmlAccessType.NONE)
public class BulkOperation implements Serializable {

  @Serial
  private static final long serialVersionUID = 6528874816710788132L;

  public enum Method {
    @XmlEnumValue("POST") POST,
    @XmlEnumValue("PUT") PUT,
    @XmlEnumValue("PATCH") PATCH,
    @XmlEnumValue("DELETE") DELETE;
  }

  @XmlAccessorType(XmlAccessType.NONE)
  public static class StatusWrapper implements Serializable {

    @Serial
    private static final long serialVersionUID = 1544738718748608248L;

    public StatusWrapper(Status code) {
      this.code = code;
    }

    public static StatusWrapper wrap(Status code) {
      return new StatusWrapper(code);
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(StatusAdapter.class)
    Status code;

    public Status getCode() {
      return this.code;
    }

    public StatusWrapper setCode(Status code) {
      this.code = code;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof StatusWrapper other)) return false;
      if (!other.canEqual((Object) this)) return false;
      final Object this$code = this.getCode();
      final Object other$code = other.getCode();
      if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof StatusWrapper;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $code = this.getCode();
      result = result * PRIME + ($code == null ? 43 : $code.hashCode());
      return result;
    }

    public String toString() {
      return "BulkOperation.StatusWrapper(code=" + this.getCode() + ")";
    }
  }

  @XmlElement
  Method method;

  @XmlElement
  String bulkId;

  @XmlElement
  String version;

  @XmlElement
  String path;

  @XmlElement
  ScimResource data;

  @XmlElement
  String location;

  @XmlElement
  BaseResource response;

  @XmlElement
  StatusWrapper status;

  public Method getMethod() {
    return this.method;
  }

  public BulkOperation setMethod(Method method) {
    this.method = method;
    return this;
  }

  public String getBulkId() {
    return this.bulkId;
  }

  public BulkOperation setBulkId(String bulkId) {
    this.bulkId = bulkId;
    return this;
  }

  public String getVersion() {
    return this.version;
  }

  public BulkOperation setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getPath() {
    return this.path;
  }

  public BulkOperation setPath(String path) {
    this.path = path;
    return this;
  }

  public ScimResource getData() {
    return this.data;
  }

  public BulkOperation setData(ScimResource data) {
    this.data = data;
    return this;
  }

  public String getLocation() {
    return this.location;
  }

  public BulkOperation setLocation(String location) {
    this.location = location;
    return this;
  }

  public BaseResource getResponse() {
    return this.response;
  }

  public BulkOperation setResponse(BaseResource response) {
    this.response = response;
    return this;
  }

  public StatusWrapper getStatus() {
    return this.status;
  }

  public BulkOperation setStatus(StatusWrapper status) {
    this.status = status;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BulkOperation other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$method = this.getMethod();
    final Object other$method = other.getMethod();
    if (this$method == null ? other$method != null : !this$method.equals(other$method)) return false;
    final Object this$bulkId = this.getBulkId();
    final Object other$bulkId = other.getBulkId();
    if (this$bulkId == null ? other$bulkId != null : !this$bulkId.equals(other$bulkId)) return false;
    final Object this$version = this.getVersion();
    final Object other$version = other.getVersion();
    if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
    final Object this$path = this.getPath();
    final Object other$path = other.getPath();
    if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
    final Object this$data = this.getData();
    final Object other$data = other.getData();
    if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
    final Object this$location = this.getLocation();
    final Object other$location = other.getLocation();
    if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
    final Object this$response = this.getResponse();
    final Object other$response = other.getResponse();
    if (this$response == null ? other$response != null : !this$response.equals(other$response)) return false;
    final Object this$status = this.getStatus();
    final Object other$status = other.getStatus();
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof BulkOperation;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $method = this.getMethod();
    result = result * PRIME + ($method == null ? 43 : $method.hashCode());
    final Object $bulkId = this.getBulkId();
    result = result * PRIME + ($bulkId == null ? 43 : $bulkId.hashCode());
    final Object $version = this.getVersion();
    result = result * PRIME + ($version == null ? 43 : $version.hashCode());
    final Object $path = this.getPath();
    result = result * PRIME + ($path == null ? 43 : $path.hashCode());
    final Object $data = this.getData();
    result = result * PRIME + ($data == null ? 43 : $data.hashCode());
    final Object $location = this.getLocation();
    result = result * PRIME + ($location == null ? 43 : $location.hashCode());
    final Object $response = this.getResponse();
    result = result * PRIME + ($response == null ? 43 : $response.hashCode());
    final Object $status = this.getStatus();
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    return result;
  }

  public String toString() {
    return "BulkOperation(method=" + this.getMethod() + ", bulkId=" + this.getBulkId() + ", version=" + this.getVersion() + ", path=" + this.getPath() + ", data=" + this.getData() + ", location=" + this.getLocation() + ", response=" + this.getResponse() + ", status=" + this.getStatus() + ")";
  }
}
