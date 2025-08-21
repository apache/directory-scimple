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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceIdReference;
import org.apache.directory.scim.spec.schema.Schema;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

@XmlType(propOrder = {"value","ref","display","type"})
@XmlAccessorType(XmlAccessType.NONE)
public class UserGroup implements Serializable {

  public static final String TYPE_DIRECT = "direct";
  public static final String TYPE_INDIRECT = "indirect";

  private static final long serialVersionUID = 8698508874413555857L;

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

  public String getType() {
    return this.type;
  }

  public UserGroup setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @deprecated The list of user group types is not limited to the canonical list, use strings instead.
   */
  @Deprecated
  public UserGroup setType(Type type) {
    this.type = type.toString();
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    UserGroup userGroup = (UserGroup) o;
    return Objects.equals(getValue(), userGroup.getValue())
      && Objects.equals(getRef(), userGroup.getRef())
      && Objects.equals(getDisplay(), userGroup.getDisplay())
      && Objects.equals(getType(), userGroup.getType());
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(getValue());
    result = 31 * result + Objects.hashCode(getRef());
    result = 31 * result + Objects.hashCode(getDisplay());
    result = 31 * result + Objects.hashCode(getType());
    return result;
  }

  public String toString() {
    return "UserGroup(value=" + this.getValue() + ", ref=" + this.getRef() + ", display=" + this.getDisplay() + ", type=" + this.getType() + ")";
  }

  /**
   * Canonical list of group types.
   * @deprecated The list of user group types is not limited to the canonical list, use strings instead.
   */
  @Deprecated
  @XmlEnum
  public enum Type {
    @XmlEnumValue(TYPE_DIRECT) DIRECT(TYPE_DIRECT),
    @XmlEnumValue(TYPE_INDIRECT) INDIRECT(TYPE_INDIRECT);

    private final String name;

    Type(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public static UserGroup.Type fromString(String name) {
      return UserGroup.Type.valueOf(name.toUpperCase(Locale.ROOT));
    }
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
  String type;
}
