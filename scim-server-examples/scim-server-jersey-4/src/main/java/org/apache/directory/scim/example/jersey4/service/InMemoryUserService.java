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

package org.apache.directory.scim.example.jersey4.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.core.repository.BaseRepository;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.example.jersey4.extensions.LuckyNumberExtension;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.core.repository.ScimRequestContext;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterExpressions;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.SortExpressions;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a singleton (effectively) {@code Repository<ScimUser>} with a memory-based
 * persistence layer.
 *
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@Named
@ApplicationScoped
public class InMemoryUserService extends BaseRepository<ScimUser> {

  static final String DEFAULT_USER_ID = UUID.randomUUID().toString();
  static final String DEFAULT_USER_EXTERNAL_ID = "e" + DEFAULT_USER_ID;
  static final String DEFAULT_USER_DISPLAY_NAME = "User " + DEFAULT_USER_ID;
  static final String DEFAULT_USER_EMAIL_VALUE = "e1@example.com";
  static final String DEFAULT_USER_EMAIL_TYPE = "work";
  static final int DEFAULT_USER_LUCKY_NUMBER = 7;

  private final Map<String, ScimUser> users = new ConcurrentHashMap<>();

  private SchemaRegistry schemaRegistry;

  @Inject
  public InMemoryUserService(SchemaRegistry schemaRegistry, PatchHandler patchHandler) {
    super(ScimUser.class, patchHandler);
    this.schemaRegistry = schemaRegistry;
  }

  protected InMemoryUserService() {}

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

    EnterpriseExtension enterpriseExtension = new EnterpriseExtension();
    enterpriseExtension.setEmployeeNumber("12345");
    EnterpriseExtension.Manager manager = new EnterpriseExtension.Manager();
    manager.setValue("bulkId:qwerty");
    enterpriseExtension.setManager(manager);
    user.addExtension(enterpriseExtension);

    users.put(user.getId(), user);
  }

  @Override
  public ScimUser create(ScimUser resource, ScimRequestContext requestContext) throws UnableToCreateResourceException {
    String id = UUID.randomUUID().toString();

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
  public ScimUser update(String id, ScimUser resource, ScimRequestContext requestContext) throws ResourceException {
    if (!users.containsKey(id)) {
      throw new ResourceNotFoundException(id);
    }
    users.put(id, resource);
    return resource;
  }

  @Override
  public ScimUser get(String id, ScimRequestContext requestContext) {
    return users.get(id);
  }

  @Override
  public void delete(String id) throws ResourceException {
    if (users.remove(id) == null) {
      throw new ResourceNotFoundException(id);
    }
  }

  @Override
  public FilterResponse<ScimUser> find(Filter filter, ScimRequestContext requestContext) {
    Schema schema = schemaRegistry.getSchema(ScimUser.SCHEMA_URI);
    return users.values().stream()
      .filter(FilterExpressions.inMemory(filter, schema))
      .sorted(SortExpressions.comparator(requestContext.getSortRequest(), schema))
      .collect(FilterResponse.paginate(requestContext.getPageRequestOrDefault()));
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return List.of(LuckyNumberExtension.class, EnterpriseExtension.class);
  }
}
