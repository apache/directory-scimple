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

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.annotation.ScimAttribute;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">section 4.1.2</a>
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Role implements Serializable, TypedAttribute {

  private static final long serialVersionUID = -2781839189814966670L;

  @XmlElement(nillable=true)
  @ScimAttribute(description="A label indicating the attribute's function.")
  String type;
  
  @XmlElement
  @ScimAttribute(description="The value of a role.")
  String value;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;

  public String getType() {
    return this.type;
  }

  public Role setType(String type) {
    this.type = type;
    return this;
  }

  public String getValue() {
    return this.value;
  }

  public Role setValue(String value) {
    this.value = value;
    return this;
  }

  public String getDisplay() {
    return this.display;
  }

  public Role setDisplay(String display) {
    this.display = display;
    return this;
  }

  public Boolean getPrimary() {
    return this.primary;
  }

  public Role setPrimary(Boolean primary) {
    this.primary = primary;
    return this;
  }

  public String toString() {
    return "Role(type=" + this.getType() + ", value=" + this.getValue() + ", display=" + this.getDisplay() + ", primary=" + this.getPrimary() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Role)) return false;
    final Role other = (Role) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$value = this.getValue();
    final Object other$value = other.getValue();
    if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
    final Object this$display = this.getDisplay();
    final Object other$display = other.getDisplay();
    if (this$display == null ? other$display != null : !this$display.equals(other$display)) return false;
    final Object this$primary = this.getPrimary();
    final Object other$primary = other.getPrimary();
    if (this$primary == null ? other$primary != null : !this$primary.equals(other$primary)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Role;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $value = this.getValue();
    result = result * PRIME + ($value == null ? 43 : $value.hashCode());
    final Object $display = this.getDisplay();
    result = result * PRIME + ($display == null ? 43 : $display.hashCode());
    final Object $primary = this.getPrimary();
    result = result * PRIME + ($primary == null ? 43 : $primary.hashCode());
    return result;
  }
}
