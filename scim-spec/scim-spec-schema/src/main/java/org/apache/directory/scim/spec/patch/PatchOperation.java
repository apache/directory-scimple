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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.directory.scim.spec.adapter.PatchOperationPathAdapter;

@Data
@EqualsAndHashCode
@XmlType(propOrder={"operation", "path", "value"})
@XmlAccessorType(XmlAccessType.NONE)
public class PatchOperation {
  
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
