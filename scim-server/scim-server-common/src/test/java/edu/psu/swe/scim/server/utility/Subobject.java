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

package edu.psu.swe.scim.server.utility;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import lombok.Data;

@Data
public class Subobject implements Serializable {
  
  private static final long serialVersionUID = -8081556701833520316L;

  @ScimAttribute
  @XmlElement
  private String string1;
  
  @ScimAttribute
  @XmlElement
  private String string2;
  
  @ScimAttribute
  @XmlElement
  private Boolean boolean1;
  
  @ScimAttribute
  @XmlElement
  private Boolean boolean2;
  
  @ScimAttribute
  @XmlElement
  private List<String> list1;

  @ScimAttribute
  @XmlElement
  private List<String> list2;
}
