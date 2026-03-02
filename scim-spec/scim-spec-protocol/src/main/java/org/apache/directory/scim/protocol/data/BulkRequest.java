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

import java.io.Serial;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.resources.BaseResource;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkRequest extends BaseResource<BulkRequest> {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";

  @Serial
  private static final long serialVersionUID = -296570866318702047L;

  @XmlElement
  Integer failOnErrors;
  
  @XmlElement(name = "Operations")
  List<BulkOperation> operations;
  
  public BulkRequest() {
    super(SCHEMA_URI);
  }

  public Integer getFailOnErrors() {
    return this.failOnErrors;
  }

  public BulkRequest setFailOnErrors(Integer failOnErrors) {
    this.failOnErrors = failOnErrors;
    return this;
  }

  public List<BulkOperation> getOperations() {
    return this.operations;
  }

  public BulkRequest setOperations(List<BulkOperation> operations) {
    this.operations = operations;
    return this;
  }

  public String toString() {
    return "BulkRequest(failOnErrors=" + this.getFailOnErrors() + ", operations=" + this.getOperations() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BulkRequest other)) return false;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$failOnErrors = this.getFailOnErrors();
    final Object other$failOnErrors = other.getFailOnErrors();
    if (this$failOnErrors == null ? other$failOnErrors != null : !this$failOnErrors.equals(other$failOnErrors))
      return false;
    final Object this$operations = this.getOperations();
    final Object other$operations = other.getOperations();
    if (this$operations == null ? other$operations != null : !this$operations.equals(other$operations)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof BulkRequest;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $failOnErrors = this.getFailOnErrors();
    result = result * PRIME + ($failOnErrors == null ? 43 : $failOnErrors.hashCode());
    final Object $operations = this.getOperations();
    result = result * PRIME + ($operations == null ? 43 : $operations.hashCode());
    return result;
  }
}
