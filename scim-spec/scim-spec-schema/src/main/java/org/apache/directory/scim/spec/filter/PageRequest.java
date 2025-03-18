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
    if (!(o instanceof PageRequest)) return false;
    final PageRequest other = (PageRequest) o;
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

  public String toString() {
    return "PageRequest(startIndex=" + this.getStartIndex() + ", count=" + this.getCount() + ")";
  }
}
