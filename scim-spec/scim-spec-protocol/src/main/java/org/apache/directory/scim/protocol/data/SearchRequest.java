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

package org.apache.directory.scim.protocol.data;

import java.io.Serial;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.directory.scim.protocol.adapter.AttributeReferenceAdapter;
import org.apache.directory.scim.protocol.adapter.FilterAdapter;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.resources.BaseResource;

/**
 * See Section 3.4.3 Querying Resources Using HTTP POST
 * (<a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">RFC 7644 section 3.4.3</a>)
 *
 * @author crh5255
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class SearchRequest extends BaseResource<SearchRequest> {

  @Serial
  private static final long serialVersionUID = 8217513543318598565L;

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";

  @XmlElement
  @XmlJavaTypeAdapter(AttributeReferenceAdapter.class)
  Set<AttributeReference> attributes;

  @XmlElement
  @XmlJavaTypeAdapter(AttributeReferenceAdapter.class)
  Set<AttributeReference> excludedAttributes;

  @XmlElement
  @XmlJavaTypeAdapter(FilterAdapter.class)
  Filter filter;

  @XmlElement
  @XmlJavaTypeAdapter(AttributeReferenceAdapter.class)
  AttributeReference sortBy;

  @XmlElement
  SortOrder sortOrder;

  @XmlElement
  Integer startIndex;

  @XmlElement
  Integer count;
  
  public SearchRequest() {
    super(SCHEMA_URI);
  }
  
  public PageRequest getPageRequest() {
    PageRequest pageRequest = new PageRequest();
    pageRequest.setStartIndex(startIndex);
    pageRequest.setCount(count);
    return pageRequest;
  }
  
  public SortRequest getSortRequest() {
    SortRequest sortRequest = new SortRequest();
    sortRequest.setSortBy(sortBy);
    sortRequest.setSortOrder(sortOrder);
    return sortRequest;
  }

  public Set<AttributeReference> getAttributes() {
    return this.attributes;
  }

  public SearchRequest setAttributes(Set<AttributeReference> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Set<AttributeReference> getExcludedAttributes() {
    return this.excludedAttributes;
  }

  public SearchRequest setExcludedAttributes(Set<AttributeReference> excludedAttributes) {
    this.excludedAttributes = excludedAttributes;
    return this;
  }

  public Filter getFilter() {
    return this.filter;
  }

  public SearchRequest setFilter(Filter filter) {
    this.filter = filter;
    return this;
  }

  public AttributeReference getSortBy() {
    return this.sortBy;
  }

  public SearchRequest setSortBy(AttributeReference sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  public SortOrder getSortOrder() {
    return this.sortOrder;
  }

  public SearchRequest setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  public Integer getStartIndex() {
    return this.startIndex;
  }

  public SearchRequest setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  public Integer getCount() {
    return this.count;
  }

  public SearchRequest setCount(Integer count) {
    this.count = count;
    return this;
  }

  public String toString() {
    return "SearchRequest(attributes=" + this.getAttributes() + ", excludedAttributes=" + this.getExcludedAttributes() + ", filter=" + this.getFilter() + ", sortBy=" + this.getSortBy() + ", sortOrder=" + this.getSortOrder() + ", startIndex=" + this.getStartIndex() + ", count=" + this.getCount() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SearchRequest other)) return false;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$attributes = this.getAttributes();
    final Object other$attributes = other.getAttributes();
    if (this$attributes == null ? other$attributes != null : !this$attributes.equals(other$attributes)) return false;
    final Object this$excludedAttributes = this.getExcludedAttributes();
    final Object other$excludedAttributes = other.getExcludedAttributes();
    if (this$excludedAttributes == null ? other$excludedAttributes != null : !this$excludedAttributes.equals(other$excludedAttributes))
      return false;
    final Object this$filter = this.getFilter();
    final Object other$filter = other.getFilter();
    if (this$filter == null ? other$filter != null : !this$filter.equals(other$filter)) return false;
    final Object this$sortBy = this.getSortBy();
    final Object other$sortBy = other.getSortBy();
    if (this$sortBy == null ? other$sortBy != null : !this$sortBy.equals(other$sortBy)) return false;
    final Object this$sortOrder = this.getSortOrder();
    final Object other$sortOrder = other.getSortOrder();
    if (this$sortOrder == null ? other$sortOrder != null : !this$sortOrder.equals(other$sortOrder)) return false;
    final Object this$startIndex = this.getStartIndex();
    final Object other$startIndex = other.getStartIndex();
    if (this$startIndex == null ? other$startIndex != null : !this$startIndex.equals(other$startIndex)) return false;
    final Object this$count = this.getCount();
    final Object other$count = other.getCount();
    if (this$count == null ? other$count != null : !this$count.equals(other$count)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SearchRequest;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $attributes = this.getAttributes();
    result = result * PRIME + ($attributes == null ? 43 : $attributes.hashCode());
    final Object $excludedAttributes = this.getExcludedAttributes();
    result = result * PRIME + ($excludedAttributes == null ? 43 : $excludedAttributes.hashCode());
    final Object $filter = this.getFilter();
    result = result * PRIME + ($filter == null ? 43 : $filter.hashCode());
    final Object $sortBy = this.getSortBy();
    result = result * PRIME + ($sortBy == null ? 43 : $sortBy.hashCode());
    final Object $sortOrder = this.getSortOrder();
    result = result * PRIME + ($sortOrder == null ? 43 : $sortOrder.hashCode());
    final Object $startIndex = this.getStartIndex();
    result = result * PRIME + ($startIndex == null ? 43 : $startIndex.hashCode());
    final Object $count = this.getCount();
    result = result * PRIME + ($count == null ? 43 : $count.hashCode());
    return result;
  }
}
