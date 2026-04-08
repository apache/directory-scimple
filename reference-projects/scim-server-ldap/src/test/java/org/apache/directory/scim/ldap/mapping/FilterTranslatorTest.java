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
import org.apache.directory.scim.spec.filter.CompareOperator;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.GroupExpression;
import org.apache.directory.scim.spec.filter.ValuePathExpression;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilterTranslatorTest {

  AttributeMapper attributeMapper = mock(AttributeMapper.class);
  ScimLdapConfig config = mock(ScimLdapConfig.class);

  FilterTranslator filterTranslator;

  @BeforeEach
  void setUp() throws Exception {
    filterTranslator = FilterTranslator.class.getDeclaredConstructor().newInstance();

    Field mapperField = FilterTranslator.class.getDeclaredField("attributeMapper");
    mapperField.setAccessible(true);
    mapperField.set(filterTranslator, attributeMapper);

    Field configField = FilterTranslator.class.getDeclaredField("config");
    configField.setAccessible(true);
    configField.set(filterTranslator, config);

    when(config.getUserObjectClasses()).thenReturn(List.of("inetOrgPerson"));
    when(config.getGroupObjectClasses()).thenReturn(List.of("groupOfNames"));
  }

  // --- Null/empty filter ---

  @Test
  void buildUserSearchFilter_nullFilter_returnsAndWithObjectClassNode() {
    ExprNode result = filterTranslator.buildUserSearchFilter(null);

    assertThat(result).isInstanceOf(AndNode.class);
    AndNode andNode = (AndNode) result;
    assertThat(andNode.getChildren()).hasSize(2);
    assertThat(andNode.getChildren().get(0)).isInstanceOf(EqualityNode.class);
    assertThat(andNode.getChildren().get(0).toString()).contains("objectClass=inetOrgPerson");
    assertThat(andNode.getChildren().get(1)).isSameAs(ObjectClassNode.OBJECT_CLASS_NODE);
  }

  @Test
  void buildUserSearchFilter_emptyExpression_returnsAndWithObjectClassNode() throws Exception {
    java.lang.reflect.Constructor<Filter> ctor = Filter.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    Filter filter = ctor.newInstance();

    ExprNode result = filterTranslator.buildUserSearchFilter(filter);

    assertThat(result).isInstanceOf(AndNode.class);
    AndNode andNode = (AndNode) result;
    assertThat(andNode.getChildren().get(1)).isSameAs(ObjectClassNode.OBJECT_CLASS_NODE);
  }

  // --- Comparison operators ---

  @Test
  void buildUserSearchFilter_eqOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName eq \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=john");
  }

  @Test
  void buildUserSearchFilter_neOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName ne \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(NotNode.class);
    ExprNode inner = ((NotNode) scimNode).getFirstChild();
    assertThat(inner).isInstanceOf(EqualityNode.class);
    assertThat(inner.toString()).contains("uid=john");
  }

  @Test
  void buildUserSearchFilter_coOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName co \"oh\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(SubstringNode.class);
    assertThat(scimNode.toString()).contains("uid=*oh*");
  }

  @Test
  void buildUserSearchFilter_swOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName sw \"jo\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(SubstringNode.class);
    assertThat(scimNode.toString()).contains("uid=jo*");
  }

  @Test
  void buildUserSearchFilter_ewOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName ew \"hn\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(SubstringNode.class);
    assertThat(scimNode.toString()).contains("uid=*hn");
  }

  @Test
  void buildUserSearchFilter_gtOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName gt \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(AndNode.class);
    AndNode gtNode = (AndNode) scimNode;
    assertThat(gtNode.getChildren().get(0)).isInstanceOf(GreaterEqNode.class);
    assertThat(gtNode.getChildren().get(1)).isInstanceOf(NotNode.class);
    assertThat(gtNode.getChildren().get(0).toString()).contains("uid>=john");
  }

  @Test
  void buildUserSearchFilter_geOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName ge \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(GreaterEqNode.class);
    assertThat(scimNode.toString()).contains("uid>=john");
  }

  @Test
  void buildUserSearchFilter_ltOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName lt \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(AndNode.class);
    AndNode ltNode = (AndNode) scimNode;
    assertThat(ltNode.getChildren().get(0)).isInstanceOf(LessEqNode.class);
    assertThat(ltNode.getChildren().get(1)).isInstanceOf(NotNode.class);
    assertThat(ltNode.getChildren().get(0).toString()).contains("uid<=john");
  }

  @Test
  void buildUserSearchFilter_leOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName le \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(LessEqNode.class);
    assertThat(scimNode.toString()).contains("uid<=john");
  }

  @Test
  void buildUserSearchFilter_prOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName pr"));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(PresenceNode.class);
    assertThat(scimNode.toString()).contains("uid=*");
  }

  // --- AttributePresentExpression ---

  @Test
  void buildUserSearchFilter_attributePresentExpression() {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");
    Filter filter = new Filter(new AttributePresentExpression(new AttributeReference("userName")));

    ExprNode result = filterTranslator.buildUserSearchFilter(filter);

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(PresenceNode.class);
    assertThat(scimNode.toString()).contains("uid=*");
  }

  // --- Logical operators ---

  @Test
  void buildUserSearchFilter_andOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");
    when(attributeMapper.getLdapUserAttribute("displayName")).thenReturn("cn");

    ExprNode result = filterTranslator.buildUserSearchFilter(
      new Filter("userName eq \"john\" and displayName eq \"John Doe\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(AndNode.class);
    AndNode innerAnd = (AndNode) scimNode;
    assertThat(innerAnd.getChildren()).hasSize(2);
    assertThat(innerAnd.getChildren().get(0)).isInstanceOf(EqualityNode.class);
    assertThat(innerAnd.getChildren().get(1)).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=john");
    assertThat(scimNode.toString()).contains("cn=John Doe");
  }

  @Test
  void buildUserSearchFilter_orOperator() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");
    when(attributeMapper.getLdapUserAttribute("displayName")).thenReturn("cn");

    ExprNode result = filterTranslator.buildUserSearchFilter(
      new Filter("userName eq \"john\" or displayName eq \"John Doe\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(OrNode.class);
    OrNode orNode = (OrNode) scimNode;
    assertThat(orNode.getChildren()).hasSize(2);
    assertThat(scimNode.toString()).contains("uid=john");
    assertThat(scimNode.toString()).contains("cn=John Doe");
  }

  // --- GroupExpression ---

  @Test
  void buildUserSearchFilter_groupExpressionNotTrue() {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");
    AttributeComparisonExpression inner = new AttributeComparisonExpression(
      new AttributeReference("userName"), CompareOperator.EQ, "john");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter(new GroupExpression(true, inner)));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(NotNode.class);
    assertThat(scimNode.toString()).contains("uid=john");
  }

  @Test
  void buildUserSearchFilter_groupExpressionNotFalse() {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");
    AttributeComparisonExpression inner = new AttributeComparisonExpression(
      new AttributeReference("userName"), CompareOperator.EQ, "john");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter(new GroupExpression(false, inner)));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=john");
  }

  // --- ValuePathExpression ---

  @Test
  void buildUserSearchFilter_valuePathExpressionWithInnerExpression() {
    when(attributeMapper.getLdapUserAttribute("type")).thenReturn("emailType");
    AttributeComparisonExpression inner = new AttributeComparisonExpression(
      new AttributeReference("type"), CompareOperator.EQ, "work");

    ExprNode result = filterTranslator.buildUserSearchFilter(
      new Filter(ValuePathExpression.fromFilterExpression("emails", inner)));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("emailType=work");
  }

  @Test
  void buildUserSearchFilter_valuePathExpressionWithoutInnerExpression_throwsUnsupported() {
    Filter filter = new Filter(new ValuePathExpression(new AttributeReference("emails")));
    assertThatThrownBy(() -> filterTranslator.buildUserSearchFilter(filter))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("ValuePathExpression without an attribute expression");
  }

  // --- Attribute resolution ---

  @Test
  void buildUserSearchFilter_unmappedAttribute_fallsBackToScimName() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn(null);

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName eq \"john\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("userName=john");
  }

  @Test
  void buildUserSearchFilter_subAttribute_resolvesCorrectly() throws Exception {
    when(attributeMapper.getLdapUserAttribute("name.givenName")).thenReturn("givenName");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("name.givenName eq \"John\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("givenName=John");
  }

  // --- Special characters in values ---

  @Test
  void buildUserSearchFilter_specialCharactersAreEscaped() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName eq \"jo*hn\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=jo\\2Ahn");
  }

  @Test
  void buildUserSearchFilter_parenthesesInValueAreEscaped() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName eq \"jo(h)n\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=jo\\28h\\29n");
  }

  @Test
  void buildUserSearchFilter_backslashInValueIsEscaped() throws Exception {
    when(attributeMapper.getLdapUserAttribute("userName")).thenReturn("uid");

    ExprNode result = filterTranslator.buildUserSearchFilter(new Filter("userName eq \"jo\\\\hn\""));

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("uid=jo\\5C\\5Chn");
  }

  // --- Group filter uses group attribute mapping ---

  @Test
  void buildGroupSearchFilter_usesGroupAttributeMapping() throws Exception {
    when(attributeMapper.getLdapGroupAttribute("displayName")).thenReturn("cn");

    ExprNode result = filterTranslator.buildGroupSearchFilter(new Filter("displayName eq \"Admins\""));

    assertThat(result).isInstanceOf(AndNode.class);
    AndNode andNode = (AndNode) result;
    assertThat(andNode.getChildren().get(0)).isInstanceOf(EqualityNode.class);
    assertThat(andNode.getChildren().get(0).toString()).contains("objectClass=groupOfNames");
    ExprNode scimNode = andNode.getChildren().get(1);
    assertThat(scimNode).isInstanceOf(EqualityNode.class);
    assertThat(scimNode.toString()).contains("cn=Admins");
  }

  @Test
  void buildGroupSearchFilter_nullFilter_returnsAndWithObjectClassNode() {
    ExprNode result = filterTranslator.buildGroupSearchFilter(null);

    assertThat(result).isInstanceOf(AndNode.class);
    AndNode andNode = (AndNode) result;
    assertThat(andNode.getChildren()).hasSize(2);
    assertThat(andNode.getChildren().get(0)).isInstanceOf(EqualityNode.class);
    assertThat(andNode.getChildren().get(0).toString()).contains("objectClass=groupOfNames");
    assertThat(andNode.getChildren().get(1)).isSameAs(ObjectClassNode.OBJECT_CLASS_NODE);
  }

  @Test
  void buildUserSearchFilter_unmappedPresentAttribute_fallsBackToScimName() {
    when(attributeMapper.getLdapUserAttribute("unknownAttr")).thenReturn(null);
    Filter filter = new Filter(new AttributePresentExpression(new AttributeReference("unknownAttr")));

    ExprNode result = filterTranslator.buildUserSearchFilter(filter);

    assertThat(result).isInstanceOf(AndNode.class);
    ExprNode scimNode = ((AndNode) result).getChildren().get(1);
    assertThat(scimNode).isInstanceOf(PresenceNode.class);
    assertThat(scimNode.toString()).contains("unknownAttr=*");
  }
}
