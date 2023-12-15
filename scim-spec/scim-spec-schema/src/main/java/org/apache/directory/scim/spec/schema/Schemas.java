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

package org.apache.directory.scim.spec.schema;

import jakarta.xml.bind.annotation.XmlEnumValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceIdReference;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.annotation.ScimType;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
public final class Schemas {
  private final static Map<Class<?>, Schema.Attribute.Type> CLASS_TO_TYPE = new HashMap<>() {{
    put(String.class, Schema.Attribute.Type.STRING);
    put(Character.class, Schema.Attribute.Type.STRING);
    put(Integer.class, Schema.Attribute.Type.INTEGER);
    put(int.class, Schema.Attribute.Type.INTEGER);
    put(Double.class, Schema.Attribute.Type.DECIMAL);
    put(double.class, Schema.Attribute.Type.DECIMAL);
    put(Float.class, Schema.Attribute.Type.DECIMAL);
    put(float.class, Schema.Attribute.Type.DECIMAL);
    put(Boolean.class, Schema.Attribute.Type.BOOLEAN);
    put(boolean.class, Schema.Attribute.Type.BOOLEAN);
    put(LocalTime.class, Schema.Attribute.Type.DATE_TIME);
    put(LocalDate.class, Schema.Attribute.Type.DATE_TIME);
    put(LocalDateTime.class, Schema.Attribute.Type.DATE_TIME);
    put(Date.class, Schema.Attribute.Type.DATE_TIME);
    put(Instant.class, Schema.Attribute.Type.DATE_TIME);
    put(byte[].class, Schema.Attribute.Type.BINARY);
  }};

  private Schemas() {}

  public static Schema schemaFor(Class<? extends ScimResource> clazz) throws ScimResourceInvalidException {
    return generateSchema(clazz, getFieldsUpTo(clazz, BaseResource.class));
  }

  public static Schema schemaForExtension(Class<? extends ScimExtension> clazz) throws ScimResourceInvalidException {
    return generateSchema(clazz, getFieldsUpTo(clazz, Object.class));
  }

  private static Schema generateSchema(Class<?> clazz, List<Field> fieldList) throws ScimResourceInvalidException {

    Schema schema = new Schema();

    ScimResourceType srt = clazz.getAnnotation(ScimResourceType.class);
    ScimExtensionType set = clazz.getAnnotation(ScimExtensionType.class);

    if (srt == null && set == null) {
      // TODO - throw?
      log.error("Neither a ScimResourceType or ScimExtensionType annotation found");
    }

    log.debug("calling set attributes with " + fieldList.size() + " fields");
    String urn = set != null ? set.id() : srt.schema();
    Set<String> invalidAttributes = new HashSet<>();
    List<Schema.Attribute> createAttributes = createAttributes(urn, fieldList, invalidAttributes, clazz.getSimpleName());
    schema.setAttributes(createAttributes);

    if (!invalidAttributes.isEmpty()) {
      StringBuilder sb = new StringBuilder();

      sb.append("Scim attributes cannot be primitive types unless they are required.  The following values were found that are primitive and not required\n\n");

      for (String s : invalidAttributes) {
        sb.append(s);
        sb.append("\n");
      }

      throw new ScimResourceInvalidException(sb.toString());
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


  private static List<Schema.Attribute> createAttributes(String urn, List<Field> fieldList, Set<String> invalidAttributes, String nameBase) throws ScimResourceInvalidException {
    List<Schema.Attribute> attributeList = new ArrayList<>();

    for (Field f : fieldList) {
      ScimAttribute sa = f.getAnnotation(ScimAttribute.class);

      log.debug("++++++++++++++++++++ Processing field " + f.getName());
      if (sa == null) {
        log.debug("Attribute {} did not have a ScimAttribute annotation", f.getName());
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
      Schema.Attribute attribute = new Schema.Attribute();
      attribute.setAccessor(Schema.AttributeAccessor.forField(f));
      attribute.setName(attributeName);
      attribute.setUrn(urn);

      List<String> canonicalTypes = null;
      Field [] enumFields = sa.canonicalValueEnum().getFields();
      log.debug("Gathered fields of off the enum, there are {} {}", enumFields.length, sa.canonicalValueEnum().getName());

      if (enumFields.length != 0) {

        //This looks goofy, but there's always at least the default value, so it's not an empty list
        if (sa.canonicalValueList().length != 1 && !sa.canonicalValueList()[0].isEmpty()) {
          throw new ScimResourceInvalidException("You cannot set both the canonicalEnumValue and canonicalValueList attributes on the same ScimAttribute");
        }

        canonicalTypes = new ArrayList<>();

        for (Field field : enumFields) {
          XmlEnumValue[] annotation = field.getAnnotationsByType(XmlEnumValue.class);

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
        attribute.setCanonicalValues(new HashSet<>(canonicalTypes));
      }

      attribute.setCaseExact(sa.caseExact());
      attribute.setDescription(sa.description());

      Class<?> typeClass;
      if (Collection.class.isAssignableFrom(f.getType())) {
        log.debug("Attribute: '{}' is a collection", attributeName);
        ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
        typeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        attribute.setMultiValued(true);
      } else if (f.getType().isArray()) {
        log.debug("Attribute: '{}' is an array", attributeName);
        typeClass = f.getType().getComponentType();

        // special case for byte[]
        if (typeClass == byte.class) {
          typeClass = byte[].class;
        } else {
          attribute.setMultiValued(true);
        }
      } else {
        typeClass = f.getType();
        attribute.setMultiValued(false);
      }

      log.debug("Attempting to set the attribute type, raw value = {}", typeClass);
      Schema.Attribute.Type type = CLASS_TO_TYPE.getOrDefault(typeClass, Schema.Attribute.Type.COMPLEX);
      attribute.setType(type);

      if (f.getAnnotation(ScimResourceIdReference.class) != null) {
        if (type == Schema.Attribute.Type.STRING) {
          attribute.setScimResourceIdReference(true);
        } else {
          log.warn("Field annotated with @ScimResourceIdReference must be a string: {}", f);
        }
      }
      attribute.setMutability(sa.mutability());

      List<String> refType = Arrays.asList(sa.referenceTypes());

      // If we just have the default single empty string, set to null
      if (refType.isEmpty() || (refType.size() == 1 && refType.get(0).isEmpty())) {
        attribute.setReferenceTypes(null);
      } else {
        attribute.setType(Schema.Attribute.Type.REFERENCE);
        attribute.setReferenceTypes(Arrays.asList(sa.referenceTypes()));
      }

      attribute.setRequired(sa.required());
      attribute.setReturned(sa.returned());
      attribute.setUniqueness(sa.uniqueness());

      ScimType st = f.getType().getAnnotation(ScimType.class);

      if (attribute.getType() == Schema.Attribute.Type.COMPLEX || st != null) {
        Class<?> componentType;
        if (!attribute.isMultiValued()) {
          componentType = f.getType();
          attribute.setSubAttributes(createAttributes(urn, Arrays.asList(f.getType().getDeclaredFields()), invalidAttributes, nameBase + "." + f.getName()), Schema.Attribute.AddAction.APPEND);
        } else if (f.getType().isArray()) {
          componentType = f.getType().getComponentType();
        } else {
          ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
          componentType = (Class<?>) stringListType.getActualTypeArguments()[0];
        }

        List<Field> fl = getFieldsUpTo(componentType, Object.class);
        List<Schema.Attribute> la = createAttributes(urn, fl, invalidAttributes, nameBase + "." + f.getName());

        attribute.setSubAttributes(la, Schema.Attribute.AddAction.APPEND);
      }
      attributeList.add(attribute);
    }

    attributeList.sort(Comparator.comparing(o -> o.name));
    log.debug("Returning {} attributes", attributeList.size());
    return attributeList;
  }

  static List<Field> getFieldsUpTo(Class<?> startClass, Class<?> exclusiveParent) {
    List<Field> currentClassFields = new ArrayList<>();
    Collections.addAll(currentClassFields, startClass.getDeclaredFields());
    Class<?> parentClass = startClass.getSuperclass();
    if (parentClass != null && (!(parentClass.equals(exclusiveParent)))) {
      List<Field> parentClassFields = getFieldsUpTo(parentClass, exclusiveParent);
      currentClassFields.addAll(parentClassFields);
    }
    return currentClassFields;
  }
}
