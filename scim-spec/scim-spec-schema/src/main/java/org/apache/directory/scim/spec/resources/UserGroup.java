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

package org.apache.directory.scim.spec.resources;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceIdReference;
import org.apache.directory.scim.spec.schema.Schema;

import java.io.Serializable;

@XmlType(propOrder = {"value","ref","display","type"})
@XmlAccessorType(XmlAccessType.NONE)
public class UserGroup implements Serializable {

  private static final long serialVersionUID = 9126588075353486789L;

  public String getValue() {
    return this.value;
  }

  public UserGroup setValue(String value) {
    this.value = value;
    return this;
  }

  public String getRef() {
    return this.ref;
  }

  public UserGroup setRef(String ref) {
    this.ref = ref;
    return this;
  }

  public String getDisplay() {
    return this.display;
  }

  public UserGroup setDisplay(String display) {
    this.display = display;
    return this;
  }

  public Type getType() {
    return this.type;
  }

  public UserGroup setType(Type type) {
    this.type = type;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserGroup)) return false;
    final UserGroup other = (UserGroup) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$value = this.getValue();
    final Object other$value = other.getValue();
    if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
    final Object this$ref = this.getRef();
    final Object other$ref = other.getRef();
    if (this$ref == null ? other$ref != null : !this$ref.equals(other$ref)) return false;
    final Object this$display = this.getDisplay();
    final Object other$display = other.getDisplay();
    if (this$display == null ? other$display != null : !this$display.equals(other$display)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof UserGroup;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $value = this.getValue();
    result = result * PRIME + ($value == null ? 43 : $value.hashCode());
    final Object $ref = this.getRef();
    result = result * PRIME + ($ref == null ? 43 : $ref.hashCode());
    final Object $display = this.getDisplay();
    result = result * PRIME + ($display == null ? 43 : $display.hashCode());
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    return result;
  }

  public String toString() {
    return "UserGroup(value=" + this.getValue() + ", ref=" + this.getRef() + ", display=" + this.getDisplay() + ", type=" + this.getType() + ")";
  }

  @XmlEnum
  public enum Type {
    @XmlEnumValue("direct") DIRECT,
    @XmlEnumValue("indirect") INDIRECT;
  }
  
  @ScimAttribute(description="The identifier of the User's group.",
    mutability = Schema.Attribute.Mutability.READ_ONLY)
  @ScimResourceIdReference
  @XmlElement
  String value;

  @ScimAttribute(name = "$ref", description="The URI of the corresponding 'Group' resource to which the user belongs.",
    referenceTypes={"User", "Group"},
    mutability = Schema.Attribute.Mutability.READ_ONLY)
  @XmlElement(name = "$ref")
  String ref;

  @ScimAttribute(description="A human-readable name, primarily used for display purposes.",
    mutability = Schema.Attribute.Mutability.READ_ONLY)
  @XmlElement
  String display;

  @ScimAttribute(description="A label indicating the attribute's function, e.g., 'direct' or 'indirect'.",
    canonicalValueList={"direct", "indirect"},
    mutability = Schema.Attribute.Mutability.READ_ONLY)
  @XmlElement
  Type type;
}
