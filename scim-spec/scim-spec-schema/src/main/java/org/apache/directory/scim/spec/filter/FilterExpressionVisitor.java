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

/**
 * Visitor interface for traversing a SCIM {@link FilterExpression} tree.
 *
 * <p>Since {@link FilterExpression} is a sealed interface with exactly five permitted
 * subtypes, implementations of this visitor are guaranteed to handle all possible
 * expression types at compile time.</p>
 *
 * <p>Usage: call {@link FilterExpression#accept(FilterExpressionVisitor)} to dispatch
 * to the appropriate {@code visit} method based on the expression's concrete type.</p>
 *
 * <p>Example — translating SCIM filters to SQL WHERE clauses:</p>
 * <pre>
 * class SqlFilterVisitor implements FilterExpressionVisitor&lt;String&gt; {
 *   &#64;Override
 *   public String visit(AttributeComparisonExpression expr) {
 *     return expr.getAttributePath().getAttributeName() + " = ?";
 *   }
 *   // ... other visit methods
 * }
 * String sql = filter.getExpression().accept(new SqlFilterVisitor());
 * </pre>
 *
 * @param <R> the result type produced by visiting each expression node
 */
public interface FilterExpressionVisitor<R> {

  /**
   * Visits an attribute comparison expression (eq, ne, co, sw, ew, gt, ge, lt, le).
   *
   * @param expr the comparison expression
   * @return the visitor result
   */
  R visit(AttributeComparisonExpression expr);

  /**
   * Visits an attribute presence expression (pr).
   *
   * @param expr the presence expression
   * @return the visitor result
   */
  R visit(AttributePresentExpression expr);

  /**
   * Visits a logical expression (and, or).
   *
   * @param expr the logical expression containing left and right operands
   * @return the visitor result
   */
  R visit(LogicalExpression expr);

  /**
   * Visits a group expression (parenthesized sub-expression, optionally negated with not).
   *
   * @param expr the group expression
   * @return the visitor result
   */
  R visit(GroupExpression expr);

  /**
   * Visits a value path expression (e.g., {@code emails[type eq "work"].value}).
   *
   * @param expr the value path expression
   * @return the visitor result
   */
  R visit(ValuePathExpression expr);
}
