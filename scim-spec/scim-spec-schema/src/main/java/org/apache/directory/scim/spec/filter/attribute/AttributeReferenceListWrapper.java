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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AttributeReferenceListWrapper {
    /** A logger for this class */
    private static final Logger log = LoggerFactory.getLogger(AttributeReferenceListWrapper.class);

  private Set<AttributeReference> attributeReferences = new HashSet<>();
  
  public AttributeReferenceListWrapper(String attributeReferencesString) {

    String[] split = StringUtils.split(attributeReferencesString, ",");

    for (String af : split) {
      log.debug("--> Attribute -> " + af);
      AttributeReference attributeReference = new AttributeReference(af.trim());
      attributeReferences.add(attributeReference);
    }
  }
  
  public static AttributeReferenceListWrapper of(Set<AttributeReference> attributeReferences) {
    AttributeReferenceListWrapper wrapper = new AttributeReferenceListWrapper("");
    wrapper.attributeReferences = attributeReferences;
    return wrapper;
  }

  public static Set<AttributeReference> getAttributeReferences(AttributeReferenceListWrapper attributeReferenceListWrapper) {
    return Optional.ofNullable(attributeReferenceListWrapper)
      .map(wrapper -> wrapper.getAttributeReferences())
      .orElse(Collections.emptySet());
  }

  public String toString() {
    if (attributeReferences == null || attributeReferences.isEmpty()) {
      return "";
    }
    
    return attributeReferences.stream().map(AttributeReference::toString).collect(Collectors.joining(","));
  }

  public Set<AttributeReference> getAttributeReferences() {
    return this.attributeReferences;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AttributeReferenceListWrapper other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$attributeReferences = this.getAttributeReferences();
    final Object other$attributeReferences = other.getAttributeReferences();
    if (this$attributeReferences == null ? other$attributeReferences != null : !this$attributeReferences.equals(other$attributeReferences))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AttributeReferenceListWrapper;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $attributeReferences = this.getAttributeReferences();
    result = result * PRIME + ($attributeReferences == null ? 43 : $attributeReferences.hashCode());
    return result;
  }
}
