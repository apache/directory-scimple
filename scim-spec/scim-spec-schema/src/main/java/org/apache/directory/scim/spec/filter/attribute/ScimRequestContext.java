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

package org.apache.directory.scim.spec.filter.attribute;

import java.util.Set;

public class ScimRequestContext {

  private Set<AttributeReference> attributeReferences;
  private Set<AttributeReference> excludedAttributeReferences;

  public ScimRequestContext(Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) {
    this.attributeReferences = attributeReferences;
    this.excludedAttributeReferences = excludedAttributeReferences;
  }

  public Set<AttributeReference> getAttributeReferences() {
    return this.attributeReferences;
  }

  public ScimRequestContext setAttributeReferences(Set<AttributeReference> attributeReferences) {
    this.attributeReferences = attributeReferences;
    return this;
  }

  public Set<AttributeReference> getExcludedAttributeReferences() {
    return this.excludedAttributeReferences;
  }

  public ScimRequestContext setExcludedAttributeReferences(Set<AttributeReference> excludedAttributeReferences) {
    this.excludedAttributeReferences = excludedAttributeReferences;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ScimRequestContext)) return false;
    final ScimRequestContext other = (ScimRequestContext) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$attributeReferences = this.getAttributeReferences();
    final Object other$attributeReferences = other.getAttributeReferences();
    if (this$attributeReferences == null ? other$attributeReferences != null : !this$attributeReferences.equals(other$attributeReferences))
      return false;
    final Object this$excludedAttributeReferences = this.getExcludedAttributeReferences();
    final Object other$excludedAttributeReferences = other.getExcludedAttributeReferences();
    if (this$excludedAttributeReferences == null ? other$excludedAttributeReferences != null : !this$excludedAttributeReferences.equals(other$excludedAttributeReferences))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ScimRequestContext;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $attributeReferences = this.getAttributeReferences();
    result = result * PRIME + ($attributeReferences == null ? 43 : $attributeReferences.hashCode());
    final Object $excludedAttributeReferences = this.getExcludedAttributeReferences();
    result = result * PRIME + ($excludedAttributeReferences == null ? 43 : $excludedAttributeReferences.hashCode());
    return result;
  }

  public String toString() {
    return "ScimRequestContext(attributeReferences=" + this.getAttributeReferences() + ", excludedAttributeReferences=" + this.getExcludedAttributeReferences() + ")";
  }
}
