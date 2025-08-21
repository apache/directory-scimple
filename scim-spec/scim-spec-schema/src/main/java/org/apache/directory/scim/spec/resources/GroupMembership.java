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
public class GroupMembership implements Serializable {

  private static final long serialVersionUID = 6418041921926482112L;

  public static final String TYPE_USER = "User";
  public static final String TYPE_GROUP = "Group";

  public String getValue() {
    return this.value;
  }

  public GroupMembership setValue(String value) {
    this.value = value;
    return this;
  }

  public String getRef() {
    return this.ref;
  }

  public GroupMembership setRef(String ref) {
    this.ref = ref;
    return this;
  }

  public String getDisplay() {
    return this.display;
  }

  public GroupMembership setDisplay(String display) {
    this.display = display;
    return this;
  }

  public String getType() {
    return this.type;
  }

  public GroupMembership setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @deprecated The list of membership types is not limited to the canonical list, use strings instead.
   */
  @Deprecated
  public GroupMembership setType(Type type) {
    this.type = type.toString();
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    GroupMembership that = (GroupMembership) o;
    return Objects.equals(getValue(), that.getValue())
      && Objects.equals(getRef(), that.getRef())
      && Objects.equals(getDisplay(), that.getDisplay())
      && Objects.equals(getType(), that.getType());
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
    return "GroupMembership(value=" + this.getValue() + ", ref=" + this.getRef() + ", display=" + this.getDisplay() + ", type=" + this.getType() + ")";
  }

  /**
   * Canonical list of membership types.
   * @deprecated The list of membership types is not limited to the canonical list, use strings instead.
   */
  @Deprecated
  @XmlEnum
  public enum Type {
    @XmlEnumValue(TYPE_USER) USER(TYPE_USER),
    @XmlEnumValue(TYPE_GROUP) GROUP(TYPE_GROUP);

    private final String name;

    Type(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public static Type fromString(String name) {
      return Type.valueOf(name.toUpperCase(Locale.ROOT));
    }
  }
  
  @ScimAttribute(description="Identifier of the member of this Group.",
    mutability = Schema.Attribute.Mutability.IMMUTABLE)
  @ScimResourceIdReference
  @XmlElement
  String value;

  @ScimAttribute(name = "$ref", description="The URI corresponding to a SCIM resource that is a member of this Group.",
    referenceTypes={"User", "Group"},
    mutability = Schema.Attribute.Mutability.IMMUTABLE)
  @XmlElement(name = "$ref")
  String ref;

  @ScimAttribute(description="A human readable name, primarily used for display purposes.",
    mutability = Schema.Attribute.Mutability.READ_ONLY)
  @XmlElement
  String display;

  @ScimAttribute(description="A label indicating the type of resource, e.g., 'User' or 'Group'.",
    canonicalValueList={"User", "Group"},
    mutability = Schema.Attribute.Mutability.IMMUTABLE)
  @XmlElement
  String type;
}
