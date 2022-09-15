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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.io.IOException;

/**
 * Creates and configures an {@link ObjectMapper} used for {@code application/scim+json} parsing.
 */
@Provider
public class ObjectMapperFactory {

  private final SchemaRegistry schemaRegistry;

  @Inject
  public ObjectMapperFactory(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  @Produces
  public ObjectMapper createObjectMapper() {

    ObjectMapper objectMapper = org.apache.directory.scim.spec.json.ObjectMapperFactory.getObjectMapper().copy();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    objectMapper.registerModule(new ScimResourceModule(schemaRegistry));
    return objectMapper;
  }

  static class ScimResourceModule extends SimpleModule {

    public ScimResourceModule(SchemaRegistry schemaRegistry) {
      super("scim-resources", Version.unknownVersion());
      addDeserializer(ScimResource.class, new ScimResourceDeserializer(schemaRegistry));
    }

    @Override
    public void setupModule(SetupContext context) {
      super.setupModule(context);
      context.addDeserializationProblemHandler(new UnknownPropertyHandler());
    }
  }

  static class UnknownPropertyHandler extends DeserializationProblemHandler {
    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {

      if (beanOrClass instanceof ScimResource) {
        ScimResource scimResource = (ScimResource) beanOrClass;
        Class<? extends ScimResource> resourceClass = scimResource.getClass();
        Class<? extends ScimExtension> extensionClass = ScimExtensionRegistry.getInstance().getExtensionClass(resourceClass, propertyName);

        if (extensionClass != null) {
          ScimExtension ext = ctxt.readPropertyValue(p, null, extensionClass);
          if (ext != null) {
            scimResource.addExtension(ext);
          }
        }
      }
      return super.handleUnknownProperty(ctxt, p, deserializer, beanOrClass, propertyName);
    }
  }

}
