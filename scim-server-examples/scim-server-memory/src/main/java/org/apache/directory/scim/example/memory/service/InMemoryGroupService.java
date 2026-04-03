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

package org.apache.directory.scim.example.memory.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.core.repository.BaseRepository;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.core.repository.ScimRequestContext;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterExpressions;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.resources.ScimGroup;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Named
@ApplicationScoped
public class InMemoryGroupService extends BaseRepository<ScimGroup> {

  private final Map<String, ScimGroup> groups = new ConcurrentHashMap<>();

  private SchemaRegistry schemaRegistry;

  @Inject
  public InMemoryGroupService(SchemaRegistry schemaRegistry, PatchHandler patchHandler) {
    super(ScimGroup.class, patchHandler);
    this.schemaRegistry = schemaRegistry;
  }

  protected InMemoryGroupService() {}

  @PostConstruct
  public void init() {
    ScimGroup group = new ScimGroup();
    group.setId(UUID.randomUUID().toString());
    group.setDisplayName("example-group");
    group.setExternalId("example-group");
    groups.put(group.getId(), group);
  }

  @Override
  public ScimGroup create(ScimGroup resource, ScimRequestContext requestContext) throws UnableToCreateResourceException {
    String id = UUID.randomUUID().toString();

    // if the external ID is not set, use the displayName instead
    if (StringUtils.isEmpty(resource.getExternalId())) {
      resource.setExternalId(resource.getDisplayName());
    }

    // check to make sure the group doesn't already exist
    boolean existingGroupFound = groups.values().stream()
      .anyMatch(group -> resource.getExternalId().equals(group.getExternalId()));
    if (existingGroupFound) {
      // HTTP leaking into data layer
      throw new UnableToCreateResourceException(Response.Status.CONFLICT, "Group '" + resource.getExternalId() + "' already exists.");
    }

    resource.setId(id);
    groups.put(id, resource);
    return resource;
  }

  @Override
  public ScimGroup update(String id, ScimGroup resource, ScimRequestContext requestContext) throws ResourceException {
    if (!groups.containsKey(id)) {
      throw new ResourceNotFoundException(id);
    }
    groups.put(id, resource);
    return resource;
  }

  @Override
  public ScimGroup get(String id, ScimRequestContext requestContext) {
    return groups.get(id);
  }

  @Override
  public void delete(String id) throws ResourceException {
    if (groups.remove(id) == null) {
      throw new ResourceNotFoundException(id);
    }
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, ScimRequestContext requestContext) {
    List<ScimGroup> filtered = groups.values().stream()
      .filter(FilterExpressions.inMemory(filter, schemaRegistry.getSchema(ScimGroup.SCHEMA_URI)))
      .toList();

    PageRequest pageRequest = requestContext.getPageRequestOrDefault();
    return new FilterResponse<>(pageRequest.paginate(filtered), filtered.size());
  }

}
