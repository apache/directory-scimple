package edu.psu.swe.scim.server.rest;

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

import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.resources.ScimResource;

@Provider
public class ObjectMapperContextResolver extends edu.psu.swe.commons.jaxrs.server.ObjectMapperContextResolver {

  private final ObjectMapper objectMapper;

  @Inject
  Registry registry;
  
  //Called through normal injection and calls Post Construct
  public ObjectMapperContextResolver() {
    super();
    objectMapper = getContext(null);
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
