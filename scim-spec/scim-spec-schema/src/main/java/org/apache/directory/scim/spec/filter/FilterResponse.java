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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

/**
 * Holds the result of a {@link org.apache.directory.scim.core.repository.Repository#find Repository.find()}
 * query, including the paginated resources and the total count of matching resources.
 *
 * <p><b>Important:</b> {@code totalResults} must be the total number of resources matching
 * the query <em>before</em> pagination is applied, not the number of resources in this page.
 * This allows SCIM clients to calculate how many pages exist. See
 * <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.4">RFC 7644 §3.4.2.4</a>.</p>
 *
 * <p>Example: if 50 users match a filter and the client requests {@code startIndex=11&count=10},
 * then {@code resources} contains 10 users and {@code totalResults} is 50.</p>
 *
 * @param <T> the resource type
 */
public class FilterResponse<T> {

  /**
   * Returns a {@link Collector} that accumulates stream elements and applies pagination,
   * producing a {@link FilterResponse} whose {@code totalResults} reflects the pre-pagination
   * count.
   *
   * <p>This is intended for in-memory or demo implementations. Production repositories
   * should push pagination into the data store's query language.</p>
   *
   * <p>Usage:</p>
   * <pre>{@code
   * return items.stream()
   *     .filter(FilterExpressions.inMemory(filter, schema))
   *     .sorted(SortExpressions.comparator(sortRequest, schema))
   *     .collect(FilterResponse.paginate(pageRequest));
   * }</pre>
   *
   * @param pageRequest the pagination parameters (startIndex and count)
   * @param <T>         the resource type
   * @return a collector that produces a paginated {@code FilterResponse}
   * @see PageRequest#paginate(List)
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.4">RFC 7644 §3.4.2.4</a>
   */
  public static <T> Collector<T, ?, FilterResponse<T>> paginate(PageRequest pageRequest) {
    return Collector.<T, List<T>, FilterResponse<T>>of(
      ArrayList::new,
      List::add,
      (a, b) -> { a.addAll(b); return a; },
      list -> new FilterResponse<>(pageRequest.paginate(list), list.size())
    );
  }

  private Collection<T> resources;

  /**
   * The total number of resources matching the query, before pagination.
   * This is NOT the size of the {@link #resources} collection (which is the page size).
   */
  private int totalResults;

  public FilterResponse() {}

  /**
   * Creates a filter response with the given page of resources and total count.
   *
   * @param resources    the resources in this page (may be a subset of all matching resources)
   * @param totalResults the total number of matching resources <em>before</em> pagination —
   *                     must be {@code >= resources.size()}
   */
  public FilterResponse(Collection<T> resources, int totalResults) {
    this.resources = resources;
    this.totalResults = totalResults;
  }

  public Collection<T> getResources() {
    return this.resources;
  }

  public FilterResponse<T> setResources(Collection<T> resources) {
    this.resources = resources;
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
    if (!(o instanceof FilterResponse other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$resources = this.getResources();
    final Object other$resources = other.getResources();
    if (this$resources == null ? other$resources != null : !this$resources.equals(other$resources)) return false;
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
    result = result * PRIME + this.getTotalResults();
    return result;
  }

  public String toString() {
    return "FilterResponse(resources=" + this.getResources() + ", totalResults=" + this.getTotalResults() + ")";
  }
}
