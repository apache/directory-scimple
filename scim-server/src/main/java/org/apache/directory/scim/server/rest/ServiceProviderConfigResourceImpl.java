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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.utility.EtagGenerator;
import org.apache.directory.scim.spec.protocol.ServiceProviderConfigResource;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

@ApplicationScoped
public class ServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {

  private final ServerConfiguration serverConfiguration;

  private final EtagGenerator etagGenerator;

  @Inject
  public ServiceProviderConfigResourceImpl(ServerConfiguration serverConfiguration, EtagGenerator etagGenerator) {
    this.serverConfiguration = serverConfiguration;
    this.etagGenerator = etagGenerator;
  }

  @Override
  public Response getServiceProviderConfiguration(UriInfo uriInfo) {
    ServiceProviderConfiguration serviceProviderConfiguration = new ServiceProviderConfiguration();
    List<AuthenticationSchema> authenticationSchemas = serverConfiguration.getAuthenticationSchemas();
    BulkConfiguration bulk = serverConfiguration.getBulkConfiguration();
    SupportedConfiguration changePassword = serverConfiguration.getChangePasswordConfiguration();
    SupportedConfiguration etagConfig = serverConfiguration.getEtagConfiguration();
    FilterConfiguration filter = serverConfiguration.getFilterConfiguration();
    SupportedConfiguration patch = serverConfiguration.getPatchConfiguration();
    SupportedConfiguration sort = serverConfiguration.getSortConfiguration();
    String documentationUrl = serverConfiguration.getDocumentationUri();
    String externalId = serverConfiguration.getId();
    String id = serverConfiguration.getId();
    Meta meta = new Meta();
    String location = uriInfo.getAbsolutePath().toString();
    String resourceType = "ServiceProviderConfig";
    LocalDateTime now = LocalDateTime.now();

    meta.setCreated(now);
    meta.setLastModified(now);
    meta.setLocation(location);
    meta.setResourceType(resourceType);
    serviceProviderConfiguration.setAuthenticationSchemes(authenticationSchemas);
    serviceProviderConfiguration.setBulk(bulk);
    serviceProviderConfiguration.setChangePassword(changePassword);
    serviceProviderConfiguration.setDocumentationUrl(documentationUrl);
    serviceProviderConfiguration.setEtag(etagConfig);
    serviceProviderConfiguration.setExternalId(externalId);
    serviceProviderConfiguration.setFilter(filter);
    serviceProviderConfiguration.setId(id);
    serviceProviderConfiguration.setMeta(meta);
    serviceProviderConfiguration.setPatch(patch);
    serviceProviderConfiguration.setSort(sort);
    
    try {
      EntityTag etag = etagGenerator.generateEtag(serviceProviderConfiguration);
      return Response.ok(serviceProviderConfiguration).tag(etag).build();
    } catch (JsonProcessingException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
      return createETagErrorResponse();
    }
  }
  
  private Response createETagErrorResponse() {
    ErrorResponse er = new ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Failed to generate the etag");
    return er.toResponse();
  }
}
