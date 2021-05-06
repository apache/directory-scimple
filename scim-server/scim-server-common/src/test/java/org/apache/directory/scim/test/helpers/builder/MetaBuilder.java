package org.apache.directory.scim.test.helpers.builder;

import java.time.LocalDateTime;

import org.apache.directory.scim.spec.schema.Meta;

public class MetaBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static final class Builder {

    private String resourceType;
    private LocalDateTime created;
    private LocalDateTime lastModified;
    private String location;
    private String version;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.resourceType = initialData.resourceType;
      this.created = initialData.created;
      this.lastModified = initialData.lastModified;
      this.location = initialData.location;
      this.version = initialData.version;
    }

    public Builder resourceType(String resourceType) {
      this.resourceType = resourceType;
      return this;
    }

    public Builder created(LocalDateTime created) {
      this.created = created;
      return this;
    }

    public Builder lastModified(LocalDateTime lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Meta build() {
      final Meta meta = new Meta();
      meta.setCreated(this.created);
      meta.setLastModified(this.lastModified);
      meta.setLocation(this.location);
      meta.setResourceType(this.resourceType);
      meta.setVersion(this.version);

      return meta;
    }
  }
}
