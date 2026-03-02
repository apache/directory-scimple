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

public final class AttributePresentExpression implements FilterExpression, ValueFilterExpression {
  @Serial
  private static final long serialVersionUID = -4491412651236977273L;
  private final AttributeReference attributePath;

  public AttributePresentExpression(AttributeReference attributePath) {
    this.attributePath = attributePath;
  }

  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + " PR";
  }

  @Override
  public String toUnqualifiedFilter() {
    String subAttributeName = this.attributePath.getSubAttributeName();
    String attributeName = subAttributeName != null ? subAttributeName : this.attributePath.getAttributeName();

    return attributeName + " PR";
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    String subAttributeName = this.attributePath.getAttributeName();
    this.attributePath.setAttributeName(parentAttributeName);
    this.attributePath.setSubAttributeName(subAttributeName);
  }

  public AttributeReference getAttributePath() {
    return this.attributePath;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AttributePresentExpression)) return false;
    final AttributePresentExpression other = (AttributePresentExpression) o;
    final Object this$attributePath = this.getAttributePath();
    final Object other$attributePath = other.getAttributePath();
    if (this$attributePath == null ? other$attributePath != null : !this$attributePath.equals(other$attributePath))
      return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $attributePath = this.getAttributePath();
    result = result * PRIME + ($attributePath == null ? 43 : $attributePath.hashCode());
    return result;
  }

  public String toString() {
    return "AttributePresentExpression(attributePath=" + this.getAttributePath() + ")";
  }
}
