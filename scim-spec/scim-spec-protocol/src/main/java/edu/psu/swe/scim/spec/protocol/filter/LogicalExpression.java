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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;
  
  @Override
  public String toFilter() {
    boolean leftParens = left instanceof LogicalExpression;
    boolean rightParens = right instanceof LogicalExpression;

    String leftString = (leftParens ? "(" : "") + left.toFilter() + (leftParens ? ")" : "");
    String rightString = (rightParens ? "(" : "") + right.toFilter() + (rightParens ? ")" : "");
    
    return leftString + " " + operator + " " + rightString;
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.left.setAttributePath(urn, parentAttributeName);
    this.right.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toUnqualifiedFilter() {
    boolean leftParens = this.left instanceof LogicalExpression;
    boolean rightParens = this.right instanceof LogicalExpression;

    String leftString = (leftParens ? "(" : "") + left.toUnqualifiedFilter() + (leftParens ? ")" : "");
    String rightString = (rightParens ? "(" : "") + right.toUnqualifiedFilter() + (rightParens ? ")" : "");

    return leftString + " " + operator + " " + rightString;
  }
}
