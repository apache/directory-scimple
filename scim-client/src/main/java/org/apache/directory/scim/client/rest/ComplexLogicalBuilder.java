package org.apache.directory.scim.client.rest;

import org.apache.directory.scim.spec.protocol.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.LogicalExpression;
import org.apache.directory.scim.spec.protocol.filter.LogicalOperator;

abstract class ComplexLogicalBuilder extends SimpleLogicalBuilder {

    @Override
    public FilterBuilder or(FilterExpression fe1) {
      if (filterExpression instanceof AttributeComparisonExpression) {
        LogicalExpression logicalExpression = new LogicalExpression();
        logicalExpression.setLeft(groupIfNeeded(filterExpression));
        logicalExpression.setRight(groupIfNeeded(fe1));
        logicalExpression.setOperator(LogicalOperator.OR);
        filterExpression = logicalExpression;
        return this;
      }
      
      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(fe1);
      logicalExpression.setOperator(LogicalOperator.OR);

      return handleLogicalExpression(logicalExpression, LogicalOperator.OR);
    }
    
    @Override
    public FilterBuilder or(FilterExpression fe1, FilterExpression fe2) {
      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(groupIfNeeded(fe1));
      logicalExpression.setRight(groupIfNeeded(fe2));
      logicalExpression.setOperator(LogicalOperator.OR);

      return handleLogicalExpression(logicalExpression, LogicalOperator.OR);
    }

    @Override
    public FilterBuilder and(FilterExpression fe1, FilterExpression fe2) {
      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(groupIfNeeded(fe1));
      logicalExpression.setRight(groupIfNeeded(fe2));
      logicalExpression.setOperator(LogicalOperator.AND);

      return handleLogicalExpression(logicalExpression, LogicalOperator.AND);
    }
    
    @Override
    public FilterBuilder and(FilterExpression fe1) {
      if (filterExpression instanceof AttributeComparisonExpression) {
        LogicalExpression logicalExpression = new LogicalExpression();
        logicalExpression.setLeft(filterExpression);
        logicalExpression.setRight(groupIfNeeded(fe1));
        logicalExpression.setOperator(LogicalOperator.AND);
        filterExpression = logicalExpression;
        return this;
      }
      
      LogicalExpression logicalExpression = new LogicalExpression();
      logicalExpression.setLeft(fe1);
      logicalExpression.setOperator(LogicalOperator.AND);

      return handleLogicalExpression(logicalExpression, LogicalOperator.AND);
    }
  }
