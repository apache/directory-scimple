package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.extension.EnterpriseExtension;

public class EnterpriseExtensionBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String employeeNumber;
    private String costCenter;
    private String organization;
    private String division;
    private String department;
    private EnterpriseExtension.Manager manager;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.employeeNumber = initialData.employeeNumber;
      this.costCenter = initialData.costCenter;
      this.organization = initialData.organization;
      this.division = initialData.division;
      this.department = initialData.department;
      this.manager = initialData.manager;
    }

    public Builder employeeNumber(String employeeNumber) {
      this.employeeNumber = employeeNumber;
      return this;
    }

    public Builder costCenter(String costCenter) {
      this.costCenter = costCenter;
      return this;
    }

    public Builder organization(String organization) {
      this.organization = organization;
      return this;
    }

    public Builder division(String division) {
      this.division = division;
      return this;
    }

    public Builder department(String department) {
      this.department = department;
      return this;
    }

    public Builder manager(EnterpriseExtension.Manager manager) {
      this.manager = manager;
      return this;
    }

    public EnterpriseExtension build() {
      EnterpriseExtension extension = new EnterpriseExtension();

      extension.setCostCenter(this.costCenter);
      extension.setDepartment(this.department);
      extension.setDivision(this.division);
      extension.setEmployeeNumber(this.employeeNumber);
      extension.setManager(this.manager);
      extension.setOrganization(this.organization);

      return extension;
    }
  }
}
