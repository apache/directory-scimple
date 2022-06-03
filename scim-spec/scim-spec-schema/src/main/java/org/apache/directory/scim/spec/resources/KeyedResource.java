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
import lombok.extern.slf4j.Slf4j;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@Slf4j
public class KeyedResource implements Serializable {
  
  private static final long serialVersionUID = 4479747886354926691L;
  
  @XmlElement
  //TODO: Adding ScimAttribute is not valid because adding new fields to the Schema is not allowed. This needs to be revisited.
  @ScimAttribute
  private String key;
  
  public KeyedResource() {
  }
  
  public void setKey(String key) {
    log.debug("Setting the key for a keyed resource to " + key);
    
    this.key = key;
  }
  
  public String getKey() {
    return key;
  }
}
