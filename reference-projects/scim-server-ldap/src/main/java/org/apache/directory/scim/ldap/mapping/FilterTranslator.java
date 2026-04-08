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

package org.apache.directory.scim.ldap.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.filter.AndNode;
import org.apache.directory.api.ldap.model.filter.EqualityNode;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.filter.GreaterEqNode;
import org.apache.directory.api.ldap.model.filter.LessEqNode;
import org.apache.directory.api.ldap.model.filter.NotNode;
import org.apache.directory.api.ldap.model.filter.ObjectClassNode;
import org.apache.directory.api.ldap.model.filter.OrNode;
import org.apache.directory.api.ldap.model.filter.PresenceNode;
import org.apache.directory.api.ldap.model.filter.SubstringNode;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.spec.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.filter.AttributePresentExpression;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterExpression;
import org.apache.directory.scim.spec.filter.FilterExpressionVisitor;
import org.apache.directory.scim.spec.filter.GroupExpression;
import org.apache.directory.scim.spec.filter.LogicalExpression;
import org.apache.directory.scim.spec.filter.LogicalOperator;
import org.apache.directory.scim.spec.filter.ValuePathExpression;

import java.util.List;
import java.util.function.Function;

/**
 * Translates SCIM {@link Filter} expressions into Apache Directory LDAP API
 * {@link ExprNode} filter trees using the {@link FilterExpressionVisitor} pattern.
 *
 * <p>All SCIM comparison operators ({@code eq}, {@code ne}, {@code co}, {@code sw},
 * {@code ew}, {@code gt}, {@code ge}, {@code lt}, {@code le}, {@code pr}) and logical
 * operators ({@code and}, {@code or}, {@code not}) are supported. SCIM attribute names
 * are resolved to their LDAP counterparts through the configurable mapping held by
 * {@link AttributeMapper}. When no mapping is found the SCIM attribute name is used
 * as-is, providing a best-effort fallback.</p>
 *
 * <p>Translation is performed by a private {@link FilterExpressionVisitor} implementation
 * ({@code LdapFilterVisitor}) that dispatches each SCIM expression node to the appropriate
 * LDAP filter construction method via {@link FilterExpression#accept(FilterExpressionVisitor)}.</p>
 *
 * <p>Filter values are escaped automatically by the {@link ExprNode} implementations,
 * eliminating the risk of LDAP filter injection.</p>
 */
@ApplicationScoped
public class FilterTranslator {

  @Inject
  AttributeMapper attributeMapper;

  @Inject
  ScimLdapConfig config;

  protected FilterTranslator() {}

  /**
   * Builds a complete LDAP search filter for user queries by combining the configured
   * user objectClass with the translated SCIM filter.
   *
   * @param filter the SCIM filter to translate, may be {@code null}
   * @return an {@link ExprNode} combining the objectClass constraint with the SCIM filter
   */
  public ExprNode buildUserSearchFilter(Filter filter) {
    ExprNode scimFilter = translateFilter(filter, attributeMapper::getLdapUserAttribute);
    ExprNode objectClass = new EqualityNode<>("objectClass", config.getUserObjectClasses().get(0));
    return new AndNode(objectClass, scimFilter);
  }

  /**
   * Builds a complete LDAP search filter for group queries by combining the configured
   * group objectClass with the translated SCIM filter.
   *
   * @param filter the SCIM filter to translate, may be {@code null}
   * @return an {@link ExprNode} combining the objectClass constraint with the SCIM filter
   */
  public ExprNode buildGroupSearchFilter(Filter filter) {
    ExprNode scimFilter = translateFilter(filter, attributeMapper::getLdapGroupAttribute);
    ExprNode objectClass = new EqualityNode<>("objectClass", config.getGroupObjectClasses().get(0));
    return new AndNode(objectClass, scimFilter);
  }

  private ExprNode translateFilter(Filter filter, Function<String, String> attrResolver) {
    if (filter == null || filter.getExpression() == null) {
      return ObjectClassNode.OBJECT_CLASS_NODE;
    }
    return filter.getExpression().accept(new LdapFilterVisitor(attrResolver));
  }

  /**
   * A {@link FilterExpressionVisitor} that translates each SCIM filter expression node
   * into the corresponding Apache Directory LDAP API {@link ExprNode}.
   */
  private static class LdapFilterVisitor implements FilterExpressionVisitor<ExprNode> {

    private final Function<String, String> attrResolver;

    LdapFilterVisitor(Function<String, String> attrResolver) {
      this.attrResolver = attrResolver;
    }

    @Override
    public ExprNode visit(AttributeComparisonExpression expr) {
      String ldapAttr = resolveAttribute(expr);
      String value = expr.getCompareValue() != null ? expr.getCompareValue().toString() : "";

      try {
        return switch (expr.getOperation()) {
          case EQ -> new EqualityNode<>(ldapAttr, value);
          case NE -> new NotNode(new EqualityNode<>(ldapAttr, value));
          case CO -> new SubstringNode(List.of(value), ldapAttr, null, null);
          case SW -> new SubstringNode(ldapAttr, value, null);
          case EW -> new SubstringNode(ldapAttr, null, value);
          case GT -> new AndNode(new GreaterEqNode<>(ldapAttr, value), new NotNode(new EqualityNode<>(ldapAttr, value)));
          case GE -> new GreaterEqNode<>(ldapAttr, value);
          case LT -> new AndNode(new LessEqNode<>(ldapAttr, value), new NotNode(new EqualityNode<>(ldapAttr, value)));
          case LE -> new LessEqNode<>(ldapAttr, value);
          case PR -> new PresenceNode(ldapAttr);
        };
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to build LDAP filter for " + ldapAttr + " " + expr.getOperation(), e);
      }
    }

    @Override
    public ExprNode visit(AttributePresentExpression expr) {
      String scimAttr = expr.getAttributePath().getAttributeName();
      String ldapAttr = attrResolver.apply(scimAttr);
      if (ldapAttr == null) {
        ldapAttr = scimAttr;
      }
      return new PresenceNode(ldapAttr);
    }

    @Override
    public ExprNode visit(LogicalExpression expr) {
      ExprNode left = expr.getLeft().accept(this);
      ExprNode right = expr.getRight().accept(this);
      if (expr.getOperator() == LogicalOperator.AND) {
        return new AndNode(left, right);
      }
      return new OrNode(left, right);
    }

    @Override
    public ExprNode visit(GroupExpression expr) {
      ExprNode inner = expr.getFilterExpression().accept(this);
      if (expr.isNot()) {
        return new NotNode(inner);
      }
      return inner;
    }

    @Override
    public ExprNode visit(ValuePathExpression expr) {
      if (expr.getAttributeExpression() != null) {
        return expr.getAttributeExpression().accept(this);
      }
      throw new UnsupportedOperationException(
        "ValuePathExpression without an attribute expression cannot be translated to LDAP");
    }

    private String resolveAttribute(AttributeComparisonExpression expr) {
      String scimAttr = expr.getAttributePath().getAttributeName();
      String subAttr = expr.getAttributePath().getSubAttributeName();
      String scimPath = subAttr != null ? scimAttr + "." + subAttr : scimAttr;
      String ldapAttr = attrResolver.apply(scimPath);
      return ldapAttr != null ? ldapAttr : scimAttr;
    }
  }
}
