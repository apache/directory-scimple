package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.resources.Name;

public class NameBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String formatted;
    private String familyName;
    private String givenName;
    private String middleName;
    private String honorificPrefix;
    private String honorificSuffix;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.formatted = initialData.formatted;
      this.familyName = initialData.familyName;
      this.givenName = initialData.givenName;
      this.middleName = initialData.middleName;
      this.honorificPrefix = initialData.honorificPrefix;
      this.honorificSuffix = initialData.honorificSuffix;
    }

    public Builder formatted(String formatted) {
      this.formatted = formatted;
      return this;
    }

    public Builder familyName(String familyName) {
      this.familyName = familyName;
      return this;
    }

    public Builder givenName(String givenName) {
      this.givenName = givenName;
      return this;
    }

    public Builder middleName(String middleName) {
      this.middleName = middleName;
      return this;
    }

    public Builder honorificPrefix(String honorificPrefix) {
      this.honorificPrefix = honorificPrefix;
      return this;
    }

    public Builder honorificSuffix(String honorificSuffix) {
      this.honorificSuffix = honorificSuffix;
      return this;
    }

    public Name build() {
      Name name = new Name();
      name.setHonorificPrefix(this.honorificPrefix);
      name.setGivenName(this.givenName);
      name.setMiddleName(this.middleName);
      name.setFamilyName(this.familyName);
      name.setHonorificSuffix(this.honorificSuffix);
      name.setFormatted(this.formatted);

      return name;
    }
  }
}
