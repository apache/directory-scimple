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

package org.apache.directory.scim.example.spring.service;

import org.apache.directory.scim.example.spring.extensions.LuckyNumberExtension;
import org.apache.directory.scim.server.exception.InvalidProviderException;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.spec.protocol.filter.FilterBuilder;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.resources.*;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;

import static org.apache.directory.scim.example.spring.service.InMemoryScimFilterMatcherTest.FilterAssert.assertThat;

import java.time.LocalDateTime;
import java.util.List;

public class InMemoryScimFilterMatcherTest {

  private final static ScimUser USER1 = user("user1", "User", "One")
        .setNickName("one")
        .setAddresses(List.of(
          new Address()
            .setType("home")
            .setPrimary(true)
            .setStreetAddress("742 Evergreen Terrace")
            .setRegion("Springfield")
            .setLocality("Unknown")
            .setPostalCode("012345")
            .setCountry("USA"),
          new Address()
            .setType("work")
            .setPrimary(true)
            .setStreetAddress("101 Reactor Way")
            .setRegion("Springfield")
            .setLocality("Unknown")
            .setPostalCode("012345")
            .setCountry("USA")
        )); static {
            USER1.addExtension(new LuckyNumberExtension().setLuckyNumber(111));
        }

  private final static ScimUser USER2 = user("user2", "User", "Two")
        .setAddresses(List.of(
          new Address()
            .setType("home")
            .setPrimary(true)
            .setStreetAddress("11234 Slumvillage Pass")
            .setRegion("Burfork")
            .setLocality("CA")
            .setPostalCode("221134")
            .setCountry("USA")
        )); static {
            USER2.addExtension(new LuckyNumberExtension().setLuckyNumber(8));
        }

  @Test
  public void userNameMatch() {
    assertThat(FilterBuilder.create().equalTo("userName", "user1"))
      .matches(USER1)
      .notMatches(USER2);
  }

  @Test
  public void familyNameMatches() {
    assertThat(FilterBuilder.create().equalTo("name.familyName", "Two"))
      .matches(USER2)
      .notMatches(USER1);
  }

  @Test
  public void startsWithMatches() {
    assertThat(FilterBuilder.create().startsWith("name.familyName", "Tw"))
      .matches(USER2)
      .notMatches(USER1);
  }

  @Test
  public void endsWithMatches() {
    assertThat(FilterBuilder.create().endsWith("name.familyName", "wo"))
      .matches(USER2)
      .notMatches(USER1);
  }

  @Test
  public void containsMatches() {
    assertThat(FilterBuilder.create().contains("name.familyName", "w"))
      .matches(USER2)
      .notMatches(USER1);
  }

  @Test
  public void givenAndFamilyNameMatches() {

    FilterBuilder filter = FilterBuilder.create()
      .equalTo("name.givenName", "User")
      .and(builder -> builder.equalTo("name.familyName", "Two"));

    assertThat(filter)
      .matches(USER2)
      .notMatches(USER1);
  }

  @Test
  public void familyNameOrMatches() {
    FilterBuilder filter = FilterBuilder.create()
      .equalTo("name.familyName", "One")
      .or(builder -> builder.equalTo("name.familyName", "Two"));

    assertThat(filter)
      .matches(USER2)
      .matches(USER1);
  }

  @Test
  public void noMatchPassword() {
    assertThat(FilterBuilder.create().equalTo("password", "super-secret"))
      .notMatches(USER1);
  }

  @Test
  public void invertUserNameMatches() {
    assertThat(FilterBuilder.create().not(filter -> filter.equalTo("userName", "user1")))
      .notMatches(USER1)
      .matches(USER2);
  }

  @Test
  public void presentAttributeMatches() {
    assertThat(FilterBuilder.create().present("nickName"))
      .matches(USER1)
      .notMatches(USER2);
  }

  @Test
  public void valuePathExpressionMatches() {
    assertThat(FilterBuilder.create().attributeHas("addresses", filter -> filter.equalTo("type", "work")))
      .matches(USER1)
      .notMatches(USER2);
  }

//  @Test
//  public void extensionValueMatches() {
//    assertThat(FilterBuilder.create().equalTo("luckyNumber", 111))
//      .matches(USER1)
//      .notMatches(USER2);
//  }

  @Test
  public void metaMatches() {
    assertThat(FilterBuilder.create().lessThan("meta.lastModified", LocalDateTime.now()))
      .matches(USER1);

    assertThat(FilterBuilder.create().greaterThan("meta.lastModified", LocalDateTime.now().minusYears(1)))
      .matches(USER1);

    assertThat(FilterBuilder.create().greaterThanOrEquals("meta.lastModified", USER1.getMeta().getLastModified()))
      .matches(USER1);

    assertThat(FilterBuilder.create().lessThanOrEquals("meta.lastModified", USER1.getMeta().getLastModified()))
      .matches(USER1);

    assertThat(FilterBuilder.create().equalTo("meta.lastModified", USER1.getMeta().getLastModified()))
      .matches(USER1);
  }

  static class FilterAssert extends AbstractAssert<FilterAssert, Filter> {

    protected FilterAssert(Filter actual) {
      super(actual, FilterAssert.class);
    }

    public FilterAssert matches(ScimUser user) {
      isNotNull();
      if (!InMemoryScimFilterMatcher.matches(user, schema(ScimUser.class), actual)) {
        failWithMessage("Expected filter '%s' to match user '%s'", actual.toString(), user);
      }
      return this;
    }

    public FilterAssert notMatches(ScimUser user) {
      isNotNull();
      if (InMemoryScimFilterMatcher.matches(user, schema(ScimUser.class), actual)) {
        failWithMessage("Expected filter '%s' to NOT match user '%s'", actual.toString(), user);
      }
      return this;
    }

    public static FilterAssert assertThat(Filter actual) {
      return new FilterAssert(actual);
    }

    public static FilterAssert assertThat(FilterBuilder actual) {
      return new FilterAssert(actual.build());
    }

    private static <T extends ScimResource> Schema schema(Class<T> resourceClass) {
      try {
        return ProviderRegistry.generateSchema(resourceClass);
      } catch (InvalidProviderException e) {
        throw new RuntimeException("Invalid configuration detected, failed to generate schema for class: " + resourceClass);
      }
    }
  }

  private static ScimUser user(String username, String givenName, String familyName) {
    ScimUser user = new ScimUser()
      .setUserName(username)
      .setPassword("super-secret")
      .setActive(true)
      .setName(new Name()
        .setGivenName(givenName)
        .setFamilyName(familyName)
      )
      .setEmails(List.of(
        new Email()
          .setType("work")
          .setPrimary(true)
          .setValue(username + "@example.com"),
        new Email()
          .setType("personal")
          .setValue(givenName + "." + familyName + "@example.com")
      ));

    user.setMeta(new Meta().setLastModified(LocalDateTime.now()));

    return user;
  }
}
