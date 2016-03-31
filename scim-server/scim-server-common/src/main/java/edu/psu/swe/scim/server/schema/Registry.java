package edu.psu.swe.scim.server.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.ScimSpecSchema;

@Singleton
@Startup
@Slf4j
public class Registry {

  private Map<String, Schema> schemaMap = new HashMap<>();
  
  private ObjectMapper objectMapper;

  public Schema getSchema(String urn) {
    return schemaMap.get(urn);
  }

  public Set<String> getAllSchemaUrns() {
    return Collections.unmodifiableSet(schemaMap.keySet());
  }

  public Collection<Schema> getAllSchemas() {
    return Collections.unmodifiableCollection(schemaMap.values());
  }

  public void addSchema(Schema schema) throws JsonProcessingException {
    schemaMap.put(schema.getId(), schema);
  }

  public void addSchemaDoc(String schemaDoc) {
    // Unmarshall the JSON document to a Schema and its associated object graph.
    try {
      Schema schema = objectMapper.readValue(schemaDoc, Schema.class);
      schemaMap.put(schema.getId(), schema);
    } catch (Throwable t) {
      log.error("Unexpected Throwable was caught while unmarshalling JSON, schema will not be added: " + t.getLocalizedMessage());
    }
  }

  @PostConstruct
  @PostActivate
  private void loadSchemaMap() {
    objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    for (String s : ScimSpecSchema.getSchemaNameSet()) {
      String schemaFile = "/schemas/" + s + ".json";
      log.debug("Attempting to load schema file: " + schemaFile);
      
      try (InputStream is = Schema.class.getClassLoader().getResourceAsStream(schemaFile)) {
        Schema schema = objectMapper.readValue(is, Schema.class);
        schemaMap.put(schema.getId(), schema);
      } catch (IOException e) {
        log.error("Unable to load schema from: " + schemaFile, e);
        continue;
      }
    }
  }
}
