package edu.psu.swe.scim.server.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.ScimSpecSchema;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Slf4j
public class Registry {

  private Map<String, String> urnMap = new HashMap<>();
  private Map<String, Schema> schemaMap = new HashMap<>();
  
  public String getSchemaDoc(String urn){
    return urnMap.get(urn);
  }
  
  public List<String> getAllSchemaUrns(){
    return new ArrayList<String>(urnMap.values());
  }
  
  public void addSchema(String urn, Schema schema) throws JsonProcessingException{
    
    ObjectMapper objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector();
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    String schemaDoc = objectMapper.writeValueAsString(schema);
    
    schemaMap.put(urn, schema);
    urnMap.put(urn, schemaDoc);
  }
  
  public void addSchemaDoc(String urn, String schemaDoc){
    
    ObjectMapper objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector();
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    // Unmarshall the JSON document to a Schema and its associated object graph.
    try {
      Schema schema = objectMapper.readValue(schemaDoc, Schema.class);
      urnMap.put(urn, schemaDoc);
      schemaMap.put(urn, schema);
      
    } catch (Throwable t) {
      log.error("Unexpected Throwable was caught while unmarshalling JSON, schema " + urn + " will not be added: " + t.getLocalizedMessage());
    }
  }

  @PostConstruct
  @PostActivate
  private void loadSchemaMap() {
    for (String s : ScimSpecSchema.getSchemaNameSet()) {
      String schemaFile = "/schemas/" + s + ".json";
      InputStream is = Schema.class.getClassLoader().getResourceAsStream(schemaFile);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      StringBuilder sb = new StringBuilder();

      String line;
      try {
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      urnMap.put(s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf(".")), sb.toString());
    }
  }
}
