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
public class UserGroup implements Serializable {

  private static final long serialVersionUID = 9126588075353486789L;

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
