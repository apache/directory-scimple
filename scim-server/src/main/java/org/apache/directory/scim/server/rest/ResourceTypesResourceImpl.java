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

package org.apache.directory.scim.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.ResourceTypesResource;
import org.apache.directory.scim.spec.protocol.data.ListResponse;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceType;

@ApplicationScoped
public class ResourceTypesResourceImpl implements ResourceTypesResource {

  private final Registry registry;

  private final RequestContext requestContext;

  @Inject
  public ResourceTypesResourceImpl(Registry registry, RequestContext requestContext) {
    this.registry = registry;
    this.requestContext = requestContext;
  }

  ResourceTypesResourceImpl() {
    this(null, null);
  }

  @Override
  public Response getAllResourceTypes(String filter) {
    
    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }

    Collection<ResourceType> resourceTypes = registry.getAllResourceTypes();
    
    for (ResourceType resourceType : resourceTypes) {
      Meta meta = new Meta();
      meta.setLocation(requestContext.getUriInfo().getAbsolutePathBuilder().path(resourceType.getName()).build().toString());
      meta.setResourceType(resourceType.getResourceType());
      
      resourceType.setMeta(meta);
    }
    
    ListResponse<ResourceType> listResponse = new ListResponse<>();
    listResponse.setItemsPerPage(resourceTypes.size());
    listResponse.setStartIndex(1);
    listResponse.setTotalResults(resourceTypes.size());
    
    List<ResourceType> objectList = new ArrayList<>(resourceTypes);
    listResponse.setResources(objectList);
    
    return Response.ok(listResponse).build();
  }

  @Override
  public Response getResourceType(String name) {
    ResourceType resourceType = registry.getResourceType(name);
    if (resourceType == null){
      return Response.status(Status.NOT_FOUND).build();  
    }
    
    Meta meta = new Meta();
    meta.setLocation(requestContext.getUriInfo().getAbsolutePath().toString());
    meta.setResourceType(resourceType.getResourceType());
    
    resourceType.setMeta(meta);
    
    return Response.ok(resourceType).build();
  }

}
