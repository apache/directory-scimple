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

import java.io.Serial;
import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "EnterpriseUser", id = EnterpriseExtension.URN, description = "Attributes commonly used in representing users that belong to, or act on behalf of, a business or enterprise.")
public class EnterpriseExtension implements ScimExtension {

  @Serial
  private static final long serialVersionUID = -6850246976790442980L;

  public static final String URN = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

  public String getEmployeeNumber() {
    return this.employeeNumber;
  }

  public EnterpriseExtension setEmployeeNumber(String employeeNumber) {
    this.employeeNumber = employeeNumber;
    return this;
  }

  public String getCostCenter() {
    return this.costCenter;
  }

  public EnterpriseExtension setCostCenter(String costCenter) {
    this.costCenter = costCenter;
    return this;
  }

  public String getOrganization() {
    return this.organization;
  }

  public EnterpriseExtension setOrganization(String organization) {
    this.organization = organization;
    return this;
  }

  public String getDivision() {
    return this.division;
  }

  public EnterpriseExtension setDivision(String division) {
    this.division = division;
    return this;
  }

  public String getDepartment() {
    return this.department;
  }

  public EnterpriseExtension setDepartment(String department) {
    this.department = department;
    return this;
  }

  public Manager getManager() {
    return this.manager;
  }

  public EnterpriseExtension setManager(Manager manager) {
    this.manager = manager;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof EnterpriseExtension)) return false;
    final EnterpriseExtension other = (EnterpriseExtension) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$employeeNumber = this.getEmployeeNumber();
    final Object other$employeeNumber = other.getEmployeeNumber();
    if (this$employeeNumber == null ? other$employeeNumber != null : !this$employeeNumber.equals(other$employeeNumber))
      return false;
    final Object this$costCenter = this.getCostCenter();
    final Object other$costCenter = other.getCostCenter();
    if (this$costCenter == null ? other$costCenter != null : !this$costCenter.equals(other$costCenter)) return false;
    final Object this$organization = this.getOrganization();
    final Object other$organization = other.getOrganization();
    if (this$organization == null ? other$organization != null : !this$organization.equals(other$organization))
      return false;
    final Object this$division = this.getDivision();
    final Object other$division = other.getDivision();
    if (this$division == null ? other$division != null : !this$division.equals(other$division)) return false;
    final Object this$department = this.getDepartment();
    final Object other$department = other.getDepartment();
    if (this$department == null ? other$department != null : !this$department.equals(other$department)) return false;
    final Object this$manager = this.getManager();
    final Object other$manager = other.getManager();
    if (this$manager == null ? other$manager != null : !this$manager.equals(other$manager)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof EnterpriseExtension;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $employeeNumber = this.getEmployeeNumber();
    result = result * PRIME + ($employeeNumber == null ? 43 : $employeeNumber.hashCode());
    final Object $costCenter = this.getCostCenter();
    result = result * PRIME + ($costCenter == null ? 43 : $costCenter.hashCode());
    final Object $organization = this.getOrganization();
    result = result * PRIME + ($organization == null ? 43 : $organization.hashCode());
    final Object $division = this.getDivision();
    result = result * PRIME + ($division == null ? 43 : $division.hashCode());
    final Object $department = this.getDepartment();
    result = result * PRIME + ($department == null ? 43 : $department.hashCode());
    final Object $manager = this.getManager();
    result = result * PRIME + ($manager == null ? 43 : $manager.hashCode());
    return result;
  }

  public String toString() {
    return "EnterpriseExtension(employeeNumber=" + this.getEmployeeNumber() + ", costCenter=" + this.getCostCenter() + ", organization=" + this.getOrganization() + ", division=" + this.getDivision() + ", department=" + this.getDepartment() + ", manager=" + this.getManager() + ")";
  }

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  public static class Manager implements Serializable {

    @Serial
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

    public String getValue() {
      return this.value;
    }

    public Manager setValue(String value) {
      this.value = value;
      return this;
    }

    public String getRef() {
      return this.ref;
    }

    public Manager setRef(String ref) {
      this.ref = ref;
      return this;
    }

    public String getDisplayName() {
      return this.displayName;
    }

    public Manager setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Manager)) return false;
      final Manager other = (Manager) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$value = this.getValue();
      final Object other$value = other.getValue();
      if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
      final Object this$ref = this.getRef();
      final Object other$ref = other.getRef();
      if (this$ref == null ? other$ref != null : !this$ref.equals(other$ref)) return false;
      final Object this$displayName = this.getDisplayName();
      final Object other$displayName = other.getDisplayName();
      if (this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Manager;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $value = this.getValue();
      result = result * PRIME + ($value == null ? 43 : $value.hashCode());
      final Object $ref = this.getRef();
      result = result * PRIME + ($ref == null ? 43 : $ref.hashCode());
      final Object $displayName = this.getDisplayName();
      result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
      return result;
    }

    public String toString() {
      return "EnterpriseExtension.Manager(value=" + this.getValue() + ", ref=" + this.getRef() + ", displayName=" + this.getDisplayName() + ")";
    }
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
