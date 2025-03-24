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

package org.apache.directory.scim.spring.it.app;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.core.repository.ETag;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterExpressions;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Creates a singleton (effectively) Provider<User> with a memory-based
 * persistence layer.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@Service
public class InMemoryUserService implements Repository<ScimUser> {

  static final String DEFAULT_USER_ID = "1";
  static final String DEFAULT_USER_EXTERNAL_ID = "e" + DEFAULT_USER_ID;
  static final String DEFAULT_USER_DISPLAY_NAME = "User " + DEFAULT_USER_ID;
  static final String DEFAULT_USER_EMAIL_VALUE = "e1@example.com";
  static final String DEFAULT_USER_EMAIL_TYPE = "work";
  static final int DEFAULT_USER_LUCKY_NUMBER = 7;

  private final Map<String, ScimUser> users = new ConcurrentHashMap<>();

  private final SchemaRegistry schemaRegistry;

  private final PatchHandler patchHandler;

  public InMemoryUserService(SchemaRegistry schemaRegistry, PatchHandler patchHandler) {
    this.schemaRegistry = schemaRegistry;
    this.patchHandler = patchHandler;
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
    
//    LuckyNumberExtension luckyNumberExtension = new LuckyNumberExtension();
//    luckyNumberExtension.setLuckyNumber(DEFAULT_USER_LUCKY_NUMBER);
//
//    user.addExtension(luckyNumberExtension);
    
    users.put(user.getId(), user);
  }

  @Override
  public Class<ScimUser> getResourceClass() {
    return ScimUser.class;
  }

  /**
   * @see Repository#create(ScimResource)
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

  @Override
  public ScimUser update(String id, Set<ETag> etags, ScimUser resource, Set<AttributeReference> includedAttributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ResourceException {
    if (!users.containsKey(id)) {
      throw new ResourceNotFoundException(id);
    }
    users.put(id, resource);
    return resource;
  }

  @Override
  public ScimUser patch(String id, Set<ETag> etags, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ResourceException {
    if (!users.containsKey(id)) {
      throw new ResourceNotFoundException(id);
    }
    ScimUser resource = patchHandler.apply(get(id), patchOperations);
    users.put(id, resource);
    return resource;
  }

  /**
   * @see Repository#get(String)
   */
  @Override
  public ScimUser get(String id) {
    return users.get(id);
  }

  /**
   * @see Repository#delete(String)
   */
  @Override
  public void delete(String id) throws ResourceException {
    if (users.remove(id) == null) {
      throw new ResourceNotFoundException(id);
    }
  }

  /**
   * @see Repository#find(Filter, PageRequest, SortRequest)
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
      .filter(FilterExpressions.inMemory(filter, schemaRegistry.getSchema(ScimUser.SCHEMA_URI)))
      .collect(Collectors.toList());

    return new FilterResponse<>(result, pageRequest, result.size());
  }

  /**
   * @see Repository#getExtensionList()
   */
  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }
}
