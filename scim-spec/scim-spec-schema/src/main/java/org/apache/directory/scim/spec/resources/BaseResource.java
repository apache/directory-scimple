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

package org.apache.directory.scim.spec.resources;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import org.apache.directory.scim.spec.validator.Urn;

/**
 * All the different variations of SCIM responses require that the object
 * contains a list of the schemas it conforms to.
 * 
 * @author crh5255
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource<SELF extends BaseResource<SELF>> implements Serializable {

  @Serial
  private static final long serialVersionUID = -7603956873008734403L;

  @XmlElement(name="schemas")
  @Size(min = 1)
  Set<@Urn String> schemas;

  public BaseResource(@Urn String urn) {
    addSchema(urn);
  }

  public SELF addSchema(@Urn String urn) {
    if (schemas == null){
      schemas = new TreeSet<>();
    }
    schemas.add(urn);
    return self();
  }

  public SELF setSchemas(@Urn Set<String> schemas) {
    if (schemas == null) {
      this.schemas.clear();
    } else {
      this.schemas = new TreeSet<>(schemas);
    }
    return self();
  }

  @SuppressWarnings("unchecked")
  protected SELF self() {
    return (SELF) this;
  }

  public @Size(min = 1) Set<@Urn String> getSchemas() {
    return this.schemas;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BaseResource other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$schemas = this.getSchemas();
    final Object other$schemas = other.getSchemas();
    if (this$schemas == null ? other$schemas != null : !this$schemas.equals(other$schemas)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof BaseResource;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $schemas = this.getSchemas();
    result = result * PRIME + ($schemas == null ? 43 : $schemas.hashCode());
    return result;
  }

  public String toString() {
    return "BaseResource(schemas=" + this.getSchemas() + ")";
  }
}
