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
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import org.apache.directory.scim.spec.validator.Urn;
import lombok.Data;

/**
 * All the different variations of SCIM responses require that the object
 * contains a list of the schemas it conforms to.
 * 
 * @author crh5255
 *
 */
@Data
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource implements Serializable {

  private static final long serialVersionUID = -7603956873008734403L;

  @XmlElement(name="schemas")
  @Size(min = 1)
  @Urn
  Set<String> schemas;
  
  public BaseResource(String urn) {
    addSchema(urn);
  }
  
  public void addSchema(String urn) {
    if (schemas == null){
      schemas = new HashSet<>();
    }
    
    schemas.add(urn);
  }
  
}
