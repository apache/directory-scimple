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
