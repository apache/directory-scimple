package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import edu.psu.swe.scim.common.ScimUtils;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.GroupExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalOperator;

public class FilterClient {
  
  private static final String QUOTE = "\"";
  private static final String NULL = "null";
  private static final String SPACE = " ";
  private static final String OPEN_PAREN = "(";
  private static final String CLOSE_PAREN = ")";
  private static final String NOT = "NOT";
  
  private StringBuilder filter = new StringBuilder();
  private FilterExpression filterExpression = null;
  
  private static class Builder {
    
  }
  
  public FilterClient equalTo(String key, String value) {
    
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterClient equalTo(String key, Boolean value) {
    
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient equalTo(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateTimeString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public FilterClient equalTo(String key, LocalDate value) {
    
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public FilterClient equalTo(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateTimeString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterClient equalTo(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient equalNull(String key) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EQ, null);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(NULL);
    return this;
  }
  
  public FilterClient endsWith(String key, String value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.EW, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.EW.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterClient startsWith(String key, String value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.SW, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.SW.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterClient notEqual(String key, String value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterClient notEqual(String key, Boolean value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient notEqual(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateTimeString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
      
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public FilterClient notEqual(String key, LocalDate value) {
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public FilterClient notEqual(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    String dateString = ScimUtils.toDateTimeString(value);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(dateString).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterClient notEqual(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient notEqualNull(String key) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.NE, null);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(NULL);
    return this;
  }
  
  public FilterClient contains(String key, String value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.CO, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.CO.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterClient greaterThan(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient greaterThan(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient greaterThan(String key, LocalDate value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient greaterThan(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterClient greaterThanOrEquals(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(value.toString());
    return this;
  }

  public FilterClient greaterThanOrEquals(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient greaterThanOrEquals(String key, LocalDate value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient greaterThanOrEquals(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.GE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterClient lessThan(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient lessThan(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient lessThan(String key, LocalDate value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient lessThan(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LT, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterClient lessThanOrEquals(String key, T value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterClient lessThanOrEquals(String key, Date value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient lessThanOrEquals(String key, LocalDate value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient lessThanOrEquals(String key, LocalDateTime value) {
    AttributeReference ar = new AttributeReference(key);
    FilterExpression filterExpression = new AttributeComparisonExpression(ar, CompareOperator.LE, value);
    
    handleComparisonExpression(filterExpression);
    
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterClient not(AttributeComparisonExpression expression) {
    filter.append(SPACE).append(NOT).append(OPEN_PAREN);
    buildExpression(expression);
    filter.append(CLOSE_PAREN);
    return this;
  }
  
  public FilterClient not(AttributeComparisonExpression ex1, LogicalOperator operator, AttributeComparisonExpression ex2) {
    filter.append(SPACE).append(NOT).append(OPEN_PAREN);
    buildExpression(ex1);
    filter.append(operator.name());
    buildExpression(ex2);
    filter.append(CLOSE_PAREN);
    return this;
  }
  
  public FilterClient and() {
    if (filterExpression == null) {
      throw new IllegalStateException();
    }
    
    LogicalExpression logicalExpression = new LogicalExpression();
    logicalExpression.setLeft(filterExpression);
    filterExpression = logicalExpression;
    
    return this;
  }
  
  public FilterClient and(FilterExpression fe1, FilterExpression fe2) {
     LogicalExpression logicalExpression = new LogicalExpression();
     logicalExpression.setLeft(fe1);
     logicalExpression.setRight(fe2);
     logicalExpression.setOperator(LogicalOperator.AND);
     
     return handleLogicalExpression(logicalExpression);
  }
  
  public FilterClient or() {
    if (filterExpression == null) {
      throw new IllegalStateException();
    }
    
    LogicalExpression logicalExpression = new LogicalExpression();
    logicalExpression.setLeft(filterExpression);
    filterExpression = logicalExpression;
    
    return this;
  }
  
  public FilterClient or(FilterExpression fe1, FilterExpression fe2) {
    LogicalExpression logicalExpression = new LogicalExpression();
    logicalExpression.setLeft(fe1);
    logicalExpression.setRight(fe2);
    logicalExpression.setOperator(LogicalOperator.OR);
    
    return handleLogicalExpression(logicalExpression);
 }
  
  public FilterClient and(AttributeComparisonExpression ex1, AttributeComparisonExpression ex2) {
        
    filter.append(OPEN_PAREN);
    buildExpression(ex1);
    filter.append(LogicalOperator.AND.name());
    buildExpression(ex2);
    filter.append(CLOSE_PAREN);    
    return this;
  }
  
//  public FilterBuilder and() {
//    filter.append(SPACE).append(LogicalOperator.AND.name()).append(SPACE);
//    
//    return this;
//  }
  
  public FilterClient or(AttributeComparisonExpression ex1, AttributeComparisonExpression ex2) {
     
     filter.append(OPEN_PAREN);
     buildExpression(ex1);
 
     filter.append(SPACE).append(LogicalOperator.OR.name()).append(SPACE);
     
     buildExpression(ex2);
     filter.append(CLOSE_PAREN);
         
     return this;
  }
  
//  public FilterClient or() {
//    filter.append(SPACE).append(LogicalOperator.OR.name()).append(SPACE);
//    
//    return this;
//  }
  
  public String build() throws UnsupportedEncodingException {
    String filterString = filter.toString().trim();
    return URLEncoder.encode(filterString, "UTF-8").replace("+", "%20");
  }
  
  private void handleComparisonExpression(FilterExpression expression) {
    
    if (filterExpression == null) {
      filterExpression = expression;
    } else {
        if (!(filterExpression instanceof LogicalExpression)) {
          throw new IllegalStateException();
        }
        
        LogicalExpression le = (LogicalExpression)filterExpression;
        le.setLeft(filterExpression);
    }
  }
  
  private FilterClient handleLogicalExpression(LogicalExpression expression) {
    
    if (filterExpression == null) {
      filterExpression = expression;
    } else if (filterExpression instanceof AttributeComparisonExpression) {
      LogicalExpression newRoot = new LogicalExpression();
      newRoot.setLeft(filterExpression);
      newRoot.setRight(expression);
      filterExpression = newRoot;
    } else if (filterExpression instanceof LogicalExpression) {
      LogicalExpression le = (LogicalExpression) filterExpression;
      
      if (le.getLeft() == null) {
        le.setLeft(expression);
      } else if (le.getRight() == null) {
        le.setRight(expression);
      } else {
        LogicalExpression newRoot = new LogicalExpression();
        newRoot.setLeft(expression);
        filterExpression = newRoot;
      }
    } else if (filterExpression instanceof GroupExpression) {
      LogicalExpression newRoot = new LogicalExpression();
      newRoot.setLeft(filterExpression);
      newRoot.setRight(expression);
      filterExpression = newRoot;
    }
    
    return this;
  }
  
  private FilterClient buildExpression(AttributeComparisonExpression ex) {
    filter.append(ex.getAttributePath().getFullAttributeName())
    .append(SPACE)
    .append(ex.getOperation().name())
    .append(SPACE)
    .append(QUOTE)
    .append(ex.getCompareValue().toString())
    .append(QUOTE);
    
    return this;
  }
}
