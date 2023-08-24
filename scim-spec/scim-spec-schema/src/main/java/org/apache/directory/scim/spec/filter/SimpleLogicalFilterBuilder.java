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

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class SimpleLogicalFilterBuilder implements FilterBuilder {

  protected FilterExpression filterExpression;

  static FilterExpression groupIfNeeded(FilterExpression filterExpression) {
    return (filterExpression instanceof LogicalExpression)
      ? new GroupExpression(false, filterExpression)
      : filterExpression;
  }

  protected void handleComparisonExpression(FilterExpression expression) {

    if (expression == null) {
      log.error("*** in handle comparison ---> expression == null");
    }

    if (filterExpression == null) {
      filterExpression = expression;
    } else {
      if (!(filterExpression instanceof LogicalExpression)) {
        throw new IllegalStateException("Invalid filter state");
      }

      LogicalExpression le = (LogicalExpression) filterExpression;
      le.setRight(groupIfNeeded(expression));
    }
  }

  @Override
  public String toString() {
    return filterExpression.toFilter();
  }

  @Override
  public FilterExpression filter() {
    return filterExpression;
  }

  @Override
  public Filter build() {
    return new Filter(filterExpression);
  }
}
