/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.filter;

import java.io.Serial;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;

public final class AttributeComparisonExpression implements FilterExpression, ValueFilterExpression {
  @Serial
  private static final long serialVersionUID = -2865840428089850575L;
  private final AttributeReference attributePath;
  private final CompareOperator operation;
  private final Object compareValue;

  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";
  private static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS";
  private static final String QUOTE = "\"";

  public AttributeComparisonExpression(AttributeReference attributePath, CompareOperator operation, Object compareValue) {
    this.attributePath = attributePath;
    this.operation = operation;
    this.compareValue = compareValue;
  }

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
      compareValueString = QUOTE + this.compareValue + QUOTE;
    } else if (this.compareValue instanceof Date date1) {
      compareValueString = QUOTE + toDateTimeString(date1) + QUOTE;
    } else if (this.compareValue instanceof LocalDate date) {
      compareValueString = QUOTE + toDateString(date) + QUOTE;
    } else if (this.compareValue instanceof LocalDateTime time) {
      compareValueString = QUOTE + toDateTimeString(time) + QUOTE;
    } else {
      compareValueString = this.compareValue.toString();
    }
    return compareValueString;
  }

  public AttributeReference getAttributePath() {
    return this.attributePath;
  }

  public CompareOperator getOperation() {
    return this.operation;
  }

  public Object getCompareValue() {
    return this.compareValue;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AttributeComparisonExpression)) return false;
    final AttributeComparisonExpression other = (AttributeComparisonExpression) o;
    final Object this$attributePath = this.getAttributePath();
    final Object other$attributePath = other.getAttributePath();
    if (this$attributePath == null ? other$attributePath != null : !this$attributePath.equals(other$attributePath))
      return false;
    final Object this$operation = this.getOperation();
    final Object other$operation = other.getOperation();
    if (this$operation == null ? other$operation != null : !this$operation.equals(other$operation)) return false;
    final Object this$compareValue = this.getCompareValue();
    final Object other$compareValue = other.getCompareValue();
    if (this$compareValue == null ? other$compareValue != null : !this$compareValue.equals(other$compareValue))
      return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $attributePath = this.getAttributePath();
    result = result * PRIME + ($attributePath == null ? 43 : $attributePath.hashCode());
    final Object $operation = this.getOperation();
    result = result * PRIME + ($operation == null ? 43 : $operation.hashCode());
    final Object $compareValue = this.getCompareValue();
    result = result * PRIME + ($compareValue == null ? 43 : $compareValue.hashCode());
    return result;
  }

  public String toString() {
    return "AttributeComparisonExpression(attributePath=" + this.getAttributePath() + ", operation=" + this.getOperation() + ", compareValue=" + this.getCompareValue() + ")";
  }
}
