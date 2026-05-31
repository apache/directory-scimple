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

package org.apache.directory.scim.spec.resources;

import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schemas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScimUser}.
 */
public class ScimUserTest {

  /** Distinctive password value so a substring assertion on toString() output is unambiguous. */
  private static final String SENTINEL = "SECRET-do-not-log-uuid";

  private ScimUser buildUserWithSentinelPassword() {
    ScimUser user = new ScimUser();
    user.setId("test-user-id-1");
    user.setUserName("jdoe");
    user.setDisplayName("John Doe");
    user.setPassword(SENTINEL);

    Name name = new Name();
    name.setGivenName("John");
    name.setFamilyName("Doe");
    user.setName(name);

    return user;
  }

  /**
   * VALUE-channel guard: a {@code Returned.NEVER} value (the sentinel) must not appear in
   * {@link ScimUser#toString()}. Pairs with {@link #allNeverReturnedAttributeNamesAbsentFromToString()}
   * (the NAME-channel guard) — both intentionally fire on the same leak; do not consolidate them.
   */
  @Test
  public void passwordSentinelAbsentFromToString() {
    ScimUser user = buildUserWithSentinelPassword();

    String toString = user.toString();

    assertThat(toString)
        .as("VALUE-channel guard: a Returned.NEVER attribute's VALUE (sentinel '%s') leaked " +
            "into ScimUser.toString(). If this fails, toString() was regenerated to include " +
            "'password'. Fix toString() to omit Returned.NEVER fields.", SENTINEL)
        .doesNotContain(SENTINEL);
  }

  /**
   * SCHEMA/NAME-channel guard: for every {@code Returned.NEVER} attribute in the
   * {@link ScimUser} schema, its name token (e.g. {@code "password="}) must be absent from
   * {@link ScimUser#toString()}. Being schema-driven, it also covers future
   * {@code Returned.NEVER} fields and the {@code password=null} case. See the inline note
   * for the schema-name/field-name assumption.
   */
  @Test
  public void allNeverReturnedAttributeNamesAbsentFromToString() {
    Schema userSchema = Schemas.schemaFor(ScimUser.class);

    List<String> neverAttributes = new ArrayList<>();
    for (Schema.Attribute attr : userSchema.getAttributes()) {
      if (Schema.Attribute.Returned.NEVER.equals(attr.getReturned())) {
        neverAttributes.add(attr.getName());
      }
    }

    assertThat(neverAttributes)
        .as("Expected at least 'password' to have Returned.NEVER in the ScimUser schema")
        .isNotEmpty();

    ScimUser user = buildUserWithSentinelPassword();
    String toString = user.toString();

    // Assert the attribute NAME token is absent. A regenerated toString() that includes
    // password will produce "..., password=SECRET-do-not-log-uuid, ..." and this assertion
    // catches it by the "password=" token independently of the value.
    //
    // Assumption: SCIM schema name == Java field name as rendered in toString().
    // If a future Returned.NEVER field's schema name differs from its toString() rendering,
    // add an explicit targeted test for that field alongside this schema-driven loop.
    for (String attrName : neverAttributes) {
      assertThat(toString)
          .as("SCHEMA/NAME-channel guard: a Returned.NEVER attribute NAME ('%s=') is being " +
              "rendered by ScimUser.toString(). Fix toString() to omit this field. " +
              "Note: this guard assumes the SCIM schema name matches the Java field name " +
              "as rendered by toString().",
              attrName)
          .doesNotContain(attrName + "=");
    }
  }

  /**
   * NAME-channel guard, null-value case: {@code "password="} must be absent from
   * {@link ScimUser#toString()} even when password is null — which the sentinel guard
   * cannot catch.
   */
  @Test
  public void nullPasswordAbsentFromToString() {
    ScimUser user = new ScimUser();
    user.setId("test-user-id-null-pw");
    user.setUserName("jdoe-null-pw");
    user.setDisplayName("Jane Doe");
    // password intentionally left null

    Name name = new Name();
    name.setGivenName("Jane");
    name.setFamilyName("Doe");
    user.setName(name);

    String toString = user.toString();

    assertThat(toString)
        .as("NAME-channel guard (null-value case): 'password=' must not appear in " +
            "ScimUser.toString() even when password is null. The VALUE-channel sentinel " +
            "test would not catch this because the sentinel is absent when password=null.")
        .doesNotContain("password=");
  }

  /**
   * Sanity check: {@code password} is annotated {@code Returned.NEVER} in the schema, so
   * the value-level guards above stay meaningful if the annotation is ever changed.
   */
  @Test
  public void passwordAttributeIsReturnedNeverInSchema() {
    Schema userSchema = Schemas.schemaFor(ScimUser.class);

    Schema.Attribute passwordAttr = userSchema.getAttribute("password");
    assertThat(passwordAttr)
        .as("Expected 'password' attribute to be present in ScimUser schema")
        .isNotNull();

    assertThat(passwordAttr.getReturned())
        .as("ScimUser.password must be annotated with Returned.NEVER per RFC 7643 §8.7.1")
        .isEqualTo(Schema.Attribute.Returned.NEVER);
  }
}
