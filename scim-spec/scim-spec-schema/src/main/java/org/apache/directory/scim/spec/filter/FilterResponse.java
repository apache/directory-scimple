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

package org.apache.directory.scim.spec.filter;

import java.util.Collection;

public class FilterResponse<T> {
  
  private Collection<T> resources;
  private PageRequest pageRequest;
  private int totalResults;
  
  public FilterResponse() {}
  
  public FilterResponse(Collection<T> resources, PageRequest pageRequest, int totalResults) {
    this.resources = resources;
    this.pageRequest = pageRequest;
    this.totalResults = totalResults;
  }

  public Collection<T> getResources() {
    return this.resources;
  }

  public FilterResponse<T> setResources(Collection<T> resources) {
    this.resources = resources;
    return this;
  }

  public PageRequest getPageRequest() {
    return this.pageRequest;
  }

  public FilterResponse<T> setPageRequest(PageRequest pageRequest) {
    this.pageRequest = pageRequest;
    return this;
  }

  public int getTotalResults() {
    return this.totalResults;
  }

  public FilterResponse<T> setTotalResults(int totalResults) {
    this.totalResults = totalResults;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof FilterResponse)) return false;
    final FilterResponse<?> other = (FilterResponse<?>) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$resources = this.getResources();
    final Object other$resources = other.getResources();
    if (this$resources == null ? other$resources != null : !this$resources.equals(other$resources)) return false;
    final Object this$pageRequest = this.getPageRequest();
    final Object other$pageRequest = other.getPageRequest();
    if (this$pageRequest == null ? other$pageRequest != null : !this$pageRequest.equals(other$pageRequest))
      return false;
    if (this.getTotalResults() != other.getTotalResults()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof FilterResponse;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $resources = this.getResources();
    result = result * PRIME + ($resources == null ? 43 : $resources.hashCode());
    final Object $pageRequest = this.getPageRequest();
    result = result * PRIME + ($pageRequest == null ? 43 : $pageRequest.hashCode());
    result = result * PRIME + this.getTotalResults();
    return result;
  }

  public String toString() {
    return "FilterResponse(resources=" + this.getResources() + ", pageRequest=" + this.getPageRequest() + ", totalResults=" + this.getTotalResults() + ")";
  }
}
