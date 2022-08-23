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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.example.spring.extensions.LuckyNumberExtension;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.server.exception.UnableToUpdateResourceException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.provider.UpdateRequest;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.PageRequest;
import org.apache.directory.scim.spec.protocol.search.SortRequest;
import org.apache.directory.scim.spec.resources.*;
import org.springframework.stereotype.Service;

/**
 * Creates a singleton (effectively) Provider<User> with a memory-based
 * persistence layer.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@Service
public class InMemoryUserService implements Provider<ScimUser> {

  static final String DEFAULT_USER_ID = "1";
  static final String DEFAULT_USER_EXTERNAL_ID = "e" + DEFAULT_USER_ID;
  static final String DEFAULT_USER_DISPLAY_NAME = "User " + DEFAULT_USER_ID;
  static final String DEFAULT_USER_EMAIL_VALUE = "e1@example.com";
  static final String DEFAULT_USER_EMAIL_TYPE = "work";
  static final int DEFAULT_USER_LUCKY_NUMBER = 7;

  private final Map<String, ScimUser> users = new HashMap<>();

  private final Registry registry;

  public InMemoryUserService(Registry registry) {
    this.registry = registry;
  }

  @PostConstruct
  public void init() {
    ScimUser user = new ScimUser();
    user.setId(DEFAULT_USER_ID);
    user.setExternalId(DEFAULT_USER_EXTERNAL_ID);
    user.setUserName(DEFAULT_USER_EXTERNAL_ID);
    user.setDisplayName(DEFAULT_USER_DISPLAY_NAME);
    user.setName(new Name()
        .setGivenName("Tester")
        .setFamilyName("McTest"));
    Email email = new Email();
    email.setDisplay(DEFAULT_USER_EMAIL_VALUE);
    email.setValue(DEFAULT_USER_EMAIL_VALUE);
    email.setType(DEFAULT_USER_EMAIL_TYPE);
    email.setPrimary(true);
    user.setEmails(List.of(email));
    
    LuckyNumberExtension luckyNumberExtension = new LuckyNumberExtension();
    luckyNumberExtension.setLuckyNumber(DEFAULT_USER_LUCKY_NUMBER);
    
    user.addExtension(luckyNumberExtension);
    
    users.put(user.getId(), user);
  }

  @Override
  public Class<ScimUser> getResourceClass() {
    return ScimUser.class;
  }

  /**
   * @see Provider#create(ScimResource)
   */
  @Override
  public ScimUser create(ScimUser resource) throws UnableToCreateResourceException {
    String resourceId = resource.getId();
    int idCandidate = resource.hashCode();
    String id = resourceId != null ? resourceId : Integer.toString(idCandidate);

    while (users.containsKey(id)) {
      id = Integer.toString(idCandidate);
      ++idCandidate;
    }

    // check to make sure the user doesn't already exist
    boolean existingUserFound = users.values().stream()
      .anyMatch(user -> user.getUserName().equals(resource.getUserName()));
    if (existingUserFound) {
      // HTTP leaking into data layer
      throw new UnableToCreateResourceException(Response.Status.CONFLICT, "User '" + resource.getUserName() + "' already exists.");
    }

    resource.setId(id);
    users.put(id, resource);
    return resource;
  }

  /**
   * @see Provider#update(UpdateRequest)
   */
  @Override
  public ScimUser update(UpdateRequest<ScimUser> updateRequest) throws UnableToUpdateResourceException {
    String id = updateRequest.getId();
    ScimUser resource = updateRequest.getResource();
    users.put(id, resource);
    return resource;
  }

  /**
   * @see Provider#get(java.lang.String)
   */
  @Override
  public ScimUser get(String id) {
    return users.get(id);
  }

  /**
   * @see Provider#delete(java.lang.String)
   */
  @Override
  public void delete(String id) {
    users.remove(id);
  }

  /**
   * @see Provider#find(Filter, PageRequest, SortRequest)
   */
  @Override
  public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {

    long count = pageRequest.getCount() != null ? pageRequest.getCount() : users.size();
    long startIndex = pageRequest.getStartIndex() != null
      ? pageRequest.getStartIndex() - 1 // SCIM is 1-based indexed
      : 0;

    List<ScimUser> result = users.values().stream()
      .skip(startIndex)
      .limit(count)
      .filter(user -> InMemoryScimFilterMatcher.matches(user, registry.getSchema(ScimUser.SCHEMA_URI), filter))
      .collect(Collectors.toList());

    return new FilterResponse<>(result, pageRequest, result.size());
  }

  /**
   * @see Provider#getExtensionList()
   */
  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return List.of(LuckyNumberExtension.class);
  }
}
