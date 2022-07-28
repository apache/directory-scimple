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

package org.apache.directory.scim.spec.protocol.attribute;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AttributeReferenceListWrapper {

  @Setter(AccessLevel.NONE)
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

  public String toString() {
    if (attributeReferences == null || attributeReferences.isEmpty()) {
      return "";
    }
    
    return attributeReferences.stream().map(AttributeReference::toString).collect(Collectors.joining(","));
  }
}
