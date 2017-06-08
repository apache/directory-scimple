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

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlEnumValue;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.psu.swe.scim.common.ScimUtils;
import edu.psu.swe.scim.server.exception.InvalidProviderException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.annotation.ScimResourceIdReference;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.annotation.ScimType;
import edu.psu.swe.scim.spec.extension.ScimExtensionRegistry;
import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ResourceType;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.AddAction;
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
  
  public ProviderRegistry() {}
  
  public ProviderRegistry(Registry registry, ScimExtensionRegistry scimExtensionRegistry) {
    this.registry = registry;
    this.scimExtensionRegistry = scimExtensionRegistry;
  }

  private Map<Class<? extends ScimResource>, Instance<? extends Provider<? extends ScimResource>>> providerMap = new HashMap<>();

  public <T extends ScimResource> void registerProvider(Class<T> clazz, Instance<? extends Provider<T>> providerInstance) throws InvalidProviderException, JsonProcessingException, UnableToRetrieveExtensionsException {

    Provider<T> provider = providerInstance.get();

    ResourceType resourceType = generateResourceType(clazz, provider);

    log.info("Calling addSchema on the base class: {}", clazz);
    registry.addSchema(generateBaseSchema(clazz));
    // NOTE generateResourceType() ensures ScimResourceType exists
    ScimResourceType scimResourceType = clazz.getAnnotation(ScimResourceType.class);
    String schemaUrn = scimResourceType.schema();
    String endpoint = scimResourceType.endpoint();
    registry.addScimResourceSchemaUrn(schemaUrn, clazz);
    registry.addScimResourceEndPoint(endpoint, clazz);

    List<Class<? extends ScimExtension>> extensionList = provider.getExtensionList();

    if (extensionList != null) {
      for (Class<? extends ScimExtension> scimExtension : extensionList) {
        log.info("Registering a extension of type: " + scimExtension);
        scimExtensionRegistry.registerExtension(clazz, scimExtension);
        
        log.info("Calling addSchema on an extension: " + scimExtension);
        registry.addSchema(generateExtensionSchema(scimExtension));
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

        if (extensionType == null) {
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

  public static Schema generateBaseSchema(Class<?> clazz) throws InvalidProviderException {
    List<Field> fieldList = ScimUtils.getFieldsUpTo(clazz, BaseResource.class);

    return generateSchema(clazz, fieldList);
  }
  
  public static Schema generateExtensionSchema(Class<?> clazz) throws InvalidProviderException {
    log.debug("----> In generateExtensionSchema");
    
    return generateSchema(clazz, ScimUtils.getFieldsUpTo(clazz, Object.class));
  }
  
  public static Schema generateSchema(Class<?> clazz, List<Field> fieldList) throws InvalidProviderException {

    // Field [] fieldList = clazz.getDeclaredFields();

    Schema schema = new Schema();

    ScimResourceType srt = clazz.getAnnotation(ScimResourceType.class);
    ScimExtensionType set = clazz.getAnnotation(ScimExtensionType.class);

    if (srt == null && set == null) {
      // TODO - throw?
      log.error("Neither a ScimResourceType or ScimExtensionType annotation found");
    }

    log.debug("calling set attributes with " + fieldList.size() + " fields");
    Set<String> invalidAttributes = new HashSet<>();
    List<Attribute> createAttributes = createAttributes(fieldList, invalidAttributes, clazz.getSimpleName());
    schema.setAttributes(createAttributes);

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

  private static List<Attribute> createAttributes(List<Field> fieldList, Set<String> invalidAttributes, String nameBase) throws InvalidProviderException {
    List<Attribute> attributeList = new ArrayList<>();

    for (Field f : fieldList) {
      ScimAttribute sa = f.getAnnotation(ScimAttribute.class);

      log.debug("++++++++++++++++++++ Processing field " + f.getName());
      if (sa == null) {
        log.debug("Attribute " + f.getName() + " did not have a ScimAttribute annotation");
        continue;
      }

      String attributeName;
      f.setAccessible(true);

      if (sa.name() == null || sa.name().isEmpty()) {
        attributeName = f.getName();
      } else {
        attributeName = sa.name();
      }

      if (f.getType().isPrimitive() && !sa.required()) {
        invalidAttributes.add(nameBase + "." + attributeName);
        continue;
      }

      //TODO - Fix this to look for the two types of canonical attributes
      Attribute attribute = new Attribute();
      attribute.setField(f);
      attribute.setName(attributeName);
      
      List<String> canonicalTypes = null;
      Field [] enumFields = sa.canonicalValueEnum().getFields();
      log.debug("Gathered fields of off the enum, there are " + enumFields.length + " " + sa.canonicalValueEnum().getName());
      
      if (enumFields.length != 0) {
        
        //This looks goofy, but there's always at least the default value, so it's not an empty list
        if (sa.canonicalValueList().length != 1 && !sa.canonicalValueList()[0].isEmpty()) {
          throw new InvalidProviderException("You cannont set both the canonicalEnumValue and canonicalValueList attributes on the same ScimAttribute");
        }
        
        canonicalTypes = new ArrayList<>();

        for (Field field : enumFields) {
          XmlEnumValue [] annotation = field.getAnnotationsByType(XmlEnumValue.class);
          
          if (annotation.length != 0) {
            canonicalTypes.add(annotation[0].value());
          } else {
            canonicalTypes.add(field.getName());
          }
        }
      } else {
        canonicalTypes = Arrays.asList(sa.canonicalValueList());
      }

      // If we just have the default single empty string, set to null
      if (canonicalTypes.isEmpty() || (canonicalTypes.size() == 1 && canonicalTypes.get(0).isEmpty())) {
        attribute.setCanonicalValues(null);
      } else {
        attribute.setCanonicalValues(new HashSet<String>(canonicalTypes));
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
      boolean attributeIsAString = false;
      log.debug("Attempting to set the attribute type, raw value = " + typeName);
      switch (typeName) {
      case STRING_TYPE_IDENTIFIER:
      case CHARACTER_ARRAY_TYPE_IDENTIFIER:
      case BIG_C_CHARACTER_ARRAY_TYPE_IDENTIFIER:
        log.debug("Setting type to String");
        attribute.setType(Type.STRING);
        attributeIsAString = true;
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
      if (f.getAnnotation(ScimResourceIdReference.class) != null) {
        if (attributeIsAString) {
          attribute.setScimResourceIdReference(true);
        } else {
          log.warn("Field annotated with @edu.psu.swe.scim.spec.annotation.ScimResourceIdReference must be a string: {}", f);
        }
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
      ScimType st = f.getType().getAnnotation(ScimType.class);
      
      if (attribute.getType() == Type.COMPLEX || st != null) {
        Class<?> componentType;
        if (!attribute.isMultiValued()) {
          componentType = f.getType();
          attribute.setSubAttributes(createAttributes(Arrays.asList(f.getType().getDeclaredFields()), invalidAttributes, nameBase + "." + f.getName()), AddAction.APPEND);
        } else if (f.getType().isArray()) {
          componentType = f.getType().getComponentType();
        } else {
          ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
          componentType = (Class<?>) stringListType.getActualTypeArguments()[0];
        }
        
        List<Field> fl = ScimUtils.getFieldsUpTo(componentType, Object.class);
        List<Attribute> la = createAttributes(fl, invalidAttributes, nameBase + "." + f.getName());
          
        attribute.setSubAttributes(la, AddAction.APPEND);
      }
      attributeList.add(attribute);
    }

    log.debug("Returning " + attributeList.size() + " attributes");
    return attributeList;
  }

  

  // private Provider<ScimGroup> groupProvider = null;
  // private Provider<ScimUser> userProvider = null;
}
