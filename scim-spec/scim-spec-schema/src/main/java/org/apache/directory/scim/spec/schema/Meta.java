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
import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.directory.scim.spec.adapter.LocalDateTimeAdapter;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;

/**
 * Defines the structure of the meta attribute for all SCIM resources as defined
 * by section 3.1 of the SCIM schema specification. See
 * <a href="https://datatracker.ietf.org/doc/html/rfc7643#section-3.1">RFC 7643 section 3.1</a> for more
 * details.
 *
 * @author Steve Moyer {@literal <smoyer@psu.edu>}
 */
@XmlType(name = "meta")
@XmlAccessorType(XmlAccessType.NONE)
public class Meta implements Serializable {
  
  private static final long serialVersionUID = -9162917034280030708L;

  @XmlElement
  @Size(min = 1)
  @ScimAttribute(mutability = Mutability.READ_ONLY, caseExact = true, description = "The name of the resource type of the resource.")
  String resourceType;
  
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  @ScimAttribute(mutability = Mutability.READ_ONLY, description = "The DateTime that the resource was added to the service provider.")
  LocalDateTime created;
  
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  @ScimAttribute(mutability = Mutability.READ_ONLY, description = "The most recent DateTime that the details of this resource were updated at the service provider.")
  LocalDateTime lastModified;
  
  @XmlElement
  @ScimAttribute(mutability = Mutability.READ_ONLY, description = "The URI of the resource being returned.")
  String location;
  
  @XmlElement
  @ScimAttribute(mutability = Mutability.READ_ONLY, description = "The version of the resource being returned.  This value must be the same as the entity-tag (ETag) HTTP response header")
  String version;

  public @Size(min = 1) String getResourceType() {
    return this.resourceType;
  }

  public Meta setResourceType(@Size(min = 1) String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  public LocalDateTime getCreated() {
    return this.created;
  }

  public Meta setCreated(LocalDateTime created) {
    this.created = created;
    return this;
  }

  public LocalDateTime getLastModified() {
    return this.lastModified;
  }

  public Meta setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public String getLocation() {
    return this.location;
  }

  public Meta setLocation(String location) {
    this.location = location;
    return this;
  }

  public String getVersion() {
    return this.version;
  }

  public Meta setVersion(String version) {
    this.version = version;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Meta)) return false;
    final Meta other = (Meta) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$resourceType = this.getResourceType();
    final Object other$resourceType = other.getResourceType();
    if (this$resourceType == null ? other$resourceType != null : !this$resourceType.equals(other$resourceType))
      return false;
    final Object this$created = this.getCreated();
    final Object other$created = other.getCreated();
    if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
    final Object this$lastModified = this.getLastModified();
    final Object other$lastModified = other.getLastModified();
    if (this$lastModified == null ? other$lastModified != null : !this$lastModified.equals(other$lastModified))
      return false;
    final Object this$location = this.getLocation();
    final Object other$location = other.getLocation();
    if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
    final Object this$version = this.getVersion();
    final Object other$version = other.getVersion();
    if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Meta;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $resourceType = this.getResourceType();
    result = result * PRIME + ($resourceType == null ? 43 : $resourceType.hashCode());
    final Object $created = this.getCreated();
    result = result * PRIME + ($created == null ? 43 : $created.hashCode());
    final Object $lastModified = this.getLastModified();
    result = result * PRIME + ($lastModified == null ? 43 : $lastModified.hashCode());
    final Object $location = this.getLocation();
    result = result * PRIME + ($location == null ? 43 : $location.hashCode());
    final Object $version = this.getVersion();
    result = result * PRIME + ($version == null ? 43 : $version.hashCode());
    return result;
  }

  public String toString() {
    return "Meta(resourceType=" + this.getResourceType() + ", created=" + this.getCreated() + ", lastModified=" + this.getLastModified() + ", location=" + this.getLocation() + ", version=" + this.getVersion() + ")";
  }
}
