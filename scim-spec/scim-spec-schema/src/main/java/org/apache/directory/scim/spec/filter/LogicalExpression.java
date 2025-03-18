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

public class LogicalExpression implements FilterExpression, ValueFilterExpression {

  FilterExpression left;
  LogicalOperator operator;
  FilterExpression right;

  public LogicalExpression() {}

  public LogicalExpression(FilterExpression left, LogicalOperator operator, FilterExpression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

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

  public FilterExpression getLeft() {
    return this.left;
  }

  public LogicalExpression setLeft(FilterExpression left) {
    this.left = left;
    return this;
  }

  public LogicalOperator getOperator() {
    return this.operator;
  }

  public LogicalExpression setOperator(LogicalOperator operator) {
    this.operator = operator;
    return this;
  }

  public FilterExpression getRight() {
    return this.right;
  }

  public LogicalExpression setRight(FilterExpression right) {
    this.right = right;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof LogicalExpression)) return false;
    final LogicalExpression other = (LogicalExpression) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$left = this.getLeft();
    final Object other$left = other.getLeft();
    if (this$left == null ? other$left != null : !this$left.equals(other$left)) return false;
    final Object this$operator = this.getOperator();
    final Object other$operator = other.getOperator();
    if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) return false;
    final Object this$right = this.getRight();
    final Object other$right = other.getRight();
    if (this$right == null ? other$right != null : !this$right.equals(other$right)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof LogicalExpression;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $left = this.getLeft();
    result = result * PRIME + ($left == null ? 43 : $left.hashCode());
    final Object $operator = this.getOperator();
    result = result * PRIME + ($operator == null ? 43 : $operator.hashCode());
    final Object $right = this.getRight();
    result = result * PRIME + ($right == null ? 43 : $right.hashCode());
    return result;
  }

  public String toString() {
    return "LogicalExpression(left=" + this.getLeft() + ", operator=" + this.getOperator() + ", right=" + this.getRight() + ")";
  }
}
