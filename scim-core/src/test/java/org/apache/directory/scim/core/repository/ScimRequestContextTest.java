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

package org.apache.directory.scim.core.repository;

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScimRequestContextTest {

  static final String USER_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";
  static final String ENTERPRISE_SCHEMA = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

  // --- getIncludedAttributeNames ---

  @Test
  void getIncludedAttributeNames_returnsFullyQualifiedNames() {
    // Server layer normalizes references before they reach the repository,
    // so all references should have URNs by this point
    AttributeReference ref1 = new AttributeReference(USER_SCHEMA + ":userName");
    AttributeReference ref2 = new AttributeReference(USER_SCHEMA + ":emails");
    ScimRequestContext ctx = new ScimRequestContext(Set.of(ref1, ref2), Set.of());

    Set<String> names = ctx.getIncludedAttributeNames();

    assertThat(names).containsExactlyInAnyOrder(
      USER_SCHEMA + ":userName",
      USER_SCHEMA + ":emails"
    );
  }

  @Test
  void getIncludedAttributeNames_extensionAttributesPreserveTheirUrn() {
    AttributeReference coreAttr = new AttributeReference(USER_SCHEMA + ":userName");
    AttributeReference extensionAttr = new AttributeReference(ENTERPRISE_SCHEMA + ":department");
    ScimRequestContext ctx = new ScimRequestContext(Set.of(coreAttr, extensionAttr), Set.of());

    Set<String> names = ctx.getIncludedAttributeNames();

    assertThat(names).containsExactlyInAnyOrder(
      USER_SCHEMA + ":userName",
      ENTERPRISE_SCHEMA + ":department"
    );
  }

  @Test
  void getIncludedAttributeNames_subAttributeIncluded() {
    AttributeReference ref = new AttributeReference(USER_SCHEMA + ":name.givenName");
    ScimRequestContext ctx = new ScimRequestContext(Set.of(ref), Set.of());

    assertThat(ctx.getIncludedAttributeNames())
      .containsExactly(USER_SCHEMA + ":name.givenName");
  }

  @Test
  void getIncludedAttributeNames_emptyReturnsEmptySet() {
    ScimRequestContext ctx = new ScimRequestContext(Set.of(), Set.of());
    assertThat(ctx.getIncludedAttributeNames()).isEmpty();
  }

  @Test
  void getIncludedAttributeNames_nullReturnsEmptySet() {
    ScimRequestContext ctx = new ScimRequestContext();
    ctx.setIncludedAttributes(null);
    assertThat(ctx.getIncludedAttributeNames()).isEmpty();
  }

  // --- getExcludedAttributeNames ---

  @Test
  void getExcludedAttributeNames_returnsFullyQualifiedNames() {
    AttributeReference ref = new AttributeReference(USER_SCHEMA + ":password");
    ScimRequestContext ctx = new ScimRequestContext(Set.of(), Set.of(ref));

    assertThat(ctx.getExcludedAttributeNames())
      .containsExactly(USER_SCHEMA + ":password");
  }

  @Test
  void getExcludedAttributeNames_emptyReturnsEmptySet() {
    ScimRequestContext ctx = new ScimRequestContext(Set.of(), Set.of());
    assertThat(ctx.getExcludedAttributeNames()).isEmpty();
  }

  // --- empty() ---

  @Test
  void empty_returnsEmptySetsForBothIncludedAndExcluded() {
    ScimRequestContext ctx = ScimRequestContext.empty();
    assertThat(ctx.getIncludedAttributeNames()).isEmpty();
    assertThat(ctx.getExcludedAttributeNames()).isEmpty();
  }
}
