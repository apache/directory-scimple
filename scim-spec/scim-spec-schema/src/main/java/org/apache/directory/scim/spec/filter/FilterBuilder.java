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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.UnaryOperator;

public interface FilterBuilder {

  FilterBuilder and(FilterExpression fe1);

  default FilterBuilder and(UnaryOperator<FilterBuilder> filter) {
    return and(filter.apply(create()).build());
  }

  default FilterBuilder and(Filter filter) {
    return and(filter.getExpression());
  }

  FilterBuilder and(FilterExpression left, FilterExpression right);

  default FilterBuilder and(Filter left, Filter right) {
    return and(left.getExpression(), right.getExpression());
  }

  default FilterBuilder and(UnaryOperator<FilterBuilder> left, UnaryOperator<FilterBuilder> right) {
    return and(left.apply(create()).build(), right.apply(create()).build());
  }

  FilterBuilder or(FilterExpression fe1);

  default FilterBuilder or(Filter filter) {
    return or(filter.getExpression());
  }

  default FilterBuilder or(UnaryOperator<FilterBuilder> filter) {
    return or(filter.apply(create()).build());
  }

  FilterBuilder or(FilterExpression left, FilterExpression right);

  default FilterBuilder or(Filter left, Filter right) {
    return or(left.getExpression(), right.getExpression());
  }
  default FilterBuilder or(UnaryOperator<FilterBuilder> left, UnaryOperator<FilterBuilder> right) {
    return or(left.apply(create()).build(), right.apply(create()).build());
  }

  FilterBuilder equalTo(String key, String value);

  FilterBuilder equalTo(String key, Boolean value);

  FilterBuilder equalTo(String key, Date value);

  FilterBuilder equalTo(String key, LocalDate value);

  FilterBuilder equalTo(String key, LocalDateTime value);

  <T extends Number> FilterBuilder equalTo(String key, T value);

  FilterBuilder equalNull(String key);

  FilterBuilder notEqual(String key, String value);

  FilterBuilder notEqual(String key, Boolean value);

  FilterBuilder notEqual(String key, Date value);

  FilterBuilder notEqual(String key, LocalDate value);

  FilterBuilder notEqual(String key, LocalDateTime value);

  <T extends Number> FilterBuilder notEqual(String key, T value);

  FilterBuilder notEqualNull(String key);

  <T extends Number> FilterBuilder greaterThan(String key, T value);

  FilterBuilder greaterThan(String key, Date value);

  FilterBuilder greaterThan(String key, LocalDate value);

  FilterBuilder greaterThan(String key, LocalDateTime value);

  <T extends Number> FilterBuilder greaterThanOrEquals(String key, T value);

  FilterBuilder greaterThanOrEquals(String key, Date value);

  FilterBuilder greaterThanOrEquals(String key, LocalDate value);

  FilterBuilder greaterThanOrEquals(String key, LocalDateTime value);

  <T extends Number> FilterBuilder lessThan(String key, T value);

  FilterBuilder lessThan(String key, Date value);

  FilterBuilder lessThan(String key, LocalDate value);

  FilterBuilder lessThan(String key, LocalDateTime value);

  <T extends Number> FilterBuilder lessThanOrEquals(String key, T value);

  FilterBuilder lessThanOrEquals(String key, Date value);

  FilterBuilder lessThanOrEquals(String key, LocalDate value);

  FilterBuilder lessThanOrEquals(String key, LocalDateTime value);

  FilterBuilder endsWith(String key, String value);

  FilterBuilder startsWith(String key, String value);

  FilterBuilder contains(String key, String value);

  FilterBuilder present(String key);

  FilterBuilder not(FilterExpression fe);

  default FilterBuilder not(Filter filter) {
    return not(filter.getExpression());
  }

  default FilterBuilder not(UnaryOperator<FilterBuilder> filter) {
    return not(filter.apply(create()).build());
  }

  FilterBuilder attributeHas(String attribute, FilterExpression filter);

  default FilterBuilder attributeHas(String attribute, UnaryOperator<FilterBuilder> filter) {
    return attributeHas(attribute, filter.apply(create()).build());
  }

  default FilterBuilder attributeHas(String attribute, Filter filter) {
    return attributeHas(attribute, filter.getExpression());
  }

  FilterExpression filter();

  Filter build();

  static FilterBuilder create() {
    return new FilterComparisonFilterBuilder();
  }
}
