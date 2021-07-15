package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.resources.Entitlement;

public class EntitlementBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String type;
    private String value;
    private String display;
    private Boolean primary;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.type = initialData.type;
      this.value = initialData.value;
      this.display = initialData.display;
      this.primary = initialData.primary;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    public Builder display(String display) {
      this.display = display;
      return this;
    }

    public Builder primary(Boolean primary) {
      this.primary = primary;
      return this;
    }

    public Entitlement build() {
      final Entitlement entitlement = new Entitlement();

      entitlement.setDisplay(this.display);
      entitlement.setPrimary(this.primary);
      entitlement.setType(this.type);
      entitlement.setValue(this.value);

      return entitlement;
    }
  }
}
