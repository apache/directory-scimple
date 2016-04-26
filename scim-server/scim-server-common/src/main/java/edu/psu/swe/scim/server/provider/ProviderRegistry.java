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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

import edu.psu.swe.scim.server.exception.InvalidProviderException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.extension.ScimExtensionRegistry;
import edu.psu.swe.scim.spec.resources.BaseResource;
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

  private static final String STRING_TYPE_IDENTIFIER = "class java.lang.String";
  private static final String CHARACTER_ARRAY_TYPE_IDENTIFIER = "class [C";
  private static final String BIG_C_CHARACTER_ARRAY_TYPE_IDENTIFIER = "class [Ljava.lang.Character;";
  private static final String INT_TYPE_IDENTIFIER = "int";
  private static final String INTEGER_TYPE_IDENTIFIER = "class java.lang.Integer";
  private static final String FLOAT_TYPE_IDENTIFIER = "float";
  private static final String BIG_F_FLOAT_TYPE_IDENTIFIER = "class java.lang.Float";
  private static final String DOUBLE_TYPE_IDENTIFIER = "double";
  private static final String BIG_D_DOUBLE_TYPE_IDENTIFIER = "class java.lang.Double";
  private static final String BOOLEAN_TYPE_IDENTIFIER = "boolean";
  private static final String BIG_B_BOOLEAN_TYPE_IDENTIFIER = "class java.lang.Boolean";
  private static final String LOCAL_TIME_TYPE_IDENTIFER = "class java.time.LocalTime";
  private static final String LOCAL_DATE_TYPE_IDENTIFER = "class java.time.LocalDate";
  private static final String LOCAL_DATE_TIME_TYPE_IDENTIFIER = "class java.time.LocalDateTime";
  private static final String DATE_TYPE_IDENTIFIER = "class java.util.Date";
  private static final String BYTE_ARRAY_TYPE_IDENTIFIER = "class [B";
  private static final String RESOURCE_REFERENCE_TYPE_IDENTIFIER = "class edu.psu.swe.scim.spec.schema.ResourceReference$ReferenceType";

  @Inject
  Registry registry;

  @Inject
  ScimExtensionRegistry scimExtensionRegistry;

  private Map<Class<? extends ScimResource>, Instance<? extends Provider<? extends ScimResource>>> providerMap = new HashMap<>();

  public <T extends ScimResource> void registerProvider(Class<T> clazz, Instance<? extends Provider<T>> providerInstance) throws InvalidProviderException, JsonProcessingException, UnableToRetrieveExtensionsException {

    Provider<T> provider = providerInstance.get();

    ResourceType resourceType = generateResourceType(clazz, provider);

    log.debug("Calling addSchema on the base");
    registry.addSchema(generateSchema(clazz));
    ScimResource newScimResourceInstance;
    try {
      newScimResourceInstance = clazz.newInstance();
      log.info("Registering a provider of type " + newScimResourceInstance.getResourceType());
    } catch (InstantiationException | IllegalAccessException e) {
      throw new InvalidProviderException(e.getMessage());
    }
    String schemaUrn = newScimResourceInstance.getBaseUrn();
    registry.addScimResourceSchemaUrn(schemaUrn, clazz);

    List<Class<? extends ScimExtension>> extensionList = provider.getExtensionList();

    if (extensionList != null) {
      for (Class<? extends ScimExtension> scimExtension : extensionList) {
        ScimExtension newScimExtensionInstance;
        try {
          newScimExtensionInstance = scimExtension.newInstance();
          log.info("Registering a extension of type " + newScimExtensionInstance.getUrn());
        } catch (InstantiationException | IllegalAccessException e) {
          throw new InvalidProviderException(e.getMessage());
        }

        scimExtensionRegistry.registerExtension(clazz, newScimExtensionInstance);
      }

      Iterator<Class<? extends ScimExtension>> iter = extensionList.iterator();

      while (iter.hasNext()) {
        log.debug("Calling addSchema on an extension");
        registry.addSchema(generateSchema(iter.next()));
      }
    }

    registry.addResourceType(resourceType);
    providerMap.put(clazz, providerInstance);
  }

  @SuppressWarnings("unchecked")
  public <T extends ScimResource> Provider<T> getProvider(Class<T> clazz) {
    Instance<? extends Provider<? extends ScimResource>> providerInstance = providerMap.get(clazz);
    if (providerInstance == null) {
      return null;
    }

    return (Provider<T>) providerInstance.get();
  }

  private ResourceType generateResourceType(Class<? extends ScimResource> base, Provider<? extends ScimResource> provider) throws InvalidProviderException, UnableToRetrieveExtensionsException {

    ScimResourceType scimResourceType = base.getAnnotation(ScimResourceType.class);

    if (scimResourceType == null) {
      throw new InvalidProviderException("Missing annotation: ScimResourceType must be at the top of scim resource classes");
    }

    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
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

  public static Schema generateSchema(Class<?> clazz) throws InvalidProviderException {
    List<Field> fieldList = getFieldsUpTo(clazz, BaseResource.class);

    // Field [] fieldList = clazz.getDeclaredFields();

    Schema schema = new Schema();

    ScimResourceType srt = clazz.getAnnotation(ScimResourceType.class);
    ScimExtensionType set = clazz.getAnnotation(ScimExtensionType.class);

    if (srt == null && set == null) {
      // TODO - throw?
      log.error("Neither a ScimResourceType or ScimExtensionType annotation found");
    }

    log.info("calling set attributes with " + fieldList.size() + " fields");
    Set<String> invalidAttributes = new HashSet<>();
    schema.setAttributes(createAttributes(fieldList, invalidAttributes, clazz.getSimpleName()));

    if (!invalidAttributes.isEmpty()) {
      StringBuilder sb = new StringBuilder();

      sb.append("Scim attributes cannot be primitive types unless they are required.  The following values were found that are primitive and not required\n\n");

      for (String s : invalidAttributes) {
        sb.append(s);
        sb.append("\n");
      }

      throw new InvalidProviderException(sb.toString());
    }

    if (srt != null) {
      schema.setId(srt.schema());
      schema.setDescription(srt.description());
      schema.setName(srt.name());
    } else {
      schema.setId(set.id());
      schema.setDescription(set.description());
      schema.setName(set.name());
    }

    return schema;
  }

  private static List<Attribute> createAttributes(List<Field> fieldList, Set<String> invalidAttributes, String nameBase) {
    List<Attribute> attributeList = new ArrayList<>();

    for (Field f : fieldList) {
      ScimAttribute sa = f.getAnnotation(ScimAttribute.class);

      log.debug("Processing field " + f.getName());
      if (sa == null) {
        log.warn("Attribute " + f.getName() + " did not have a ScimAttribute annotation");
        continue;
      }

      String attributeName;

      if (sa.name() == null || sa.name().isEmpty()) {
        attributeName = f.getName();
      } else {
        attributeName = sa.name();
      }

      if (f.getType().isPrimitive() && sa.required() == false) {
        invalidAttributes.add(nameBase + "." + attributeName);
        continue;
      }

      Attribute attribute = new Attribute();
      attribute.setField(f);
      attribute.setName(attributeName);
      List<String> cononicalTypes = Arrays.asList(sa.canonicalValues());

      // If we just have the default single empty string, set to null
      if (cononicalTypes.isEmpty() || (cononicalTypes.size() == 1 && cononicalTypes.get(0).isEmpty())) {
        attribute.setCanonicalValues(null);
      } else {
        attribute.setCanonicalValues(new HashSet<String>(Arrays.asList(sa.canonicalValues())));
      }

      attribute.setCaseExact(sa.caseExact());
      attribute.setDescription(sa.description());

      String typeName = null;
      if (Collection.class.isAssignableFrom(f.getType())) {
        log.debug("We have a collection");
        ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
        Class<?> attributeContainedClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        typeName = attributeContainedClass.getTypeName();
        attribute.setMultiValued(true);
      } else if (f.getType().isArray()) {
        log.debug("We have an array");
        Class<?> componentType = f.getType().getComponentType();
        typeName = componentType.getTypeName();
        attribute.setMultiValued(true);
      } else {
        typeName = f.getType().toString();
        attribute.setMultiValued(false);
      }

      // attribute.setType(sa.type());
      log.info("Attempting to set the attribute type, raw value = " + typeName);
      switch (typeName) {
      case STRING_TYPE_IDENTIFIER:
      case CHARACTER_ARRAY_TYPE_IDENTIFIER:
      case BIG_C_CHARACTER_ARRAY_TYPE_IDENTIFIER:
        log.debug("Setting type to String");
        attribute.setType(Type.STRING);
        break;
      case INT_TYPE_IDENTIFIER:
      case INTEGER_TYPE_IDENTIFIER:
        log.debug("Setting type to integer");
        attribute.setType(Type.INTEGER);
        break;
      case FLOAT_TYPE_IDENTIFIER:
      case BIG_F_FLOAT_TYPE_IDENTIFIER:
      case DOUBLE_TYPE_IDENTIFIER:
      case BIG_D_DOUBLE_TYPE_IDENTIFIER:
        log.debug("Setting type to decimal");
        attribute.setType(Type.DECIMAL);
        break;
      case BOOLEAN_TYPE_IDENTIFIER:
      case BIG_B_BOOLEAN_TYPE_IDENTIFIER:
        log.debug("Setting type to boolean");
        attribute.setType(Type.BOOLEAN);
        break;
      case BYTE_ARRAY_TYPE_IDENTIFIER:
        log.debug("Setting type to binary");
        attribute.setType(Type.BINARY);
        break;
      case DATE_TYPE_IDENTIFIER:
      case LOCAL_DATE_TIME_TYPE_IDENTIFIER:
      case LOCAL_TIME_TYPE_IDENTIFER:
      case LOCAL_DATE_TYPE_IDENTIFER:
        log.debug("Setting type to date time");
        attribute.setType(Type.DATE_TIME);
        break;
      case RESOURCE_REFERENCE_TYPE_IDENTIFIER:
        log.debug("Setting type to reference");
        attribute.setType(Type.REFERENCE);
        break;
      default:
        log.debug("Setting type to complex");
        attribute.setType(Type.COMPLEX);
      }
      
      attribute.setMutability(sa.mutability());

      List<String> refType = Arrays.asList(sa.referenceTypes());

      // If we just have the default single empty string, set to null
      if (refType.isEmpty() || (refType.size() == 1 && refType.get(0).isEmpty())) {
        attribute.setReferenceTypes(null);
      } else {
        attribute.setReferenceTypes(Arrays.asList(sa.referenceTypes()));
      }

      attribute.setRequired(sa.required());
      attribute.setReturned(sa.returned());
      attribute.setUniqueness(sa.uniqueness());

      //if (sa.type().equals(Type.COMPLEX))
      if (attribute.getType() == Type.COMPLEX) {
        if (!attribute.isMultiValued()) {
          // attribute.setSubAttributes(addAttributes(getFieldsUpTo(f.getType(),
          // BaseResource.class)));
          attribute.setSubAttributes(createAttributes(Arrays.asList(f.getType().getDeclaredFields()), invalidAttributes, nameBase + "." + f.getName()));
        } else if (f.getType().isArray()) {
          Class<?> componentType = f.getType().getComponentType();
          // attribute.setSubAttributes(addAttributes(getFieldsUpTo(componentType,
          // BaseResource.class)));
          attribute.setSubAttributes(createAttributes(Arrays.asList(componentType.getDeclaredFields()), invalidAttributes, nameBase + "." + componentType.getSimpleName()));
        } else {
          ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
          Class<?> attributeContainedClass = (Class<?>) stringListType.getActualTypeArguments()[0];
          // attribute.setSubAttributes(addAttributes(getFieldsUpTo(attributeContainedClass,
          // BaseResource.class)));
          attribute.setSubAttributes(createAttributes(Arrays.asList(attributeContainedClass.getDeclaredFields()), invalidAttributes, nameBase + "." + attributeContainedClass.getSimpleName()));
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
