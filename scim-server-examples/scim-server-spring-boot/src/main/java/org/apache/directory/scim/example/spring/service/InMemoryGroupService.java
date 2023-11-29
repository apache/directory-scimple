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

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.exception.UnableToCreateResourceException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.*;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InMemoryGroupService implements Repository<ScimGroup> {

  private final Map<String, ScimGroup> groups = new HashMap<>();

  private final SchemaRegistry schemaRegistry;

  private final PatchHandler patchHandler;

  public InMemoryGroupService(SchemaRegistry schemaRegistry, PatchHandler patchHandler) {
    this.schemaRegistry = schemaRegistry;
    this.patchHandler = patchHandler;
  }

  @PostConstruct
  public void init() {
    ScimGroup group = new ScimGroup();
    group.setId(UUID.randomUUID().toString());
    group.setDisplayName("example-group");
    group.setExternalId("example-group");
    groups.put(group.getId(), group);
  }

  @Override
  public Class<ScimGroup> getResourceClass() {
    return ScimGroup.class;
  }

  @Override
  public ScimGroup create(ScimGroup resource) throws UnableToCreateResourceException {
    String id = UUID.randomUUID().toString();

    // if the external ID is not set, use the displayName instead
    if (!StringUtils.hasText(resource.getExternalId())) {
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
  public ScimGroup update(String id, String version, ScimGroup resource, Set<AttributeReference> includedAttributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ResourceException {
    groups.put(id, resource);
    return resource;
  }

  @Override
  public ScimGroup patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ResourceException {
    ScimGroup resource = patchHandler.apply(get(id), patchOperations);
    groups.put(id, resource);
    return resource;
  }

  @Override
  public ScimGroup get(String id) {
    return groups.get(id);
  }

  @Override
  public void delete(String id) {
    groups.remove(id);
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
    long count = pageRequest.getCount() != null ? pageRequest.getCount() : groups.size();
    long startIndex = pageRequest.getStartIndex() != null
      ? pageRequest.getStartIndex() - 1 // SCIM is 1-based indexed
      : 0;

    List<ScimGroup> result = groups.values().stream()
      .skip(startIndex)
      .limit(count)
      .filter(FilterExpressions.inMemory(filter, schemaRegistry.getSchema(ScimGroup.SCHEMA_URI)))
      .collect(Collectors.toList());

    return new FilterResponse<>(result, pageRequest, result.size());
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Collections.emptyList();
  }

}
