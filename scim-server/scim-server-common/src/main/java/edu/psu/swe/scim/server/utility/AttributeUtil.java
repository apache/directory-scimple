package edu.psu.swe.scim.server.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.rest.ScimResourceDeserializer;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.AttributeContainer;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import lombok.extern.slf4j.Slf4j;

@Stateless
@Slf4j
public class AttributeUtil {

  @Inject
  Registry registry;

  ObjectMapper objectMapper;

  @PostConstruct
  public void init() { // TODO move this to a CDI producer
    objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
    AnnotationIntrospector pair = new AnnotationIntrospectorPair(jacksonIntrospector, jaxbIntrospector);
    objectMapper.setAnnotationIntrospector(pair);

    objectMapper.setSerializationInclusion(Include.NON_NULL);

    SimpleModule module = new SimpleModule();
    module.addDeserializer(ScimResource.class, new ScimResourceDeserializer(this.registry, this.objectMapper));
    objectMapper.registerModule(module);
  }

  public <T extends ScimResource> T keepAlwaysAttributesForDisplay(T resource) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException, IOException {
    return setAttributesForDisplayInternal(resource, Returned.DEFAULT, Returned.REQUEST, Returned.NEVER);
  }
  
  public <T extends ScimResource> T setAttributesForDisplay(T resource) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException, IOException {
    return setAttributesForDisplayInternal(resource, Returned.REQUEST, Returned.NEVER);
  }
  
  private <T extends ScimResource> T setAttributesForDisplayInternal(T resource, Returned ... removeAttributesOfTypes) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException, IOException {

    T copy = cloneScimResource(resource);
    String resourceType = copy.getResourceType();
    Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

    // return always and default, exclude never and requested
    for (Returned removeAttributesOfType : removeAttributesOfTypes) {
      removeAttributesOfType(copy, schema, removeAttributesOfType);
    }

    for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
      String extensionUrn = extensionEntry.getKey();
      ScimExtension scimExtension = extensionEntry.getValue();

      Schema extensionSchema = registry.getSchema(extensionUrn);

      for (Returned removeAttributesOfType : removeAttributesOfTypes) {
        removeAttributesOfType(scimExtension, extensionSchema, removeAttributesOfType);
      }
    }
    return copy;
  }

  public <T extends ScimResource> T setAttributesForDisplay(T resource, Set<AttributeReference> attributes) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException, IOException {
    if (attributes.isEmpty()) {
      return setAttributesForDisplay(resource);
    } else {
      T copy = cloneScimResource(resource);

      String resourceType = copy.getResourceType();
      Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

      // return always and specified attributes, exclude never
      Set<Attribute> attributesToKeep = resolveAttributeReferences(attributes, true);
      removeAttributesOfType(copy, schema, Returned.DEFAULT, attributesToKeep);
      removeAttributesOfType(copy, schema, Returned.REQUEST, attributesToKeep);
      removeAttributesOfType(copy, schema, Returned.NEVER);

      for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
        String extensionUrn = extensionEntry.getKey();
        ScimExtension scimExtension = extensionEntry.getValue();

        Schema extensionSchema = registry.getSchema(extensionUrn);

        removeAttributesOfType(scimExtension, extensionSchema, Returned.DEFAULT, attributesToKeep);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.REQUEST, attributesToKeep);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.NEVER);
      }
      return copy;
    }
  }

  public <T extends ScimResource> T setExcludedAttributesForDisplay(T resource, Set<AttributeReference> excludedAttributes) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException, IOException {

    if (excludedAttributes.isEmpty()) {
      return setAttributesForDisplay(resource);
    } else {
      T copy = cloneScimResource(resource);

      String resourceType = copy.getResourceType();
      Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

      // return always and default, exclude never and specified attributes
      Set<Attribute> attributesToRemove = resolveAttributeReferences(excludedAttributes, false);
      removeAttributesOfType(copy, schema, Returned.REQUEST);
      removeAttributesOfType(copy, schema, Returned.NEVER);
      removeAttributes(copy, schema, attributesToRemove);

      for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
        String extensionUrn = extensionEntry.getKey();
        ScimExtension scimExtension = extensionEntry.getValue();

        Schema extensionSchema = registry.getSchema(extensionUrn);

        removeAttributesOfType(scimExtension, extensionSchema, Returned.REQUEST);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.NEVER);
        removeAttributes(scimExtension, extensionSchema, attributesToRemove);
      }
      return copy;
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T cloneScimResource(T original) throws IOException {
    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(boas);
    oos.writeObject(original);

    ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    T copy = null;
    try {
      copy = (T) ois.readObject();
    } catch (ClassNotFoundException e) {
      // Should never happen
      log.error("", e);
    }
    return copy;
  }

  private void removeAttributesOfType(Object object, AttributeContainer attributeContainer, Returned returned) throws IllegalArgumentException, IllegalAccessException {
    Function<Attribute, Boolean> function = (attribute) -> returned == attribute.getReturned();
    processAttributes(object, attributeContainer, function);
  }

  private void removeAttributesOfType(Object object, AttributeContainer attributeContainer, Returned returned, Set<Attribute> attributesToKeep) throws IllegalArgumentException, IllegalAccessException {
    Function<Attribute, Boolean> function = (attribute) -> !attributesToKeep.contains(attribute) && returned == attribute.getReturned();
    processAttributes(object, attributeContainer, function);
  }

  private void removeAttributes(Object object, AttributeContainer attributeContainer, Set<Attribute> attributesToRemove) throws IllegalArgumentException, IllegalAccessException {
    Function<Attribute, Boolean> function = (attribute) -> attributesToRemove.contains(attribute);
    processAttributes(object, attributeContainer, function);
  }

  private void processAttributes(Object object, AttributeContainer attributeContainer, Function<Attribute, Boolean> function) throws IllegalArgumentException, IllegalAccessException {

    if (attributeContainer != null && object != null) {
      for (Attribute attribute : attributeContainer.getAttributes()) {
        Field field = attribute.getField();
        if (function.apply(attribute)) {
          field.setAccessible(true);
          if (!field.getType().isPrimitive()) {
            Object obj = field.get(object);
            if (obj == null) {
              continue;
            }
            
            log.info("field to be set to null = " + field.getType().getName());
            field.set(object, null);
          }
        } else if (!attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
          String name = field.getName();
          log.debug("### Processing single value complex field " + name);
          field.setAccessible(true);
          Object subObject = field.get(object);

          if (subObject == null) {
            continue;
          }
          
          Attribute subAttribute = attributeContainer.getAttribute(name);
          log.debug("### container type = " + attributeContainer.getClass().getName());
          if (subAttribute == null) {
            log.debug("#### subattribute == null");
          }
          processAttributes(subObject, subAttribute, function);
        } else if (attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
          String name = field.getName();
          log.debug("### Processing multi-valued complex field " + name);
          field.setAccessible(true);
          Object subObject = field.get(object);

          if (subObject == null) {
            continue;
          }

          if (Collection.class.isAssignableFrom(subObject.getClass())) {
            Collection<?> collection = (Collection<?>) subObject;
            for (Object o : collection) {
              Attribute subAttribute = attributeContainer.getAttribute(name);
              processAttributes(o, subAttribute, function);
            }
          } else if (field.getType().isArray()) {
            Object[] array = (Object[]) subObject;

            for (Object o : array) {
              Attribute subAttribute = attributeContainer.getAttribute(name);
              processAttributes(o, subAttribute, function);
            }
          }
        }
      }
    }
  }

  public Set<AttributeReference> getAttributeReferences(String s) {
    Set<AttributeReference> attributeReferences = new HashSet<>();

    String[] split = StringUtils.split(s, ",");

    for (String af : split) {
      AttributeReference attributeReference = new AttributeReference(af);
      attributeReferences.add(attributeReference);
    }

    return attributeReferences;
  }

  private Set<Attribute> resolveAttributeReferences(Set<AttributeReference> attributeReferences, boolean includeAttributeChain) throws AttributeDoesNotExistException {
    Set<Attribute> attributes = new HashSet<>();

    for (AttributeReference attributeReference : attributeReferences) {
      Set<Attribute> findAttributes = findAttribute(attributeReference, includeAttributeChain);
      attributes.addAll(findAttributes);
    }

    return attributes;
  }

  private Set<Attribute> findAttribute(AttributeReference attributeReference, boolean includeAttributeChain) throws AttributeDoesNotExistException {
    String schemaUrn = attributeReference.getUrn();
    Schema schema = null;

    if (!StringUtils.isEmpty(schemaUrn)) {
      schema = registry.getSchema(schemaUrn);

      Set<Attribute> attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
      if (attributes == null) {
        throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
      }
      return attributes;
    }

    // Handle unqualified attributes, look in the core schemas
    schema = registry.getSchema(ScimUser.SCHEMA_URI);
    Set<Attribute> attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
    if (attributes != null) {
      return attributes;
    }

    schema = registry.getSchema(ScimGroup.SCHEMA_URI);
    attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
    if (attributes != null) {
      return attributes;
    }

    throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
  }

  private Set<Attribute> findAttributeInSchema(Schema schema, AttributeReference attributeReference, boolean includeAttributeChain) {
    AttributeContainer attributeContainer = schema;
    String[] attributeNames = attributeReference.getAttributeName();

    Set<Attribute> attributes = new HashSet<>();

    for (String attributeName : attributeNames) {
      attributeContainer = attributeContainer.getAttribute(attributeName);

      if (attributeContainer == null) {
        return null;
      }

      if (includeAttributeChain) {
        attributes.add((Attribute) attributeContainer);
      }
    }

    attributes.add((Attribute) attributeContainer);

    return attributes;
  }

}
