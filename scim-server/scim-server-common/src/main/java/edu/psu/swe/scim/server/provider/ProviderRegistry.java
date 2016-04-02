package edu.psu.swe.scim.server.provider;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
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
        
    Field [] baseFieldList = base.getFields();
    
    Schema baseSchema = new Schema();
    
    baseSchema.setAttributes(addAttributes(baseFieldList));
   
    
    return null;
  }
   
  private List<Attribute> addAttributes(Field [] fieldList) {
    List<Attribute> attributeList = new ArrayList<>();
    
    for (Field f : fieldList) {
    	ScimAttribute sa = f.getAnnotation(ScimAttribute.class);
    	
    	if (sa == null) {
    		continue;
    	}
    	
    	Attribute attribute = new Attribute();
        attribute.setCanonicalValues(new HashSet<String>(Arrays.asList(sa.canonicalValues())));
        attribute.setCaseExact(sa.caseExact());
        attribute.setDescription(sa.description());
        
        if (Collection.class.isAssignableFrom(f.getType()) || f.getType().isArray()) {
          attribute.setMultiValued(true);
        } else {
          attribute.setMultiValued(false);
        }
        
        attribute.setMutability(sa.mutability());
        attribute.setName(sa.name());
        attribute.setReferenceTypes(Arrays.asList(sa.referenceTypes()));
        attribute.setRequired(sa.required());
        attribute.setReturned(sa.returned());
        attribute.setType(sa.type());
        attribute.setUniqueness(sa.uniqueness());
        
    	if (sa.type().equals(Type.COMPLEX)) {
    	  if (!attribute.isMultiValued()) {
    		attribute.setSubAttributes(addAttributes(f.getType().getFields()));
    	  } else if (f.getType().isArray()){
    		 Class<?> componentType = f.getType().getComponentType();
    		 attribute.setSubAttributes(addAttributes(componentType.getFields()));
    	  } else {
    	     ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
    	     Class<?> attributeContainedClass = (Class<?>) stringListType.getActualTypeArguments()[0];
    	     attribute.setSubAttributes(addAttributes(attributeContainedClass.getFields()));
    	  }
    	}
    	attributeList.add(attribute);
    }
    
    return attributeList;
  }
//  private Provider<ScimGroup> groupProvider = null;
//  private Provider<ScimUser> userProvider = null;
}
