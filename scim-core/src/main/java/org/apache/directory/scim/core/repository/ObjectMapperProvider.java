package org.apache.directory.scim.core.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

public class ObjectMapperProvider {
  protected static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new JakartaXmlBindAnnotationModule());

    AnnotationIntrospector pair = new AnnotationIntrospectorPair(
      new JakartaXmlBindAnnotationIntrospector(objectMapper.getTypeFactory()),
      new JacksonAnnotationIntrospector());
    objectMapper.setAnnotationIntrospector(pair);

    return objectMapper;
  }
}
