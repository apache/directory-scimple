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
