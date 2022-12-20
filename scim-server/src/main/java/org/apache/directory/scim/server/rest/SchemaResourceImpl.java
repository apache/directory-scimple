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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.directory.scim.protocol.SchemaResource;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.core.schema.SchemaRegistry;

@ApplicationScoped
public class SchemaResourceImpl implements SchemaResource {

  private final SchemaRegistry schemaRegistry;

  @Inject
  public SchemaResourceImpl(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public SchemaResourceImpl() {
    // CDI
    this(null);
  }

  @Override
  public Response getAllSchemas(String filter, UriInfo uriInfo) {

    if (filter != null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    ListResponse<Schema> listResponse = new ListResponse<>();
    Collection<Schema> schemas = schemaRegistry.getAllSchemas();
    
    for (Schema schema : schemas) {
      Meta meta = new Meta();
      meta.setLocation(uriInfo.getAbsolutePathBuilder().path(schema.getId()).build().toString());
      meta.setResourceType(Schema.RESOURCE_NAME);
      
      schema.setMeta(meta);
    }
    
    listResponse.setItemsPerPage(schemas.size());
    listResponse.setStartIndex(1);
    listResponse.setTotalResults(schemas.size());
    
    List<Schema> objectList = new ArrayList<>(schemas);
    listResponse.setResources(objectList);
    
    return Response.ok(listResponse).build();
  }

  @Override
  public Response getSchema(String urn, UriInfo uriInfo) {
    
    Schema schema = schemaRegistry.getSchema(urn);
    if (schema == null){
      return Response.status(Status.NOT_FOUND).build();  
    }
    
    Meta meta = new Meta();
    meta.setLocation(uriInfo.getAbsolutePath().toString());
    meta.setResourceType(Schema.RESOURCE_NAME);
    
    schema.setMeta(meta);
    
    return Response.ok(schema).build();
    
  }
}
