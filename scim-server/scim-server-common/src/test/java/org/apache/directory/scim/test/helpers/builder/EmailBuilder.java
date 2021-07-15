package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.resources.Email;

public class EmailBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String type;
    private boolean primary;
    private String value;
    private String display;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.type = initialData.type;
      this.primary = initialData.primary;
      this.value = initialData.value;
      this.display = initialData.display;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder primary(boolean primary) {
      this.primary = primary;
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

    public Email build() {
      Email email = new Email();
      email.setType(this.type);
      email.setPrimary(this.primary);
      email.setDisplay(this.display);
      email.setValue(this.value);

      return email;
    }
  }
}
