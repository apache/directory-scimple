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
import jakarta.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.annotation.ScimAttribute;

import java.io.Serial;
import java.io.Serializable;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">section 4.1.2</a>
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Photo implements Serializable, TypedAttribute {

  @Serial
  private static final long serialVersionUID = 8821620834716156789L;
 
  @XmlElement
  @ScimAttribute(description="URL of a photo of the User.", referenceTypes={"external"})
  String value;
  
  @XmlElement(nillable=true)
  @ScimAttribute(canonicalValueList={"photo", "thumbnail"}, description="A label indicating the attribute's function; e.g., 'photo' or 'thumbnail'.")
  String type;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;

  public String getValue() {
    return this.value;
  }

  public Photo setValue(String value) {
    this.value = value;
    return this;
  }

  public String getType() {
    return this.type;
  }

  public Photo setType(String type) {
    this.type = type;
    return this;
  }

  public String getDisplay() {
    return this.display;
  }

  public Photo setDisplay(String display) {
    this.display = display;
    return this;
  }

  public Boolean getPrimary() {
    return this.primary;
  }

  public Photo setPrimary(Boolean primary) {
    this.primary = primary;
    return this;
  }

  public String toString() {
    return "Photo(value=" + this.getValue() + ", type=" + this.getType() + ", display=" + this.getDisplay() + ", primary=" + this.getPrimary() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Photo other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$value = this.getValue();
    final Object other$value = other.getValue();
    if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$display = this.getDisplay();
    final Object other$display = other.getDisplay();
    if (this$display == null ? other$display != null : !this$display.equals(other$display)) return false;
    final Object this$primary = this.getPrimary();
    final Object other$primary = other.getPrimary();
    if (this$primary == null ? other$primary != null : !this$primary.equals(other$primary)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Photo;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $value = this.getValue();
    result = result * PRIME + ($value == null ? 43 : $value.hashCode());
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $display = this.getDisplay();
    result = result * PRIME + ($display == null ? 43 : $display.hashCode());
    final Object $primary = this.getPrimary();
    result = result * PRIME + ($primary == null ? 43 : $primary.hashCode());
    return result;
  }
}
