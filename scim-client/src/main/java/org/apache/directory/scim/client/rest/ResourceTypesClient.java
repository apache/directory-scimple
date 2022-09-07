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

package org.apache.directory.scim.client.rest;

import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.apache.directory.scim.protocol.ResourceTypesResource;
import org.apache.directory.scim.spec.schema.ResourceType;

public class ResourceTypesClient implements AutoCloseable {

  private static final GenericType<List<ResourceType>> LIST_RESOURCE_TYPE = new GenericType<List<ResourceType>>(){};

  private final Client client;
  private final WebTarget target;
  private final ResourceTypesResourceClient resourceTypesResourceClient = new ResourceTypesResourceClient();

  public ResourceTypesClient(Client client, String baseUrl) {
    this.client = client;
    this.target = this.client.target(baseUrl).path("ResourceTypes");
  }

  public List<ResourceType> getAllResourceTypes(String filter) throws RestException {
    List<ResourceType> resourceTypes;
    Response response = this.resourceTypesResourceClient.getAllResourceTypes(filter);

    try {
      RestClientUtil.checkForSuccess(response);

      resourceTypes = response.readEntity(LIST_RESOURCE_TYPE);
    } finally {
      RestClientUtil.close(response);
    }
    return resourceTypes;
  }

  public Optional<ResourceType> getResourceType(String name) throws RestException, ProcessingException, IllegalStateException {
    Optional<ResourceType> resourceType;
    Response response = this.resourceTypesResourceClient.getResourceType(name);

    try {
      resourceType = RestClientUtil.readEntity(response, ResourceType.class);
    } finally {
      RestClientUtil.close(response);
    }
    return resourceType;
  }

  @Override
  public void close() throws Exception {
    this.client.close();
  }

  private class ResourceTypesResourceClient implements ResourceTypesResource {

    @Override
    public Response getAllResourceTypes(String filter) throws RestException {
      Response response = ResourceTypesClient.this.target
          .queryParam("filter", filter)
          .request("application/scim+json")
          .get();

      return response;
    }

    @Override
    public Response getResourceType(String name) throws RestException {
      Response response = ResourceTypesClient.this.target
          .path(name)
          .request("application/scim+json")
          .get();

      return response;
    }
  }
}
