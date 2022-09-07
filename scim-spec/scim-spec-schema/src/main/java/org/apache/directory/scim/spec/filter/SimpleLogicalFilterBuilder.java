package org.apache.directory.scim.spec.filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class SimpleLogicalFilterBuilder implements FilterBuilder {

  protected FilterExpression filterExpression;

  @Override
  public FilterBuilder and() {
    if (filterExpression == null) {
      throw new IllegalStateException();
    }

    LogicalExpression logicalExpression = new LogicalExpression();
    logicalExpression.setLeft(groupIfNeeded(filterExpression));
    logicalExpression.setOperator(LogicalOperator.AND);
    filterExpression = logicalExpression;

    return this;
  }

  @Override
  public FilterBuilder or() {
    if (filterExpression == null) {
      throw new IllegalStateException();
    }

    LogicalExpression logicalExpression = new LogicalExpression();
    logicalExpression.setLeft(groupIfNeeded(filterExpression));
    logicalExpression.setOperator(LogicalOperator.OR);
    filterExpression = logicalExpression;

    return this;
  }

  static FilterExpression groupIfNeeded(FilterExpression filterExpression) {
    return (filterExpression instanceof LogicalExpression)
      ? new GroupExpression(false, filterExpression)
      : filterExpression;
  }

  protected FilterBuilder handleLogicalExpression(LogicalExpression expression, LogicalOperator operator) {
    log.info("In handleLogicalExpression");
    if (filterExpression == null) {
      filterExpression = expression;
    } else if (filterExpression instanceof AttributeComparisonExpression) {
      log.info("Adding a logical expression as the new root");

      log.info("Setting as left: " + filterExpression.toFilter());
      expression.setLeft(filterExpression);
      log.info("Setting as right: " + expression.toFilter());

      filterExpression = expression;
    } else if (filterExpression instanceof LogicalExpression) {
      log.info("filter exression is a logical expression");
      LogicalExpression le = (LogicalExpression) filterExpression;

      if (le.getLeft() == null) {
        log.info("Setting left to: " + expression.toFilter());
        le.setLeft(expression);
      } else if (le.getRight() == null) {
        log.info("Setting right to: " + expression.toFilter());
        le.setRight(expression);
      } else {
        log.info("The current base is complete, raising up one level");
        LogicalExpression newRoot = new LogicalExpression();
        log.info("Setting left to: " + expression);
        newRoot.setLeft(expression);
        filterExpression = newRoot;
      }
    } else if (filterExpression instanceof GroupExpression) {
      log.info("Found group expression");
      LogicalExpression newRoot = new LogicalExpression();
      newRoot.setLeft(filterExpression);
      newRoot.setRight(expression);
      newRoot.setOperator(operator);
      filterExpression = newRoot;
    }

    log.info("New filter expression: " + filterExpression.toFilter());

    return this;
  }

  protected void handleComparisonExpression(FilterExpression expression) {

    if (expression == null) {
      log.error("*** in handle comparison ---> expression == null");
    }

    if (filterExpression == null) {
      filterExpression = expression;
    } else {
      if (!(filterExpression instanceof LogicalExpression)) {
        throw new IllegalStateException();
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
