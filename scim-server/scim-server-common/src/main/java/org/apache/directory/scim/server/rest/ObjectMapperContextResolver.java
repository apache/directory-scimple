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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.resources.ScimResource;

@Provider
public class ObjectMapperContextResolver extends edu.psu.swe.commons.jaxrs.server.ObjectMapperContextResolver {

  private final ObjectMapper objectMapper;

  @Inject
  Registry registry;
  
  //Called through normal injection and calls Post Construct
  public ObjectMapperContextResolver() {
    super();
    objectMapper = super.getContext(null);
  }
  
  //Not call through container context and therefore must manually call postConstruct method
  public ObjectMapperContextResolver(Registry registry) {
    this();
    this.registry = registry;
    postConstruct();
  }

  @PostConstruct
  protected void postConstruct() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(ScimResource.class, new ScimResourceDeserializer(this.registry, this.objectMapper));
    this.objectMapper.registerModule(module);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }

}
