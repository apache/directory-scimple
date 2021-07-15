package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.extension.EnterpriseExtension;

public class EnterpriseExtensionManagerBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String value;
    private String ref;
    private String displayName;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.value = initialData.value;
      this.ref = initialData.ref;
      this.displayName = initialData.displayName;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    public Builder ref(String ref) {
      this.ref = ref;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public EnterpriseExtension.Manager build() {
      EnterpriseExtension.Manager manager = new EnterpriseExtension.Manager();
      manager.setDisplayName(this.displayName);
      manager.setRef(this.ref);
      manager.setValue(this.value);

      return manager;
    }
  }
}
