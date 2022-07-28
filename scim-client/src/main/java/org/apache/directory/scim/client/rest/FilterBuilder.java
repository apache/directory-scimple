package org.apache.directory.scim.client.rest;

import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.search.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface FilterBuilder {

  FilterBuilder and();

  FilterBuilder and(FilterExpression fe1);

  FilterBuilder and(FilterExpression fe1, FilterExpression fe2);

  FilterBuilder or();

  FilterBuilder or(FilterExpression fe1);

  FilterBuilder or(FilterExpression fe1, FilterExpression fe2);

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

  FilterBuilder not(FilterExpression filter);

  FilterBuilder attributeHas(String attribute, FilterExpression filter) throws FilterParseException;

  FilterExpression filter();

  Filter build();

  static FilterBuilder create() {
    return new ComparisonBuilder();
  }
}
