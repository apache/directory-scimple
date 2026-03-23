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

import java.io.Serializable;
import java.util.function.Function;

public sealed interface FilterExpression extends Serializable
  permits AttributeComparisonExpression, AttributePresentExpression, GroupExpression, LogicalExpression, ValuePathExpression {
  
  String toFilter();

  void setAttributePath(String urn, String parentAttributeName);

  String toUnqualifiedFilter();

  default <U> U map(Function<? super FilterExpression, U> mapper) {
    return mapper.apply(this);
  }

  /**
   * Dispatches to the appropriate {@link FilterExpressionVisitor} method based on
   * this expression's concrete type.
   *
   * @param visitor the visitor to dispatch to
   * @param <R>     the result type
   * @return the result of visiting this expression
   */
  default <R> R accept(FilterExpressionVisitor<R> visitor) {
    if (this instanceof AttributeComparisonExpression e) return visitor.visit(e);
    if (this instanceof AttributePresentExpression e) return visitor.visit(e);
    if (this instanceof LogicalExpression e) return visitor.visit(e);
    if (this instanceof GroupExpression e) return visitor.visit(e);
    if (this instanceof ValuePathExpression e) return visitor.visit(e);
    throw new IllegalStateException("Unknown FilterExpression type: " + getClass().getName());
  }
}
