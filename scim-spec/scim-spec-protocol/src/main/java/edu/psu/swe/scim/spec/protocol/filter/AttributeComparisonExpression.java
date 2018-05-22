package edu.psu.swe.scim.spec.protocol.filter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.Value;

@Value
public class AttributeComparisonExpression implements FilterExpression, ValueFilterExpression {
  AttributeReference attributePath;
  CompareOperator operation;
  Object compareValue;
  
  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";
  private static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS";
  private static final String QUOTE = "\"";

  @Override
  public String toFilter() {
    String filter = this.attributePath.getFullyQualifiedAttributeName() + " " + this.operation + " " + this.createCompareValueString();

    return filter;
  }

  @Override
  public String toUnqualifiedFilter() {
    String subAttributeName = this.attributePath.getSubAttributeName();
    String unqualifiedAttributeName = subAttributeName != null ? subAttributeName : this.attributePath.getAttributeName();

    return unqualifiedAttributeName + " " + operation + " " + this.createCompareValueString();
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

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    String subAttributeName = this.attributePath.getAttributeName();
    this.attributePath.setAttributeName(parentAttributeName);
    this.attributePath.setSubAttributeName(subAttributeName);
  }

  private String createCompareValueString() {
    String compareValueString;

    if (this.compareValue == null) {
      compareValueString = "null";
    } else if (this.compareValue instanceof String) {
      String escaped = StringEscapeUtils.escapeEcmaScript((String) this.compareValue);

      compareValueString = QUOTE + escaped + QUOTE;
    } else if (this.compareValue instanceof Date) {
      compareValueString = QUOTE + toDateTimeString((Date) this.compareValue) + QUOTE;
    } else if (this.compareValue instanceof LocalDate) {
      compareValueString = QUOTE + toDateString((LocalDate) this.compareValue) + QUOTE;
    } else if (this.compareValue instanceof LocalDateTime) {
      compareValueString = QUOTE + toDateTimeString((LocalDateTime) this.compareValue) + QUOTE;
    } else {
      compareValueString = this.compareValue.toString();
    }
    return compareValueString;
  }
}
