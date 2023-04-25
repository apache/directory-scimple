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

package org.apache.directory.scim.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.io.IOException;

/**
 * Creates and configures an {@link ObjectMapper} used for {@code application/scim+json} parsing.
 */
public class ObjectMapperFactory {

  private final static ObjectMapper objectMapper = createObjectMapper();

  /**
   * Returns an ObjectMapper configured for use with Jackson and Jakarta bindings.
   * This ObjectMapper does NOT unmarshal SCIM Extension values, use {@link #createObjectMapper(SchemaRegistry)} when
   * serializing REST response and requests.
   * @return an ObjectMapper configured for use with Jackson and Jakarta bindings.
   */
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    AnnotationIntrospector pair = new AnnotationIntrospectorPair(
      new JakartaXmlBindAnnotationIntrospector(objectMapper.getTypeFactory()),
      new JacksonAnnotationIntrospector());
    objectMapper.setAnnotationIntrospector(pair);

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return objectMapper;
  }

  /**
   * Creates and configures an {@link ObjectMapper} SCIM Resource in REST request and responses {@code application/scim+json}.
   */
  public static ObjectMapper createObjectMapper(SchemaRegistry schemaRegistry) {
    ObjectMapper objectMapper = createObjectMapper().copy();
    objectMapper.registerModule(new JakartaXmlBindAnnotationModule());
    objectMapper.registerModule(new ScimResourceModule(schemaRegistry));
    return objectMapper;
  }

  static class ScimResourceModule extends SimpleModule {

    private final SchemaRegistry schemaRegistry;

    public ScimResourceModule(SchemaRegistry schemaRegistry) {
      super("scim-resources", Version.unknownVersion());
      this.schemaRegistry = schemaRegistry;
      addDeserializer(ScimResource.class, new ScimResourceDeserializer(schemaRegistry));
    }

    @Override
    public void setupModule(SetupContext context) {
      super.setupModule(context);
      context.addDeserializationProblemHandler(new UnknownPropertyHandler(schemaRegistry));
    }
  }

  static class UnknownPropertyHandler extends DeserializationProblemHandler {

    private final SchemaRegistry schemaRegistry;

    UnknownPropertyHandler(SchemaRegistry schemaRegistry) {
      this.schemaRegistry = schemaRegistry;
    }

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {

      if (beanOrClass instanceof ScimResource) {
        ScimResource scimResource = (ScimResource) beanOrClass;
        Class<? extends ScimResource> resourceClass = scimResource.getClass();
        Class<? extends ScimExtension> extensionClass = schemaRegistry.getExtensionClass(resourceClass, propertyName);

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
