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

package edu.psu.swe.scim.spec.protocol.data;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkOperation {

  public enum Method {
    @XmlEnumValue("POST") POST,
    @XmlEnumValue("PUT") PUT,
    @XmlEnumValue("PATCH") PATCH,
    @XmlEnumValue("DELETE") DELETE;
  }
  
  @Data
  @AllArgsConstructor
  @XmlAccessorType(XmlAccessType.NONE)
  public static class StatusWrapper {
    
    public static StatusWrapper wrap(Status code) {
      return new StatusWrapper(code);
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(StatusAdapter.class)
    Status code;
  }

  @XmlElement
  Method method;
  
  @XmlElement
  String bulkId;
  
  @XmlElement
  String version;
  
  @XmlElement
  String path;
  
  @XmlElement
  ScimResource data;
  
  @XmlElement
  String location;
  
  @XmlElement
  BaseResource response;
  
  @XmlElement
  StatusWrapper status;
}
