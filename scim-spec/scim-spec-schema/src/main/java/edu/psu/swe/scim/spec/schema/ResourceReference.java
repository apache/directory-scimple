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

package edu.psu.swe.scim.spec.schema;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimResourceIdReference;
import lombok.Data;

@Data
@XmlType(propOrder = {"value","ref","display","type"})
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceReference implements Serializable {

  private static final long serialVersionUID = 9126588075353486789L;

  @XmlEnum
  public enum ReferenceType {
    @XmlEnumValue("direct") DIRECT,
    @XmlEnumValue("indirect") INDIRECT
  }
  
  @ScimAttribute(description="Reference Element Identifier")
  @ScimResourceIdReference
  @XmlElement
  String value;

  @ScimAttribute(description="The URI of the corresponding resource ", referenceTypes={"User", "Group"})
  @XmlElement(name = "$ref")
  String ref;

  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  @XmlElement
  String display;

  @ScimAttribute(description="A label indicating the attribute's function; e.g., 'direct' or 'indirect'.", canonicalValueList={"direct", "indirect"})
  @XmlElement
  ReferenceType type;
}
