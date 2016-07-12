package edu.psu.swe.scim.spec.protocol.filter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class AttributeComparisonExpression implements AttributeExpression, ValueFilterExpression {
  AttributeReference attributePath;
  CompareOperator operation;
  Object compareValue;
  
  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";
  private static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS";
  private static final String QUOTE = "\"";
  
  @Override
  public String toFilter() {
    String compareValueString = null;
    if (compareValue == null) {
      compareValueString = "null";
    } else if (compareValue instanceof String) {
      compareValueString = QUOTE + compareValue + QUOTE;
    } else if (compareValue instanceof Number) {
      compareValueString = compareValue.toString();
    } else if (compareValue instanceof Date) {
      compareValueString = QUOTE + toDateTimeString((Date) compareValue) + QUOTE;
    } else if (compareValue instanceof LocalDate) {
      compareValueString = QUOTE + toDateString((LocalDate) compareValue) + QUOTE;
    } else if (compareValue instanceof LocalDateTime) {
      compareValueString = QUOTE + toDateTimeString((LocalDateTime) compareValue) + QUOTE;
    }

    return attributePath.getFullyQualifiedAttributeName() + " " + operation + " " + compareValueString;
  }
  public static String toDateString(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
    return dateFormat.format(date);
  }
  
  public static String toDateTimeString(Date date) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ISO_8601_DATE_TIME_FORMAT);
    return dateTimeFormat.format(date);
  }
  
  public static String toDateString(LocalDate ld) {
    return ld.format(DateTimeFormatter.ISO_DATE);
  }
  
  public static String toDateTimeString(LocalDateTime ldt) {
    return ldt.format(DateTimeFormatter.ISO_DATE_TIME);
  }
}
