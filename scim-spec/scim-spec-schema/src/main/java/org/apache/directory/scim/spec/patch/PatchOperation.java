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

package org.apache.directory.scim.spec.patch;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.directory.scim.spec.adapter.PatchOperationPathAdapter;

import java.io.Serializable;

@XmlType(propOrder={"operation", "path", "value"})
@XmlAccessorType(XmlAccessType.NONE)
public class PatchOperation implements Serializable {

  private static final long serialVersionUID = 7748584008639433236L;

  public Type getOperation() {
    return this.operation;
  }

  public PatchOperation setOperation(Type operation) {
    this.operation = operation;
    return this;
  }

  public PatchOperationPath getPath() {
    return this.path;
  }

  public PatchOperation setPath(PatchOperationPath path) {
    this.path = path;
    return this;
  }

  public Object getValue() {
    return this.value;
  }

  public PatchOperation setValue(Object value) {
    this.value = value;
    return this;
  }

  public String toString() {
    return "PatchOperation(operation=" + this.getOperation() + ", path=" + this.getPath() + ", value=" + this.getValue() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof PatchOperation)) return false;
    final PatchOperation other = (PatchOperation) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$operation = this.getOperation();
    final Object other$operation = other.getOperation();
    if (this$operation == null ? other$operation != null : !this$operation.equals(other$operation)) return false;
    final Object this$path = this.getPath();
    final Object other$path = other.getPath();
    if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
    final Object this$value = this.getValue();
    final Object other$value = other.getValue();
    if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof PatchOperation;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $operation = this.getOperation();
    result = result * PRIME + ($operation == null ? 43 : $operation.hashCode());
    final Object $path = this.getPath();
    result = result * PRIME + ($path == null ? 43 : $path.hashCode());
    final Object $value = this.getValue();
    result = result * PRIME + ($value == null ? 43 : $value.hashCode());
    return result;
  }

  @XmlEnum(String.class)
  public enum Type {
    @XmlEnumValue("add") ADD,
    @XmlEnumValue("remove") REMOVE,
    @XmlEnumValue("replace") REPLACE;
  }
  
  @XmlElement(name="op")
  private Type operation;
  
  @XmlElement
  @XmlJavaTypeAdapter(PatchOperationPathAdapter.class)
  private PatchOperationPath path;
  
  @XmlElement
  private Object value;
  
}
