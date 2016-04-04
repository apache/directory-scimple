package edu.psu.swe.scim.server.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ResourceType;
import edu.psu.swe.scim.spec.schema.Schema;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Slf4j
public class Registry {

  private Map<String, Schema> schemaMap = new HashMap<>();
  
  private Map<String, ResourceType> resourceTypeMap = new HashMap<>();
  
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
    log.info("Adding schema " + schema.getId() + " into the registry");
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

  public ResourceType getResourceType(String name) {
    return resourceTypeMap.get(name);
  }
  
  public Collection<ResourceType> getAllResourceTypes() {
    return Collections.unmodifiableCollection(resourceTypeMap.values());
  }
  
  public void addResourceType(ResourceType resourceType) {
    resourceTypeMap.put(resourceType.getName(), resourceType);
  }
  
  public void addResourceType(Class<? extends ScimResource> resourceTypeClass) {
    addResourceType(resourceTypeClass, Collections.emptyList());
  }
  
  public void addResourceType(Class<? extends ScimResource> resourceTypeClass, List<Class<? extends ScimExtension>> extensions) {
    ResourceType resourceType = createResourceType(resourceTypeClass);
    for (Class<? extends ScimExtension> extensionClass : extensions) {
      ScimExtension extension;
//      resourceType.addExtension(extension.getUrn(), extension);

    }
//    resourceType.setExten
  }
  
  private ResourceType createResourceType(Class<? extends ScimResource> resourceTypeClass) {
    ScimResourceType annotation = resourceTypeClass.getAnnotation(ScimResourceType.class);
    
    if (annotation == null) {
      log.error(resourceTypeClass + " must be annotated with the @ScimResourceType annotation.");
      return null;
    }
    
    ResourceType resourceType = new ResourceType(annotation);
    return resourceType;
  }
  
//  private ScimExtension createResourceType(Class<? extends ScimResource> resourceTypeClass) {

}
