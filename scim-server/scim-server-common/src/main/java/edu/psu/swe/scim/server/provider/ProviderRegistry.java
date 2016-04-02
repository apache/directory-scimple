package edu.psu.swe.scim.server.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import edu.psu.swe.scim.server.exception.InvalidProviderException;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ResourceType;
import edu.psu.swe.scim.spec.schema.Schema;
import lombok.Data;

@Singleton
@Startup
@Data
public class ProviderRegistry {
  
  @Inject
  Registry registry;
  
  public Map<Class<? extends ScimResource>, Provider<? extends ScimResource>> providerMap = new HashMap<>();
  
  public <T extends ScimResource> void registerProvider(Class<T> clazz, Provider<T> provider) throws InvalidProviderException {
    ResourceType resourceType = generateResourceType(clazz, provider);
    generateSchemas(clazz, provider.getExtensionList());
    registry.addResourceType(resourceType);
    providerMap.put(clazz, provider);
  }

  @SuppressWarnings("unchecked")
  public <T extends ScimResource> Provider<T> getProvider(Class<T> clazz) {
    return (Provider<T>) providerMap.get(clazz);
  }
  
  private ResourceType generateResourceType(Class<? extends ScimResource> base, Provider<? extends ScimResource> provider) throws InvalidProviderException {

    ScimResourceType scimResourceType = base.getAnnotation(ScimResourceType.class);
    
    if (scimResourceType == null) {
      throw new InvalidProviderException("Missing annotation: ScimResourceType must be at the top of scim resource classes");
    }
    
    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.desription());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());
    
    List<Class<? extends ScimExtension>> extensionList = provider.getExtensionList();
    
    if (extensionList != null) {
    
      List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();
      
      for (Class<? extends ScimExtension> se : extensionList) {
        
        ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);
        
        if (extensionList == null) {
          throw new InvalidProviderException("Missing annotation: ScimExtensionType must be at the top of scim extension classes");
        }
        
        ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
        ext.setRequired(extensionType.required());
        ext.setSchemaUrn(extensionType.id());
        extensionSchemaList.add(ext);
      }
      
      resourceType.setSchemaExtensions(extensionSchemaList);
    }
    
    return resourceType;
  }
  
  
  private List<Schema> generateSchemas(Class<? extends ScimResource> base, List<Class<? extends ScimExtension>> extensionList) throws InvalidProviderException {
    ScimAttribute [] baseAttributes = base.getAnnotationsByType(ScimAttribute.class);
    
    if (baseAttributes.length == 0) {
      throw new InvalidProviderException("Missing annotation: cannot have a schema with no ScimAttribute values");
    }
    
    return null;
  }
   
//  private Provider<ScimGroup> groupProvider = null;
//  private Provider<ScimUser> userProvider = null;
}
