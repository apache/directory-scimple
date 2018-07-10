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

package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class AttributePresentExpression implements FilterExpression, ValueFilterExpression {
  AttributeReference attributePath;

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
}
