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

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;

public class SortRequest {
  private AttributeReference sortBy;
  private SortOrder sortOrder;

  public AttributeReference getSortBy() {
    return this.sortBy;
  }

  public SortRequest setSortBy(AttributeReference sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  public SortOrder getSortOrder() {
    return this.sortOrder;
  }

  public SortRequest setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SortRequest other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$sortBy = this.getSortBy();
    final Object other$sortBy = other.getSortBy();
    if (this$sortBy == null ? other$sortBy != null : !this$sortBy.equals(other$sortBy)) return false;
    final Object this$sortOrder = this.getSortOrder();
    final Object other$sortOrder = other.getSortOrder();
    if (this$sortOrder == null ? other$sortOrder != null : !this$sortOrder.equals(other$sortOrder)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SortRequest;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $sortBy = this.getSortBy();
    result = result * PRIME + ($sortBy == null ? 43 : $sortBy.hashCode());
    final Object $sortOrder = this.getSortOrder();
    result = result * PRIME + ($sortOrder == null ? 43 : $sortOrder.hashCode());
    return result;
  }

  public String toString() {
    return "SortRequest(sortBy=" + this.getSortBy() + ", sortOrder=" + this.getSortOrder() + ")";
  }
}
