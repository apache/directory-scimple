package edu.psu.swe.scim.server.provider;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

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
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Data
@Slf4j
public class ProviderRegistry {

  @Inject
  Registry registry;

  public Map<Class<? extends ScimResource>, Provider<? extends ScimResource>> providerMap = new HashMap<>();

  public <T extends ScimResource> void registerProvider(Class<T> clazz, Provider<T> provider) throws InvalidProviderException, JsonProcessingException {
    ResourceType resourceType = generateResourceType(clazz, provider);

    log.debug("Calling addSchema on the base");
    registry.addSchema(generateSchema(clazz));

    List<Class<? extends ScimExtension>> extensionList = provider.getExtensionList();

    Iterator<Class<? extends ScimExtension>> iter = extensionList.iterator();

    while (iter.hasNext()) {
      log.debug("Calling addSchema on an extension");
      registry.addSchema(generateSchema(iter.next()));
    }

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

  private Schema generateSchema(Class<?> clazz) {

    //List<Field> fieldList = getFieldsUpTo(clazz, BaseResource.class);
    
    Field [] fieldList = clazz.getDeclaredFields();

    Schema schema = new Schema();

    ScimResourceType srt = clazz.getAnnotation(ScimResourceType.class);
    ScimExtensionType set = clazz.getAnnotation(ScimExtensionType.class);

    if (srt == null && set == null) {
      // TODO - throw?
      log.error("Neither a ScimResourceType or ScimExtensionType annotation found");
    }

    log.info("calling set attributes with " + fieldList.length + " fields");
    schema.setAttributes(createAttributes(fieldList));

    if (srt != null) {
      schema.setId(srt.schema());
      schema.setDescription(srt.desription());
      schema.setName(srt.name());
    } else {
      schema.setId(set.id());
      schema.setDescription(set.description());
      schema.setName(set.name());
    }

    return schema;
  }

  private List<Attribute> createAttributes(Field[] fieldList) {
    List<Attribute> attributeList = new ArrayList<>();

    for (Field f : fieldList) {
      ScimAttribute sa = f.getAnnotation(ScimAttribute.class);

      log.debug("Processing field " + f.getName());
      if (sa == null) {
        log.warn("Attribute " + f.getName() + " did not have a ScimAttribute annotation");
        continue;
      }

      Attribute attribute = new Attribute();
      attribute.setField(f);
      
      List<String> cononicalTypes = Arrays.asList(sa.canonicalValues());
      
      //If we just have the default single empty string, set to null
      if (cononicalTypes.isEmpty() || (cononicalTypes.size() == 1 && cononicalTypes.get(0).isEmpty())) {
        log.info("### Setting canonical values to null");
        attribute.setCanonicalValues(null);
      } else {
        log.info("### Canonical types has " + cononicalTypes.size() + " entries and the first is " + cononicalTypes.get(0));
        attribute.setCanonicalValues(new HashSet<String>(Arrays.asList(sa.canonicalValues())));
      }
      
      attribute.setCaseExact(sa.caseExact());
      attribute.setDescription(sa.description());

      if (Collection.class.isAssignableFrom(f.getType()) || f.getType().isArray()) {
        attribute.setMultiValued(true);
      } else {
        attribute.setMultiValued(false);
      }

      if (sa.name() == null || sa.name().isEmpty()) {
        attribute.setName(f.getName());
      } else {
        attribute.setName(sa.name());
      }
      
      attribute.setMutability(sa.mutability());
      
      List<String> refType = Arrays.asList(sa.referenceTypes());
      
      //If we just have the default single empty string, set to null
      if (refType.isEmpty() || (refType.size() == 1 && refType.get(0).isEmpty())) {
        attribute.setReferenceTypes(null);
      } else {
        attribute.setReferenceTypes(Arrays.asList(sa.referenceTypes()));
      }
      
      attribute.setRequired(sa.required());
      attribute.setReturned(sa.returned());
      attribute.setType(sa.type());
      attribute.setUniqueness(sa.uniqueness());

      if (sa.type().equals(Type.COMPLEX)) {
        if (!attribute.isMultiValued()) {
          //attribute.setSubAttributes(addAttributes(getFieldsUpTo(f.getType(), BaseResource.class)));
          attribute.setSubAttributes(createAttributes(f.getType().getDeclaredFields()));
        } else if (f.getType().isArray()) {
          Class<?> componentType = f.getType().getComponentType();
          //attribute.setSubAttributes(addAttributes(getFieldsUpTo(componentType, BaseResource.class)));
          attribute.setSubAttributes(createAttributes(componentType.getDeclaredFields()));
        } else {
          ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
          Class<?> attributeContainedClass = (Class<?>) stringListType.getActualTypeArguments()[0];
          //attribute.setSubAttributes(addAttributes(getFieldsUpTo(attributeContainedClass, BaseResource.class)));
          attribute.setSubAttributes(createAttributes(attributeContainedClass.getDeclaredFields()));
        }
      }
      attributeList.add(attribute);
    }

    log.debug("Returning " + attributeList.size() + " attributes");
    return attributeList;
  }

  public static List<Field> getFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
    List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
    Class<?> parentClass = startClass.getSuperclass();
    if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
      List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
      currentClassFields.addAll(parentClassFields);
    }
    return currentClassFields;
  }
  
  // private Provider<ScimGroup> groupProvider = null;
  // private Provider<ScimUser> userProvider = null;
}
