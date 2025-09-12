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

import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;

import java.util.Objects;
import java.util.Set;

public class ScimRequestContext {

  private Set<AttributeReference> attributeReferences;
  private Set<AttributeReference> excludedAttributeReferences;
  private PageRequest pageRequest;
  private SortRequest sortRequest;

  public ScimRequestContext() {}

  public ScimRequestContext(Set<AttributeReference> attributeReferences,
                            Set<AttributeReference> excludedAttributeReferences,
                            PageRequest pageRequest,
                            SortRequest sortRequest) {
    this.attributeReferences = attributeReferences;
    this.excludedAttributeReferences = excludedAttributeReferences;
    this.pageRequest = pageRequest;
    this.sortRequest = sortRequest;
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

  public PageRequest getPageRequest() {
    return pageRequest;
  }

  public ScimRequestContext setPageRequest(PageRequest pageRequest) {
    this.pageRequest = pageRequest;
    return this;
  }

  public SortRequest getSortRequest() {
    return sortRequest;
  }

  public ScimRequestContext setSortRequest(SortRequest sortRequest) {
    this.sortRequest = sortRequest;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    ScimRequestContext that = (ScimRequestContext) o;
    return Objects.equals(getAttributeReferences(), that.getAttributeReferences()) && Objects.equals(getExcludedAttributeReferences(), that.getExcludedAttributeReferences()) && Objects.equals(getPageRequest(), that.getPageRequest()) && Objects.equals(getSortRequest(), that.getSortRequest());
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(getAttributeReferences());
    result = 31 * result + Objects.hashCode(getExcludedAttributeReferences());
    result = 31 * result + Objects.hashCode(getPageRequest());
    result = 31 * result + Objects.hashCode(getSortRequest());
    return result;
  }

  @Override
  public String toString() {
    return "ScimRequestContext{" +
      "attributeReferences=" + attributeReferences +
      ", excludedAttributeReferences=" + excludedAttributeReferences +
      ", pageRequest=" + pageRequest +
      ", sortRequest=" + sortRequest +
      '}';
  }
}
