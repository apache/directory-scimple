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

package org.apache.directory.scim.spec.extension;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import lombok.Data;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "EnterpriseUser", id = EnterpriseExtension.URN, description = "Attributes commonly used in representing users that belong to, or act on behalf of, a business or enterprise.")
@Data
public class EnterpriseExtension implements ScimExtension {

  private static final long serialVersionUID = -6850246976790442980L;

  public static final String URN = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class Manager implements Serializable {

    private static final long serialVersionUID = -7930518578899296192L;

    @ScimAttribute(description = "The \"id\" of the SCIM resource representing the user's manager.  RECOMMENDED.")
    @XmlElement
    private String value;

    @ScimAttribute(name="$ref", description = "The URI of the SCIM resource representing the User's manager.  RECOMMENDED.")
    @XmlElement(name="$ref")
    private String ref;

    @ScimAttribute(mutability = Mutability.READ_ONLY, description = "he displayName of the user's manager.  This attribute is OPTIONAL.")
    @XmlElement
    private String displayName;
  }

  @ScimAttribute(description = "A string identifier, typically numeric or alphanumeric, assigned to a person, typically based on order of hire or association with an organization.")
  @XmlElement
  private String employeeNumber;

  @ScimAttribute(description = "Identifies the name of a cost center.")
  @XmlElement
  private String costCenter;

  @ScimAttribute(description = "Identifies the name of an organization.")
  @XmlElement
  private String organization;

  @ScimAttribute(description = "Identifies the name of a division.")
  @XmlElement
  private String division;

  @ScimAttribute(description = "Identifies the name of a department.")
  @XmlElement
  private String department;

  @ScimAttribute(description = "The user's manager.  A complex type that optionally allows service providers to represent organizational hierarchy by referencing the \"id\" attribute of another User.")
  @XmlElement
  private Manager manager;

  @Override
  public String getUrn() {
    return URN;
  }
}
