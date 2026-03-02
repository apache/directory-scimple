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

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;

import java.io.Serial;

public final class ValuePathExpression implements FilterExpression {

  @Serial
  private static final long serialVersionUID = 2615135752981305135L;
  // urn:parentAttribute[attributeExpression].subAttribute

  AttributeReference attributePath;
  FilterExpression attributeExpression;

  public ValuePathExpression() {}

  public ValuePathExpression(AttributeReference attributePath) {
    this.attributePath = attributePath;
  }

  public ValuePathExpression(AttributeReference attributePath, FilterExpression attributeExpression) {
    this.attributePath = attributePath;
    this.attributeExpression = attributeExpression;
  }

  public static ValuePathExpression fromFilterExpression(AttributeReference attrRef, FilterExpression attributeExpression) {
    ValuePathExpression vpe = new ValuePathExpression(attrRef, attributeExpression);

    return vpe;
  }

  public static ValuePathExpression fromFilterExpression(String attribute, FilterExpression expression) {
    AttributeReference attributeReference = new AttributeReference(attribute);

    return fromFilterExpression(attributeReference, expression);
  }

  @Override
  public String toFilter() {
    String filter;

    if (this.attributeExpression != null) {
      String subAttributeName = this.attributePath.getSubAttributeName();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (subAttributeName != null) {
        String base = this.attributePath.getAttributeBase();
        filter = base + "[" + attributeExpressionFilter + "]." + subAttributeName;
      } else {
        String attribute = this.attributePath.getFullyQualifiedAttributeName();
        filter = attribute + "[" + attributeExpressionFilter + "]";
      }
    } else {
      filter = this.attributePath.getFullyQualifiedAttributeName();
    }
    return filter;
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    String subAttributeName = this.attributePath.getAttributeName();
    this.attributePath.setAttributeName(parentAttributeName);
    this.attributePath.setSubAttributeName(subAttributeName);
    this.attributeExpression.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toUnqualifiedFilter() {
    String filter;

    if (this.attributeExpression != null) {
      String attributeName = this.attributePath.getAttributeName();
      String subAttributeName = this.attributePath.getSubAttributeName();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (subAttributeName != null) {
        filter = attributeName + "[" + attributeExpressionFilter + "]." + subAttributeName;
      } else {
        filter = attributeName + "[" + attributeExpressionFilter + "]";
      }
    } else {
      String subAttributeName = this.attributePath.getSubAttributeName();
      filter = this.attributePath.getAttributeName() + (subAttributeName != null ? "." + subAttributeName : "");
    }
    return filter;
  }

  public AttributeReference getAttributePath() {
    return this.attributePath;
  }

  public ValuePathExpression setAttributePath(AttributeReference attributePath) {
    this.attributePath = attributePath;
    return this;
  }

  public FilterExpression getAttributeExpression() {
    return this.attributeExpression;
  }

  public ValuePathExpression setAttributeExpression(FilterExpression attributeExpression) {
    this.attributeExpression = attributeExpression;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ValuePathExpression other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$attributePath = this.getAttributePath();
    final Object other$attributePath = other.getAttributePath();
    if (this$attributePath == null ? other$attributePath != null : !this$attributePath.equals(other$attributePath))
      return false;
    final Object this$attributeExpression = this.getAttributeExpression();
    final Object other$attributeExpression = other.getAttributeExpression();
    if (this$attributeExpression == null ? other$attributeExpression != null : !this$attributeExpression.equals(other$attributeExpression))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ValuePathExpression;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $attributePath = this.getAttributePath();
    result = result * PRIME + ($attributePath == null ? 43 : $attributePath.hashCode());
    final Object $attributeExpression = this.getAttributeExpression();
    result = result * PRIME + ($attributeExpression == null ? 43 : $attributeExpression.hashCode());
    return result;
  }

  public String toString() {
    return "ValuePathExpression(attributePath=" + this.getAttributePath() + ", attributeExpression=" + this.getAttributeExpression() + ")";
  }
}
