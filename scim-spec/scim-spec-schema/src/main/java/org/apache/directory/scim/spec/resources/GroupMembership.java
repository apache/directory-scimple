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
import lombok.Data;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceIdReference;
import org.apache.directory.scim.spec.schema.Schema;

import java.io.Serializable;

@Data
@XmlType(propOrder = {"value","ref","display","type"})
@XmlAccessorType(XmlAccessType.NONE)
public class GroupMembership implements Serializable {

  private static final long serialVersionUID = 9126588075353486789L;

  @XmlEnum
  public enum Type {
    @XmlEnumValue("User") USER,
    @XmlEnumValue("Group") GROUP;
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
  Type type;
}
