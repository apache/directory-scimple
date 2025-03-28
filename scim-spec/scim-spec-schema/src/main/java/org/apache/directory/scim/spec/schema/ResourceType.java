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

package org.apache.directory.scim.spec.schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResourceWithOptionalId;
import org.apache.directory.scim.spec.validator.Urn;

/**
 * SCIM ResourceType
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7643#section-6">ResourceType Schema</a>
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceType extends ScimResourceWithOptionalId {
  
  public static final String RESOURCE_NAME = "ResourceType";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";
  private static final long serialVersionUID = -696969911228870476L;

  public @Size(min = 1) String getName() {
    return this.name;
  }

  public ResourceType setName(@Size(min = 1) String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return this.description;
  }

  public ResourceType setDescription(String description) {
    this.description = description;
    return this;
  }

  public @Size(min = 1) String getEndpoint() {
    return this.endpoint;
  }

  public ResourceType setEndpoint(@Size(min = 1) String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public @Urn @Size(min = 1) String getSchemaUrn() {
    return this.schemaUrn;
  }

  public ResourceType setSchemaUrn(@Urn @Size(min = 1) String schemaUrn) {
    this.schemaUrn = schemaUrn;
    return this;
  }

  public List<SchemaExtensionConfiguration> getSchemaExtensions() {
    return this.schemaExtensions;
  }

  public ResourceType setSchemaExtensions(List<SchemaExtensionConfiguration> schemaExtensions) {
    this.schemaExtensions = schemaExtensions;
    return this;
  }

  public String toString() {
    return "ResourceType(name=" + this.getName() + ", description=" + this.getDescription() + ", endpoint=" + this.getEndpoint() + ", schemaUrn=" + this.getSchemaUrn() + ", schemaExtensions=" + this.getSchemaExtensions() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ResourceType)) return false;
    final ResourceType other = (ResourceType) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
    final Object this$description = this.getDescription();
    final Object other$description = other.getDescription();
    if (this$description == null ? other$description != null : !this$description.equals(other$description))
      return false;
    final Object this$endpoint = this.getEndpoint();
    final Object other$endpoint = other.getEndpoint();
    if (this$endpoint == null ? other$endpoint != null : !this$endpoint.equals(other$endpoint)) return false;
    final Object this$schemaUrn = this.getSchemaUrn();
    final Object other$schemaUrn = other.getSchemaUrn();
    if (this$schemaUrn == null ? other$schemaUrn != null : !this$schemaUrn.equals(other$schemaUrn)) return false;
    final Object this$schemaExtensions = this.getSchemaExtensions();
    final Object other$schemaExtensions = other.getSchemaExtensions();
    if (this$schemaExtensions == null ? other$schemaExtensions != null : !this$schemaExtensions.equals(other$schemaExtensions))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ResourceType;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $description = this.getDescription();
    result = result * PRIME + ($description == null ? 43 : $description.hashCode());
    final Object $endpoint = this.getEndpoint();
    result = result * PRIME + ($endpoint == null ? 43 : $endpoint.hashCode());
    final Object $schemaUrn = this.getSchemaUrn();
    result = result * PRIME + ($schemaUrn == null ? 43 : $schemaUrn.hashCode());
    final Object $schemaExtensions = this.getSchemaExtensions();
    result = result * PRIME + ($schemaExtensions == null ? 43 : $schemaExtensions.hashCode());
    return result;
  }

  public static class SchemaExtensionConfiguration implements Serializable {

    private static final long serialVersionUID = 7351651561572744255L;

    @XmlElement(name = "schema")
    @Urn
    @Size(min = 1)
    String schemaUrn;

    @XmlElement
    boolean required;

    public @Urn @Size(min = 1) String getSchemaUrn() {
      return this.schemaUrn;
    }

    public SchemaExtensionConfiguration setSchemaUrn(@Urn @Size(min = 1) String schemaUrn) {
      this.schemaUrn = schemaUrn;
      return this;
    }

    public boolean isRequired() {
      return this.required;
    }

    public SchemaExtensionConfiguration setRequired(boolean required) {
      this.required = required;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof SchemaExtensionConfiguration)) return false;
      final SchemaExtensionConfiguration other = (SchemaExtensionConfiguration) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$schemaUrn = this.getSchemaUrn();
      final Object other$schemaUrn = other.getSchemaUrn();
      if (this$schemaUrn == null ? other$schemaUrn != null : !this$schemaUrn.equals(other$schemaUrn)) return false;
      if (this.isRequired() != other.isRequired()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof SchemaExtensionConfiguration;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $schemaUrn = this.getSchemaUrn();
      result = result * PRIME + ($schemaUrn == null ? 43 : $schemaUrn.hashCode());
      result = result * PRIME + (this.isRequired() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "ResourceType.SchemaExtensionConfiguration(schemaUrn=" + this.getSchemaUrn() + ", required=" + this.isRequired() + ")";
    }
  }

  @XmlElement
  @Size(min = 1)
  String name;

  @XmlElement
  String description;

  @XmlElement
  @Size(min = 1)
  String endpoint;

  @XmlElement(name = "schema")
  @Urn
  @Size(min = 1)
  String schemaUrn;

  @XmlElement
  List<SchemaExtensionConfiguration> schemaExtensions;
  
  public ResourceType() {
    super(SCHEMA_URI, RESOURCE_NAME);
  }
  
  public ResourceType(ScimResourceType annotation) {
    super(SCHEMA_URI, RESOURCE_NAME);
    this.name = annotation.name();
    this.description = annotation.description();
    this.schemaUrn = annotation.schema();
    this.endpoint = annotation.endpoint();
  }

  @Override
  public ResourceType setSchemas(Set<String> schemas) {
    return (ResourceType) super.setSchemas(schemas);
  }

  @Override
  public ResourceType setMeta(@NotNull Meta meta) {
    return (ResourceType) super.setMeta(meta);
  }

  @Override
  public ResourceType setExternalId(String externalId) {
    return (ResourceType) super.setExternalId(externalId);
  }

  @Override
  public ResourceType setExtensions(Map<String, ScimExtension> extensions) {
    return (ResourceType) super.setExtensions(extensions);
  }

  @Override
  public ResourceType setId(String id) {
    return (ResourceType) super.setId(id);
  }

  @Override
  public ResourceType addSchema(String urn) {
    return (ResourceType) super.addSchema(urn);
  }

  @Override
  public ResourceType addExtension(ScimExtension extension) {
    return (ResourceType) super.addExtension(extension);
  }
}
