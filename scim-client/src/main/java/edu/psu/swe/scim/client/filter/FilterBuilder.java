package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import edu.psu.swe.scim.common.ScimUtils;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.LogicalOperator;

public class FilterBuilder {
  
  private static final String QUOTE = "\"";
  private static final String NULL = "null";
  private static final String SPACE = " ";
  private static final String OPEN_PAREN = "(";
  private static final String CLOSE_PAREN = ")";
  
  private StringBuilder filter = new StringBuilder();
  
  public FilterBuilder equalTo(String key, String value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterBuilder equalTo(String key, Boolean value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder equalTo(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE);
    return this;
  }
  
  public FilterBuilder equalTo(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE);
    return this;
  }
  
  public FilterBuilder equalTo(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterBuilder equalTo(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder equalNull(String key) {
    filter.append(key).append(SPACE).append(CompareOperator.EQ.name()).append(SPACE).append(NULL);
    return this;
  }
  
  public FilterBuilder endsWith(String key, String value) {
    filter.append(key).append(SPACE).append(CompareOperator.EW.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterBuilder startsWith(String key, String value) {
    filter.append(key).append(SPACE).append(CompareOperator.SW.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterBuilder notEqual(String key, String value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public FilterBuilder notEqual(String key, Boolean value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder notEqual(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE);
    return this;
  }
  
  public FilterBuilder notEqual(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE);
    return this;
  }
  
  public FilterBuilder notEqual(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterBuilder notEqual(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder notEqualNull(String key) {
    filter.append(key).append(SPACE).append(CompareOperator.NE.name()).append(SPACE).append(NULL);
    return this;
  }
  
  public FilterBuilder contains(String key, String value) {
    filter.append(key).append(SPACE).append(CompareOperator.CO.name()).append(SPACE).append(QUOTE).append(value).append(QUOTE);
    return this;
  }
  
  public <T extends Number> FilterBuilder greaterThan(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder greaterThan(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder greaterThan(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder greaterThan(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.GT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterBuilder greaterThanOrEquals(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(value.toString());
    return this;
  }

  public FilterBuilder greaterThanOrEquals(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder greaterThanOrEquals(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder greaterThanOrEquals(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.GE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterBuilder lessThan(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder lessThan(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder lessThan(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder lessThan(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.LT.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public <T extends Number> FilterBuilder lessThanOrEquals(String key, T value) {
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(value.toString());
    return this;
  }
  
  public FilterBuilder lessThanOrEquals(String key, Date value) {
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder lessThanOrEquals(String key, LocalDate value) {
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder lessThanOrEquals(String key, LocalDateTime value) {
    filter.append(key).append(SPACE).append(CompareOperator.LE.name()).append(SPACE).append(QUOTE).append(ScimUtils.toDateTimeString(value)).append(QUOTE).append(SPACE);
    return this;
  }
  
  public FilterBuilder and(AttributeComparisonExpression ex1, AttributeComparisonExpression ex2) {
    
    filter.append(OPEN_PAREN);
    buildExpression(ex1);
    filter.append(LogicalOperator.AND.name());
    buildExpression(ex2);
    filter.append(CLOSE_PAREN);    
    return this;
  }
  
  public FilterBuilder and() {
    filter.append(SPACE).append(LogicalOperator.AND.name()).append(SPACE);
    
    return this;
  }
  
  public FilterBuilder or(AttributeComparisonExpression ex1, AttributeComparisonExpression ex2) {
     
     filter.append(OPEN_PAREN);
     buildExpression(ex1);
 
     filter.append(SPACE).append(LogicalOperator.OR.name()).append(SPACE);
     
     buildExpression(ex2);
     filter.append(CLOSE_PAREN);
         
     return this;
  }
  
  public FilterBuilder or() {
    filter.append(SPACE).append(LogicalOperator.OR.name()).append(SPACE);
    
    return this;
  }
  
  public String build() throws UnsupportedEncodingException {
    String filterString = filter.toString().trim();
    return URLEncoder.encode(filterString, "UTF-8").replace("+", "%20");
  }
  
  private FilterBuilder buildExpression(AttributeComparisonExpression ex) {
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
