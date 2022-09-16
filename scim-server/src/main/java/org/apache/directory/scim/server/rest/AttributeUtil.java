/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.server.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.server.exception.AttributeDoesNotExistException;
import org.apache.directory.scim.server.exception.AttributeException;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Type;
import org.apache.directory.scim.core.schema.SchemaRegistry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

@Slf4j
class AttributeUtil {

  SchemaRegistry schemaRegistry;

  AttributeUtil(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public <T extends ScimResource> T keepAlwaysAttributesForDisplay(T resource) throws AttributeException {
    return setAttributesForDisplayInternal(resource, Returned.DEFAULT, Returned.REQUEST, Returned.NEVER);
  }
  
  public <T extends ScimResource> T setAttributesForDisplay(T resource) throws AttributeException {
    return setAttributesForDisplayInternal(resource, Returned.REQUEST, Returned.NEVER);
  }
  
  private <T extends ScimResource> T setAttributesForDisplayInternal(T resource, Returned ... removeAttributesOfTypes) throws AttributeException {
    T copy = cloneScimResource(resource);
    String resourceType = copy.getResourceType();
    Schema schema = schemaRegistry.getBaseSchemaOfResourceType(resourceType);

    // return always and default, exclude never and requested
    for (Returned removeAttributesOfType : removeAttributesOfTypes) {
      removeAttributesOfType(copy, schema, removeAttributesOfType);
    }

    for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
      String extensionUrn = extensionEntry.getKey();
      ScimExtension scimExtension = extensionEntry.getValue();

      Schema extensionSchema = schemaRegistry.getSchema(extensionUrn);

      for (Returned removeAttributesOfType : removeAttributesOfTypes) {
        removeAttributesOfType(scimExtension, extensionSchema, removeAttributesOfType);
      }
    }
    return copy;
  }

  public <T extends ScimResource> T setAttributesForDisplay(T resource, Set<AttributeReference> attributes) throws AttributeException {
    if (attributes.isEmpty()) {
      return setAttributesForDisplay(resource);
    } else {
      T copy = cloneScimResource(resource);
      
      String resourceType = copy.getResourceType();
      Schema schema = schemaRegistry.getBaseSchemaOfResourceType(resourceType);

      // return always and specified attributes, exclude never
      Set<Attribute> attributesToKeep = resolveAttributeReferences(attributes, true);
      Set<String> extensionsToRemove = new HashSet<>();
      removeAttributesOfType(copy, schema, Returned.DEFAULT, attributesToKeep);
      removeAttributesOfType(copy, schema, Returned.REQUEST, attributesToKeep);
      removeAttributesOfType(copy, schema, Returned.NEVER);

      for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
        String extensionUrn = extensionEntry.getKey();
        ScimExtension scimExtension = extensionEntry.getValue();
        boolean removeExtension = true;

        for (Attribute attributeToKeep : attributesToKeep) {
          if (extensionUrn.equalsIgnoreCase(attributeToKeep.getUrn())) {
            removeExtension = false;

            break;
          }
        }
        if (removeExtension) {
          extensionsToRemove.add(extensionUrn);

          continue;
        }
        Schema extensionSchema = schemaRegistry.getSchema(extensionUrn);

        removeAttributesOfType(scimExtension, extensionSchema, Returned.DEFAULT, attributesToKeep);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.REQUEST, attributesToKeep);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.NEVER);
      }
      for (String extensionUrn : extensionsToRemove) {
        copy.removeExtension(extensionUrn);
      }
      return copy;
    }
  }

  public <T extends ScimResource> T setExcludedAttributesForDisplay(T resource, Set<AttributeReference> excludedAttributes) throws AttributeException {

    if (excludedAttributes.isEmpty()) {
      return setAttributesForDisplay(resource);
    } else {
      T copy = cloneScimResource(resource);

      String resourceType = copy.getResourceType();
      Schema schema = schemaRegistry.getBaseSchemaOfResourceType(resourceType);

      // return always and default, exclude never and specified attributes
      Set<Attribute> attributesToRemove = resolveAttributeReferences(excludedAttributes, false);
      removeAttributesOfType(copy, schema, Returned.REQUEST);
      removeAttributesOfType(copy, schema, Returned.NEVER);
      removeAttributes(copy, schema, attributesToRemove);

      for (Entry<String, ScimExtension> extensionEntry : copy.getExtensions().entrySet()) {
        String extensionUrn = extensionEntry.getKey();
        ScimExtension scimExtension = extensionEntry.getValue();

        Schema extensionSchema = schemaRegistry.getSchema(extensionUrn);

        removeAttributesOfType(scimExtension, extensionSchema, Returned.REQUEST);
        removeAttributesOfType(scimExtension, extensionSchema, Returned.NEVER);
        removeAttributes(scimExtension, extensionSchema, attributesToRemove);
      }
      return copy;
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T cloneScimResource(T original) throws AttributeException {
    try {
    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(boas);
    oos.writeObject(original);

    ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    return (T) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new AttributeException(e);
    }
  }

  private void removeAttributesOfType(Object object, AttributeContainer attributeContainer, Returned returned) throws AttributeException {
    Function<Attribute, Boolean> function = (attribute) -> returned == attribute.getReturned();
    processAttributes(object, attributeContainer, function);
  }

  private void removeAttributesOfType(Object object, AttributeContainer attributeContainer, Returned returned, Set<Attribute> attributesToKeep) throws AttributeException {
    Function<Attribute, Boolean> function = (attribute) -> !attributesToKeep.contains(attribute) && returned == attribute.getReturned();
    processAttributes(object, attributeContainer, function);
  }

  private void removeAttributes(Object object, AttributeContainer attributeContainer, Set<Attribute> attributesToRemove) throws AttributeException {
    Function<Attribute, Boolean> function = (attribute) -> attributesToRemove.contains(attribute);
    processAttributes(object, attributeContainer, function);
  }

  private void processAttributes(Object object, AttributeContainer attributeContainer, Function<Attribute, Boolean> function) throws AttributeException {
    try {
      if (attributeContainer != null && object != null) {
        for (Attribute attribute : attributeContainer.getAttributes()) {

          Schema.AttributeAccessor accessor = attribute.getAccessor();

          if (function.apply(attribute)) {
            if (!accessor.getType().isPrimitive()) {
              Object obj = accessor.get(object);
              if (obj == null) {
                continue;
              }

              log.info("field to be set to null = " + accessor.getType().getName());
              accessor.set(object, null);
            }
          } else if (!attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
            log.debug("### Processing single value complex field " + attribute.getName());
            Object subObject = accessor.get(object);

            if (subObject == null) {
              continue;
            }

            Attribute subAttribute = attributeContainer.getAttribute(attribute.getName());
            log.debug("### container type = " + attributeContainer.getClass().getName());
            if (subAttribute == null) {
              log.debug("#### subattribute == null");
            }
            processAttributes(subObject, subAttribute, function);
          } else if (attribute.isMultiValued() && attribute.getType() == Type.COMPLEX) {
            log.debug("### Processing multi-valued complex field " + attribute.getName());
            Object subObject = accessor.get(object);

            if (subObject == null) {
              continue;
            }

            if (Collection.class.isAssignableFrom(subObject.getClass())) {
              Collection<?> collection = (Collection<?>) subObject;
              for (Object o : collection) {
                Attribute subAttribute = attributeContainer.getAttribute(attribute.getName());
                processAttributes(o, subAttribute, function);
              }
            } else if (accessor.getType().isArray()) {
              Object[] array = (Object[]) subObject;

              for (Object o : array) {
                Attribute subAttribute = attributeContainer.getAttribute(attribute.getName());
                processAttributes(o, subAttribute, function);
              }
            }
          }
        }
      }
    } catch (IllegalArgumentException e) {
      throw new AttributeException(e);
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
      if (!findAttributes.isEmpty()) {
        attributes.addAll(findAttributes);
      }
    }

    return attributes;
  }

  private Set<Attribute> findAttribute(AttributeReference attributeReference, boolean includeAttributeChain) throws AttributeDoesNotExistException {
    String schemaUrn = attributeReference.getUrn();
    Schema schema = null;
    Set<Attribute> attributes;
    
    if (!StringUtils.isEmpty(schemaUrn)) {
      schema = schemaRegistry.getSchema(schemaUrn);

      attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
      if (attributes.isEmpty()) {
        log.error("Attribute " + attributeReference.getFullyQualifiedAttributeName() + "not found in schema " + schemaUrn);
        throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
      }
      return attributes;
    }

    // Handle unqualified attributes, look in the core schemas
    schema = schemaRegistry.getSchema(ScimUser.SCHEMA_URI);
    attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
    if (!attributes.isEmpty()) {
      return attributes;
    }

    schema = schemaRegistry.getSchema(ScimGroup.SCHEMA_URI);
    attributes = findAttributeInSchema(schema, attributeReference, includeAttributeChain);
    if (!attributes.isEmpty()) {
      return attributes;
    }

    log.error("Attribute " + attributeReference.getFullyQualifiedAttributeName() + "not found in any schema.");
    throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
  }

  private Set<Attribute> findAttributeInSchema(Schema schema, AttributeReference attributeReference, boolean includeAttributeChain) {
    if (schema == null) {
      return Collections.emptySet();
    }
    Set<Attribute> attributes = new HashSet<>();
    String attributeName = attributeReference.getAttributeName();
    String subAttributeName = attributeReference.getSubAttributeName();
    Attribute attribute = schema.getAttribute(attributeName);

    if (attribute == null) {
      return Collections.emptySet();
    }
    if (includeAttributeChain || subAttributeName == null) {
      attributes.add(attribute);
    }
    if (subAttributeName != null) {
      attribute = attribute.getAttribute(subAttributeName);

      if (attribute == null) {
        return Collections.emptySet();
      }
      attributes.add(attribute);
    }
    if (attribute.getType() == Type.COMPLEX && includeAttributeChain) {
      List<Attribute> remaininAttributes = attribute.getAttributes();
      attributes.addAll(remaininAttributes);
    }
    return attributes;
  }

}
