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

import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class SearchRequest extends BaseResource<SearchRequest> {

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

}
