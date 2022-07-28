package org.apache.directory.scim.spec.protocol.filter;

import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

class FilterComparisonFilterBuilder extends ComplexLogicalFilterBuilder {

    @Override
    public FilterBuilder equalTo(String key, String value) {

      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder equalTo(String key, Boolean value) {

      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder equalTo(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder equalTo(String key, LocalDate value) {

      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder equalTo(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder equalTo(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder equalNull(String key) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, null);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqual(String key, String value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqual(String key, Boolean value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqual(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqual(String key, LocalDate value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqual(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder notEqual(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder notEqualNull(String key) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, null);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder endsWith(String key, String value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EW, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder startsWith(String key, String value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.SW, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder contains(String key, String value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.CO, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder greaterThan(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThan(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThan(String key, LocalDate value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThan(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder greaterThanOrEquals(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThanOrEquals(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThanOrEquals(String key, LocalDate value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder greaterThanOrEquals(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder lessThan(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThan(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThan(String key, LocalDate value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThan(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public <T extends Number> FilterBuilder lessThanOrEquals(String key, T value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThanOrEquals(String key, Date value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThanOrEquals(String key, LocalDate value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder lessThanOrEquals(String key, LocalDateTime value) {
      AttributeReference ar = new AttributeReference(key);
      FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);

      handleComparisonExpression(filterExpression);

      return this;
    }

    @Override
    public FilterBuilder not(FilterExpression expression) {
      GroupExpression groupExpression = new GroupExpression();

      groupExpression.setNot(true);
      groupExpression.setFilterExpression(expression);

      handleComparisonExpression(groupExpression);

      return this;
    }

    @Override
    public FilterBuilder attributeHas(String attribute, FilterExpression expression) throws FilterParseException {
      handleComparisonExpression(ValuePathExpression.fromFilterExpression(attribute, expression));

      return this;
    }
  }
