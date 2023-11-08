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

package org.apache.directory.scim.core.repository;

import lombok.Data;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;

import java.util.Set;

/**
 * Holds information about a SCIM query/find/search request.
 */
@Data
public class FindRequest {

  private final Set<AttributeReference> attributes;

  private final Set<AttributeReference> excludedAttributes;

  private final Filter filter;

  private final AttributeReference sortBy;

  private final SortOrder sortOrder;

  private final Integer startIndex;

  private final Integer count;

  public FindRequest(Set<AttributeReference> attributes, Set<AttributeReference> excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) {
    this.attributes = attributes;
    this.excludedAttributes = excludedAttributes;
    this.filter = filter;
    this.sortBy = sortBy;
    this.sortOrder = sortOrder;
    this.startIndex = startIndex;
    this.count = count;
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
