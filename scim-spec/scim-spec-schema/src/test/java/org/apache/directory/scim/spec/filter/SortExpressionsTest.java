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
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schemas;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SortExpressions#comparator(SortRequest, Schema)}, which converts a SCIM
 * {@link SortRequest} into a {@link Comparator} for in-memory sorting of {@link ScimResource}s.
 * Covers ascending/descending order, sub-attributes, DateTime sorting, case-insensitive string
 * comparison, null handling, unknown attributes, and composition with filtering and pagination.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.3">RFC 7644 Section 3.4.2.3 - Sorting</a>
 */
class SortExpressionsTest {

  private static final Schema USER_SCHEMA = Schemas.schemaFor(ScimUser.class);

  private static final LocalDateTime NOW = LocalDateTime.now();

  private static final ScimUser ALICE = user("alice", "Zulu", NOW.minusDays(2), NOW.minusDays(3));
  private static final ScimUser BOB = user("bob", "Alpha", NOW, NOW);
  private static final ScimUser CHARLIE = user("Charlie", "Mango", NOW.minusDays(1), NOW.minusDays(2));
  private static final ScimUser DAVE = userNoName("dave");

  private static final List<ScimUser> ALL_USERS = List.of(ALICE, BOB, CHARLIE, DAVE);

  @Test
  void defaultAscendingSortOrder() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"));
    // sortOrder is null — RFC 7644 says default is ascending

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  @Test
  void explicitAscending() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"))
      .setSortOrder(SortOrder.ASCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  @Test
  void explicitDescending() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"))
      .setSortOrder(SortOrder.DESCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("dave", "Charlie", "bob", "alice");
  }

  @Test
  void dateTimeAttribute() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("meta.lastModified"))
      .setSortOrder(SortOrder.ASCENDING);

    // DAVE has null lastModified, should sort to end
    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("alice", "Charlie", "bob", "dave");
  }

  @Test
  void descendingWithNullValuesAlwaysSortToEnd() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("meta.lastModified"))
      .setSortOrder(SortOrder.DESCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    // DAVE has null meta - should sort to END regardless of direction
    assertThat(userNames(sorted)).containsExactly("bob", "Charlie", "alice", "dave");
  }

  @Test
  void dateTimeCreatedAttribute() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("meta.created"))
      .setSortOrder(SortOrder.ASCENDING);

    // DAVE has null meta, should sort to end
    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("alice", "Charlie", "bob", "dave");
  }

  @Test
  void subAttribute() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("name.familyName"))
      .setSortOrder(SortOrder.ASCENDING);

    // DAVE has no name set, should sort to end
    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("bob", "Charlie", "alice", "dave");
  }

  @Test
  void caseInsensitiveStringSort() {
    // userName is not caseExact, so "Charlie" should sort between "bob" and "dave"
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"))
      .setSortOrder(SortOrder.ASCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    // Case-insensitive: alice, bob, Charlie, dave (not Charlie, alice, bob, dave)
    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  @Test
  void unsupportedAttributePreservesOrder() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("nonExistent"))
      .setSortOrder(SortOrder.ASCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    // No-op comparator should preserve original order
    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  @Test
  void sortWithFilter() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"))
      .setSortOrder(SortOrder.DESCENDING);

    Filter filter = FilterBuilder.create().startsWith("userName", "b").build();

    List<ScimResource> result = ALL_USERS.stream()
      .map(ScimResource.class::cast)
      .filter(FilterExpressions.inMemory(filter, USER_SCHEMA))
      .sorted(SortExpressions.comparator(request, USER_SCHEMA))
      .collect(Collectors.toList());

    assertThat(userNames(result)).containsExactly("bob");
  }

  @Test
  void sortWithPagination() {
    SortRequest request = new SortRequest()
      .setSortBy(new AttributeReference("userName"))
      .setSortOrder(SortOrder.ASCENDING);

    List<ScimResource> result = ALL_USERS.stream()
      .map(ScimResource.class::cast)
      .sorted(SortExpressions.comparator(request, USER_SCHEMA))
      .skip(1)
      .limit(2)
      .collect(Collectors.toList());

    assertThat(userNames(result)).containsExactly("bob", "Charlie");
  }

  @Test
  void nullSortRequestReturnsNoOpComparator() {
    Comparator<ScimResource> comparator = SortExpressions.comparator(null, USER_SCHEMA);

    List<ScimResource> sorted = ALL_USERS.stream()
      .map(ScimResource.class::cast)
      .sorted(comparator)
      .collect(Collectors.toList());

    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  @Test
  void nullSortByReturnsNoOpComparator() {
    SortRequest request = new SortRequest()
      .setSortOrder(SortOrder.ASCENDING);

    List<ScimResource> sorted = sorted(ALL_USERS, request);

    assertThat(userNames(sorted)).containsExactly("alice", "bob", "Charlie", "dave");
  }

  private static List<ScimResource> sorted(List<ScimUser> users, SortRequest request) {
    return users.stream()
      .map(ScimResource.class::cast)
      .sorted(SortExpressions.comparator(request, USER_SCHEMA))
      .collect(Collectors.toList());
  }

  private static List<String> userNames(List<ScimResource> resources) {
    List<String> names = new ArrayList<>();
    for (ScimResource r : resources) {
      names.add(((ScimUser) r).getUserName());
    }
    return names;
  }

  private static ScimUser user(String username, String familyName, LocalDateTime lastModified, LocalDateTime created) {
    ScimUser user = new ScimUser()
      .setUserName(username)
      .setActive(true)
      .setName(new Name()
        .setGivenName(username)
        .setFamilyName(familyName)
      )
      .setEmails(List.of(
        new Email()
          .setType("work")
          .setPrimary(true)
          .setValue(username + "@example.com")
      ));

    user.setMeta(new Meta()
      .setLastModified(lastModified)
      .setCreated(created));

    return user;
  }

  private static ScimUser userNoName(String username) {
    ScimUser user = new ScimUser()
      .setUserName(username)
      .setActive(true)
      .setEmails(List.of(
        new Email()
          .setType("work")
          .setPrimary(true)
          .setValue(username + "@example.com")
      ));

    // No name set, no meta set — tests null handling
    return user;
  }
}
