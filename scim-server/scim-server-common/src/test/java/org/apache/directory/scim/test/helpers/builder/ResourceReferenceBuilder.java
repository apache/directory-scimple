package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.schema.ResourceReference;

public class ResourceReferenceBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {
    private String ref;
    private String display;
    private String value;
    private ResourceReference.ReferenceType type;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.ref = initialData.ref;
      this.display = initialData.display;
      this.value = initialData.value;
      this.type = initialData.type;
    }

    public Builder ref(String ref) {
      this.ref = ref;
      return this;
    }

    public Builder display(String display) {
      this.display = display;
      return this;
    }

    public Builder type(ResourceReference.ReferenceType type) {
      this.type = type;
      return this;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    public ResourceReference build() {
      final ResourceReference resourceReference = new ResourceReference();

      resourceReference.setDisplay(this.display);
      resourceReference.setRef(this.ref);
      resourceReference.setType(this.type);
      resourceReference.setValue(this.value);

      return resourceReference;
    }
  }
}
