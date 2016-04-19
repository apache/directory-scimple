package edu.psu.swe.scim.server.utility;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
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

  public <T extends ScimResource> T setAttributesForDisplay(T resource) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException {
    String resourceType = resource.getResourceType();
    Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

    // return always and default, exclude never and requested
    removeAttributesOfType(resource, schema, Returned.REQUEST);
    removeAttributesOfType(resource, schema, Returned.NEVER);
    return resource;
  }
  
  public <T extends ScimResource> T setAttributesForDisplay(T resource, String attributes) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException {
    if (StringUtils.isEmpty(attributes)) {
      return setAttributesForDisplay(resource);
    } else {
      String resourceType = resource.getResourceType();
      Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

      // return always and specified attributes, exclude never
      Set<Attribute> attributesToKeep = getAttributes(attributes);
      removeAttributesOfType(resource, schema, Returned.DEFAULT, attributesToKeep);
      removeAttributesOfType(resource, schema, Returned.REQUEST, attributesToKeep);
      removeAttributesOfType(resource, schema, Returned.NEVER, attributesToKeep);
    }

    return resource;
  }

  public <T extends ScimResource> T setExcludedAttributesForDisplay(T resource, String excludedAttributes) throws IllegalArgumentException, IllegalAccessException, AttributeDoesNotExistException {
   
    if (StringUtils.isEmpty(excludedAttributes)) {
      return setAttributesForDisplay(resource);
    } else {
      String resourceType = resource.getResourceType();
      Schema schema = registry.getBaseSchemaOfResourceType(resourceType);

      // return always and default, exclude never and specified attributes
      Set<Attribute> attributesToRemove = getAttributes(excludedAttributes);
      removeAttributesOfType(resource, schema, Returned.REQUEST);
      removeAttributesOfType(resource, schema, Returned.NEVER);
      removeAttributes(resource, schema, attributesToRemove);
    }

    return resource;
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
 
    if (attributeContainer == null) {
      log.error("attribute container == null");
    } else {
      if (attributeContainer.getAttributes() == null) {
        log.error("attribute container returned null for getAttributes");
      }
    }
    
    for (Attribute attribute : attributeContainer.getAttributes()) {
      Field field = attribute.getField();
      if (function.apply(attribute)) {
        field.setAccessible(true);
        if (!field.getType().isPrimitive()) {
          field.set(object, null);
        }
      } else if (!attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
        String name = field.getName();
        field.setAccessible(true);
        Object subObject = field.get(object);
        Attribute subAttribute = attributeContainer.getAttribute(name);
        processAttributes(subObject, subAttribute, function);
      } else if (attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
        String name = field.getName();
        field.setAccessible(true);
        Object subObject = field.get(object);

        if (subObject == null) {
          continue;
        }
        
        if (Collection.class.isAssignableFrom(subObject.getClass())) {
          Collection<?> collection = (Collection<?>) subObject;
          for(Object o : collection) {
            Attribute subAttribute = attributeContainer.getAttribute(name);
            processAttributes(o, subAttribute, function);
          }
        } else if (field.getType().isArray()) {
          Object [] array = (Object []) subObject;
          
          for(Object o : array) {
            Attribute subAttribute = attributeContainer.getAttribute(name);
            processAttributes(o, subAttribute, function);
          }
        }
        
      }
    }
  }

  private Set<Attribute> getAttributes(String s) throws AttributeDoesNotExistException {
    Set<Attribute> attributes = new HashSet<>();

    String[] split = StringUtils.split(s, ",");

    for (String af : split) {
      AttributeReference attributeReference = new AttributeReference(af);
      attributes.add(findAttribute(attributeReference));
    }

    return attributes;
  }

  private Attribute findAttribute(AttributeReference attributeReference) throws AttributeDoesNotExistException {
    String schemaUrn = attributeReference.getUrn();
    String[] attributeNames = attributeReference.getAttributeName();
    Schema schema = null;

    if (!StringUtils.isEmpty(schemaUrn)) {
      schema = registry.getSchema(schemaUrn);

      Attribute attribute = findAttributeInSchema(schema, attributeNames);
      if (attribute == null) {
        throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
      }
      return attribute;
    }

    // Handle unqualified attributes, look in the core schemas
    schema = registry.getSchema(ScimUser.SCHEMA_URI);
    Attribute attribute = findAttributeInSchema(schema, attributeNames);
    if (attribute != null) {
      return attribute;
    }

    schema = registry.getSchema(ScimGroup.SCHEMA_URI);
    attribute = findAttributeInSchema(schema, attributeNames);
    if (attribute != null) {
      return attribute;
    }

    throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
  }

  private Attribute findAttributeInSchema(Schema schema, String[] attributeNames) throws AttributeDoesNotExistException {
    AttributeContainer attributeContainer = schema;

    for (String attributeName : attributeNames) {
      attributeContainer = attributeContainer.getAttribute(attributeName);
      if (attributeContainer == null) {
        break;
      }
    }
    return (Attribute) attributeContainer;
  }

}
