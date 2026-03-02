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

public class GroupExpression implements FilterExpression, ValueFilterExpression {

  boolean not;
  FilterExpression filterExpression;

  public GroupExpression() {}

  public GroupExpression(boolean not, FilterExpression filterExpression) {
    this.not = not;
    this.filterExpression = filterExpression;
  }

  @Override
  public String toFilter() {
    return (not ? "NOT" : "") + "(" + filterExpression.toFilter() + ")";
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.filterExpression.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toUnqualifiedFilter() {
    return (not ? "NOT" : "") + "(" + filterExpression.toUnqualifiedFilter() + ")";
  }

  public boolean isNot() {
    return this.not;
  }

  public GroupExpression setNot(boolean not) {
    this.not = not;
    return this;
  }

  public FilterExpression getFilterExpression() {
    return this.filterExpression;
  }

  public GroupExpression setFilterExpression(FilterExpression filterExpression) {
    this.filterExpression = filterExpression;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof GroupExpression other)) return false;
    if (!other.canEqual((Object) this)) return false;
    if (this.isNot() != other.isNot()) return false;
    final Object this$filterExpression = this.getFilterExpression();
    final Object other$filterExpression = other.getFilterExpression();
    if (this$filterExpression == null ? other$filterExpression != null : !this$filterExpression.equals(other$filterExpression))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof GroupExpression;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isNot() ? 79 : 97);
    final Object $filterExpression = this.getFilterExpression();
    result = result * PRIME + ($filterExpression == null ? 43 : $filterExpression.hashCode());
    return result;
  }

  public String toString() {
    return "GroupExpression(not=" + this.isNot() + ", filterExpression=" + this.getFilterExpression() + ")";
  }
}
