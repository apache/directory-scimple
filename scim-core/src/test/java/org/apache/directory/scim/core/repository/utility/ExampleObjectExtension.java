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

package org.apache.directory.scim.core.repository.utility;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;

import java.io.Serializable;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "ExampleObject", id = ExampleObjectExtension.URN, description = "Example Object Extensions.")
@Data
public class ExampleObjectExtension implements ScimExtension {

  private static final long serialVersionUID = -5398090056271556423L;

  public static final String URN = "urn:ietf:params:scim:schemas:extension:example:2.0:Object";

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class ComplexObject implements Serializable {

    private static final long serialVersionUID = 2822581434679824690L;

    @ScimAttribute(description = "The \"id\" of the complex object.")
    @XmlElement
    private String value;

    @ScimAttribute(mutability = Mutability.READ_ONLY, description = "displayName of the object.")
    @XmlElement
    private String displayName;
  }

  @ScimAttribute(returned = Returned.ALWAYS)
  @XmlElement
  private String valueAlways;

  @ScimAttribute(returned = Returned.DEFAULT)
  @XmlElement
  private String valueDefault;

  @ScimAttribute(returned = Returned.NEVER)
  @XmlElement
  private String valueNever;

  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private String valueRequest;
  
  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private ComplexObject valueComplex;
  
  @ScimAttribute
  @XmlElement
  private List<String> list;
  
  @ScimAttribute
  @XmlElement
  private List<Order> enumList;
  
  @ScimAttribute
  @XmlElement
  private Subobject subobject;

  @Override
  public String getUrn() {
    return URN;
  }
}
