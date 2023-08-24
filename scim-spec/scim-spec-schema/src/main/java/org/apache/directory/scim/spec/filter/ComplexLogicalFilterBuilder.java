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

abstract class ComplexLogicalFilterBuilder extends SimpleLogicalFilterBuilder {

    @Override
    public FilterBuilder or(FilterExpression fe1) {
      if (filterExpression == null) {
        throw new IllegalStateException("Cannot call or(Filter), call or(Filter, Filter) instead.");
      }
      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(groupIfNeeded(filterExpression));
      logicalExpression.setRight(groupIfNeeded(fe1));
      logicalExpression.setOperator(LogicalOperator.OR);
      filterExpression = logicalExpression;
      return this;
    }
    
    @Override
    public FilterBuilder or(FilterExpression fe1, FilterExpression fe2) {
      if (filterExpression == null) {
        LogicalExpression logicalExpression = new LogicalExpression();
        logicalExpression.setLeft(groupIfNeeded(fe1));
        logicalExpression.setRight(groupIfNeeded(fe2));
        logicalExpression.setOperator(LogicalOperator.OR);
        filterExpression = logicalExpression;
      } else {
        this.or(fe1).or(fe2);
      }
      return this;
    }

    @Override
    public FilterBuilder and(FilterExpression fe1, FilterExpression fe2) {
      if (filterExpression == null) {
        LogicalExpression logicalExpression = new LogicalExpression();
        logicalExpression.setLeft(groupIfNeeded(fe1));
        logicalExpression.setRight(groupIfNeeded(fe2));
        logicalExpression.setOperator(LogicalOperator.AND);
        filterExpression = logicalExpression;
      } else {
        this.and(fe1).and(fe2);
      }
      return this;
    }
    
    @Override
    public FilterBuilder and(FilterExpression fe1) {
      if (filterExpression == null) {
        throw new IllegalStateException("Cannot call and(Filter), call and(Filter, Filter) instead.");
      }

      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(filterExpression);
      logicalExpression.setRight(groupIfNeeded(fe1));
      logicalExpression.setOperator(LogicalOperator.AND);
      filterExpression = logicalExpression;
      return this;
    }
  }
