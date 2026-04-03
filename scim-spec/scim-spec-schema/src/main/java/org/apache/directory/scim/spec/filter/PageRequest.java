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

import java.util.List;

public class PageRequest {
  private Integer startIndex;
  private Integer count;

  public Integer getStartIndex() {
    return this.startIndex;
  }

  public PageRequest setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  public Integer getCount() {
    return this.count;
  }

  public PageRequest setCount(Integer count) {
    this.count = count;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof PageRequest other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$startIndex = this.getStartIndex();
    final Object other$startIndex = other.getStartIndex();
    if (this$startIndex == null ? other$startIndex != null : !this$startIndex.equals(other$startIndex)) return false;
    final Object this$count = this.getCount();
    final Object other$count = other.getCount();
    if (this$count == null ? other$count != null : !this$count.equals(other$count)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof PageRequest;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $startIndex = this.getStartIndex();
    result = result * PRIME + ($startIndex == null ? 43 : $startIndex.hashCode());
    final Object $count = this.getCount();
    result = result * PRIME + ($count == null ? 43 : $count.hashCode());
    return result;
  }

  /**
   * Converts the 1-based SCIM {@code startIndex} to a 0-based offset suitable for
   * Java stream {@code skip()} or list {@code subList()} operations.
   *
   * <p>SCIM uses 1-based indexing: {@code startIndex=1} means the first result.
   * Per <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.4">RFC 7644 §3.4.2.4</a>,
   * a value less than 1 is interpreted as 1 (offset 0). Returns 0 if
   * {@code startIndex} is null.</p>
   *
   * @return the 0-based offset (always {@code >= 0})
   */
  public long getZeroBasedStartIndex() {
    return startIndex != null ? Math.max(0, startIndex - 1L) : 0L;
  }

  /**
   * Returns the effective page size, defaulting to {@code totalResults} if the
   * client did not specify a {@code count} parameter.
   *
   * <p>Per <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.4">RFC 7644 §3.4.2.4</a>,
   * a {@code count} of 0 means "return no resources" (only {@code totalResults}),
   * and a negative value is interpreted as 0.</p>
   *
   * @param totalResults the total number of matching resources (before pagination)
   * @return the effective page size (always {@code >= 0})
   */
  public long getEffectiveCount(int totalResults) {
    if (count == null) {
      return (long) totalResults;
    }
    return Math.max(0L, count.longValue());
  }

  /**
   * Returns the sublist of {@code items} corresponding to this page.
   *
   * <p>Applies {@link #getZeroBasedStartIndex()} and {@link #getEffectiveCount(int)}
   * to produce the correct window, clamping to list bounds.</p>
   *
   * @param items the full list of matching results (before pagination)
   * @param <T>   the element type
   * @return the page slice; never null
   */
  public <T> List<T> paginate(List<T> items) {
    int from = (int) Math.min(getZeroBasedStartIndex(), items.size());
    int to = (int) Math.min(from + getEffectiveCount(items.size()), items.size());
    return items.subList(from, to);
  }

  public String toString() {
    return "PageRequest(startIndex=" + this.getStartIndex() + ", count=" + this.getCount() + ")";
  }
}
