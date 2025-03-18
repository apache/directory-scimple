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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * This class overrides the required id element in ScimResource for use as a
 * base class for some of the odd SCIM resources.
 * 
 * @author crh5255
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResourceWithOptionalId extends ScimResource {
  
  private static final long serialVersionUID = -379538554565387791L;

  @XmlElement
  String id;
  
  public ScimResourceWithOptionalId(String urn, String resourceType) {
    super(urn, resourceType);
  }

  public String getId() {
    return this.id;
  }

  public ScimResourceWithOptionalId setId(String id) {
    this.id = id;
    return this;
  }

  public String toString() {
    return "ScimResourceWithOptionalId(id=" + this.getId() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ScimResourceWithOptionalId)) return false;
    final ScimResourceWithOptionalId other = (ScimResourceWithOptionalId) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ScimResourceWithOptionalId;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    return result;
  }
}
