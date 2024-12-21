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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import lombok.*;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.validator.Urn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Defines the structure of the SCIM schemas as defined by section 7 of the SCIM
 * schema specification. See
 * <a href="https://datatracker.ietf.org/doc/html/rfc7643#section-7">RFC 7643 section 7</a> for more
 * details.
 *
 * @author Steve Moyer {@literal <smoyer@psu.edu>}
 */
@XmlRootElement(name = "schema")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class Schema implements AttributeContainer {

  private static final Logger LOG = LoggerFactory.getLogger(Schema.class);
  
  public static final String RESOURCE_NAME = "Schema";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Schema";
  private static final long serialVersionUID = 1869782412244161741L;


  /**
   * Defines the structure of attributes included in SCIM schemas as defined by
   * section 7 of the SCIM schema specification. See
   * <a href="https://datatracker.ietf.org/doc/html/rfc7643#section-7">RFC 7643 section 7</a> for more
   * details.
   *
   * @author Steve Moyer {@literal <smoyer@psu.edu>}
   */
  @XmlType(name = "attribute")
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class Attribute implements AttributeContainer {

    private static final long serialVersionUID = 1683400114899587851L;

    public enum Mutability {

      @XmlEnumValue("immutable") IMMUTABLE,
      @XmlEnumValue("readOnly") READ_ONLY,
      @XmlEnumValue("readWrite") READ_WRITE,
      @XmlEnumValue("writeOnly") WRITE_ONLY;

    }

    public enum Returned {
      @XmlEnumValue("always") ALWAYS,
      @XmlEnumValue("default") DEFAULT,
      @XmlEnumValue("never") NEVER,
      @XmlEnumValue("request") REQUEST;
    }

    @XmlEnum(String.class)
    public enum Type {
      @XmlEnumValue("binary") BINARY,
      @XmlEnumValue("boolean") BOOLEAN,
      @XmlEnumValue("complex") COMPLEX,
      @XmlEnumValue("dateTime") DATE_TIME,
      @XmlEnumValue("decimal") DECIMAL,
      @XmlEnumValue("integer") INTEGER,
      @XmlEnumValue("reference") REFERENCE,
      @XmlEnumValue("string") STRING;
    }

    public enum Uniqueness {
      @XmlEnumValue("global") GLOBAL,
      @XmlEnumValue("none") NONE,
      @XmlEnumValue("server") SERVER;
    }
    
    public enum AddAction {
      REPLACE,
      APPEND
    }

    // The attribute name must match the ABNF pattern defined in section 2.1 of
    // the SCIM Schema specification.
    @XmlElement
    @Pattern(regexp = "\\p{Alpha}(-|_|\\p{Alnum})*")
    String name;
    
    @XmlElement
    Type type;

    String schemaUrn;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    String path;

    @XmlElement
    Set<Attribute> subAttributes;
    
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    Map<String, Attribute> subAttributeNamesMap = new HashMap<>();
    
    @XmlElement
    boolean multiValued;
    
    @XmlElement
    String description;
    
    @XmlElement
    boolean required;
    
    @XmlElement
    Set<String> canonicalValues;
    
    @XmlElement
    boolean caseExact;
    
    @XmlElement
    Mutability mutability;
    
    @XmlElement
    Returned returned;
    
    @XmlElement
    Uniqueness uniqueness;
    
    @XmlElement
    List<String> referenceTypes;

    transient AttributeAccessor accessor;

    private boolean scimResourceIdReference;

    public String getUrn() {
      return schemaUrn + ":" + path;
    }

    @Override
    public Set<Attribute> getAttributes() {
      return Collections.unmodifiableSet(subAttributes);
    }
    
    public void setSubAttributes(Set<Attribute> attributes, AddAction action) {
      
      if (action.equals(AddAction.REPLACE)) {
        subAttributeNamesMap.clear();
      }
      
      for (Attribute attribute : attributes) {
        String name = attribute.getName();
        if (name == null) {
          LOG.warn("Attribute name was null, skipping name indexing");
          continue;
        }
        subAttributeNamesMap.put(name.toLowerCase(Locale.ENGLISH), attribute);
      }
      
      if(action.equals(AddAction.REPLACE)) {
        this.subAttributes = attributes;
      } else {
        if (subAttributes == null) {
          subAttributes = new TreeSet<>(Comparator.comparing(o -> o.name));
        }
        this.subAttributes.addAll(attributes);
      }
    }
    
    public Attribute getAttribute(String name) {
      if (name == null) {
        return null;
      }
      return subAttributeNamesMap.get(name.toLowerCase(Locale.ENGLISH));
    }

  }
  
  @Urn
  @NotNull
  @Size(min = 1, max = 65535)
  @XmlElement
  String id;

  @XmlElement
  String name;

  @XmlElement
  String description;

  @Size(min = 1, max = 65535)
  @XmlElement
  @XmlElementWrapper(name = "attributes")
  Set<Attribute> attributes;
  
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  Map<String, Attribute> attributeNamesMap = new HashMap<>();
  
  @XmlElement
  Meta meta;

  @Override
  public String getUrn() {
    return id;
  }

  public Set<Attribute> getAttributes() {
    return Collections.unmodifiableSet(attributes);
  }
  
  public void setAttributes(Set<Attribute> attributes) {
    attributeNamesMap.clear();
    
    for (Attribute attribute : attributes) {
      String name = attribute.getName();
      if (name == null) {
        LOG.warn("Attribute name was null, skipping name indexing");
        continue;
      }
      attributeNamesMap.put(name.toLowerCase(Locale.ENGLISH), attribute);
    }
    
    this.attributes = attributes;
  }
  
  public Attribute getAttribute(String name) {
    if (name == null) {
      return null;
    }
    return attributeNamesMap.get(name.toLowerCase(Locale.ENGLISH));
  }

  public Attribute getAttributeFromPath(String path) {
    if (path == null) {
      return null;
    }

    String[] parts = path.split("\\.");
    Attribute attribute = getAttribute(parts[0]);
    for (int index = 1; index < parts.length; index++) {
      attribute = attribute.getAttribute(parts[index]);
    }

    return attribute;
  }

  public interface AttributeAccessor {
    <T> T get(Object resource);

    void set(Object resource, Object value);

    Class<?> getType();

    static AttributeAccessor forField(Field field) {
      return new FieldAttributeAccessor(field);
    }

    boolean isAccessible(Object resource);
  }

  @EqualsAndHashCode
  private static class FieldAttributeAccessor implements AttributeAccessor {

    private final Field field;

    public FieldAttributeAccessor(Field field) {
      this.field = field;
    }

    @Override
    public <T> T get(Object resource) {
      try {
        field.setAccessible(true);
        return (T) field.get(resource);
      } catch (IllegalAccessException e) {
        throw new ScimResourceInvalidException("Schema definition is invalid", e);
      }
    }

    @Override
    public void set(Object resource, Object value) {
      try {
        field.setAccessible(true);
        field.set(resource, value);
      } catch (IllegalAccessException e) {
        throw new ScimResourceInvalidException("Schema definition is invalid", e);
      }
    }

    @Override
    public Class<?> getType() {
      return field.getType();
    }

    @Override
    public boolean isAccessible(Object resource)
    {
      try {
        return field.canAccess(resource);
      }
      catch (IllegalArgumentException e) {
        return false;
      }
    }
  }
}
