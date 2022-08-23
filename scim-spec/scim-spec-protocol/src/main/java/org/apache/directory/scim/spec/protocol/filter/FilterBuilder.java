package org.apache.directory.scim.spec.protocol.filter;

import org.apache.directory.scim.spec.protocol.search.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.UnaryOperator;

public interface FilterBuilder {

  FilterBuilder and();

  FilterBuilder and(FilterExpression fe1);

  default FilterBuilder and(UnaryOperator<FilterBuilder> filter) {
    return and(filter.apply(FilterBuilder.create()).build());
  }

  default FilterBuilder and(Filter filter) {
    return and(filter.getExpression());
  }

  FilterBuilder and(FilterExpression left, FilterExpression right);

  default FilterBuilder and(Filter left, Filter right) {
    return and(left.getExpression(), right.getExpression());
  }

  default FilterBuilder and(UnaryOperator<FilterBuilder> left, UnaryOperator<FilterBuilder> right) {
    return and(left.apply(FilterBuilder.create()).build(), right.apply(FilterBuilder.create()).build());
  }

  FilterBuilder or();

  FilterBuilder or(FilterExpression fe1);

  default FilterBuilder or(Filter filter) {
    return or(filter.getExpression());
  }

  default FilterBuilder or(UnaryOperator<FilterBuilder> filter) {
    return or(filter.apply(FilterBuilder.create()).build());
  }

  FilterBuilder or(FilterExpression left, FilterExpression right);

  default FilterBuilder or(Filter left, Filter right) {
    return or(left.getExpression(), right.getExpression());
  }
  default FilterBuilder or(UnaryOperator<FilterBuilder> left, UnaryOperator<FilterBuilder> right) {
    return or(left.apply(FilterBuilder.create()).build(), right.apply(FilterBuilder.create()).build());
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
    return not(filter.apply(FilterBuilder.create()).build());
  }

  FilterBuilder attributeHas(String attribute, FilterExpression filter);

  default FilterBuilder attributeHas(String attribute, UnaryOperator<FilterBuilder> filter) {
    return attributeHas(attribute, filter.apply(FilterBuilder.create()).build());
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
