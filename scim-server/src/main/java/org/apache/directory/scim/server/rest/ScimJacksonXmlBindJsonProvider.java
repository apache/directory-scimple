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

import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.apache.directory.scim.core.json.ObjectMapperFactory;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.Constants;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Adds JacksonJaxbJsonProvider for custom MediaType {@code application/scim+json} and application/json.
 */
@Provider
@Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
@Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
@ApplicationScoped
public class ScimJacksonXmlBindJsonProvider extends JacksonXmlBindJsonProvider {

  private static final Set<Package> SUPPORTED_PACKAGES = Set.of(ScimResource.class.getPackage(),
                                                                ListResponse.class.getPackage(),
                                                                ServiceProviderConfiguration.class.getPackage());

  public ScimJacksonXmlBindJsonProvider() {
    // CDI
  }

  @Inject
  public ScimJacksonXmlBindJsonProvider(SchemaRegistry schemaRegistry) {
    super(ObjectMapperFactory.createObjectMapper(schemaRegistry), DEFAULT_ANNOTATIONS);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return super.isReadable(type, genericType, annotations, mediaType)
      && SUPPORTED_PACKAGES.contains(type.getPackage());
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return super.isWriteable(type, genericType, annotations, mediaType)
      && SUPPORTED_PACKAGES.contains(type.getPackage());
  }
}
