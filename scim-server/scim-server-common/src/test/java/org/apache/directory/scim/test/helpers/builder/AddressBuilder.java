package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.spec.resources.Address;

public class AddressBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {
    private String type;
    private Boolean primary;
    private String display;
    private String formatted;
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.country = initialData.country;
      this.display = initialData.display;
      this.formatted = initialData.formatted;
      this.locality = initialData.locality;
      this.region = initialData.region;
      this.postalCode = initialData.postalCode;
      this.primary = initialData.primary;
      this.streetAddress = initialData.streetAddress;
      this.type = initialData.type;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder primary(Boolean primary) {
      this.primary = primary;
      return this;
    }

    public Builder display(String display) {
      this.display = display;
      return this;
    }


    public Builder formatted(String formatted) {
      this.formatted = formatted;
      return this;
    }

    public Builder streetAddress(String streetAddress) {
      this.streetAddress = streetAddress;
      return this;
    }

    public Builder locality(String locality) {
      this.locality = locality;
      return this;
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    public Builder postalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    public Address build() {
      Address address = new Address();
      address.setType(this.type);
      address.setPrimary(this.primary);
      address.setDisplay(this.display);
      address.setFormatted(this.formatted);
      address.setStreetAddress(this.streetAddress);
      address.setLocality(this.locality);
      address.setRegion(this.region);
      address.setPostalCode(this.postalCode);
      address.setCountry(this.country);

      return address;
    }
  }
}
