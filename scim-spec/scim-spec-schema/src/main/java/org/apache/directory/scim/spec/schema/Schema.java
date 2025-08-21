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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
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
public class Schema implements AttributeContainer {

  private static final Logger LOG = LoggerFactory.getLogger(Schema.class);
  
  public static final String RESOURCE_NAME = "Schema";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Schema";
  private static final long serialVersionUID = 1869782412244161741L;

  public @Urn @NotNull @Size(min = 1, max = 65535) String getId() {
    return this.id;
  }

  public Schema setId(@Urn @NotNull @Size(min = 1, max = 65535) String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return this.name;
  }

  public Schema setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return this.description;
  }

  public Schema setDescription(String description) {
    this.description = description;
    return this;
  }

  public Map<String, Attribute> getAttributeNamesMap() {
    return this.attributeNamesMap;
  }

  public Schema setAttributeNamesMap(Map<String, Attribute> attributeNamesMap) {
    this.attributeNamesMap = attributeNamesMap;
    return this;
  }

  public Meta getMeta() {
    return this.meta;
  }

  public Schema setMeta(Meta meta) {
    this.meta = meta;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Schema)) return false;
    final Schema other = (Schema) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
    final Object this$description = this.getDescription();
    final Object other$description = other.getDescription();
    if (this$description == null ? other$description != null : !this$description.equals(other$description))
      return false;
    final Object this$attributes = this.getAttributes();
    final Object other$attributes = other.getAttributes();
    if (this$attributes == null ? other$attributes != null : !this$attributes.equals(other$attributes)) return false;
    final Object this$attributeNamesMap = this.getAttributeNamesMap();
    final Object other$attributeNamesMap = other.getAttributeNamesMap();
    if (this$attributeNamesMap == null ? other$attributeNamesMap != null : !this$attributeNamesMap.equals(other$attributeNamesMap))
      return false;
    final Object this$meta = this.getMeta();
    final Object other$meta = other.getMeta();
    if (this$meta == null ? other$meta != null : !this$meta.equals(other$meta)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Schema;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $description = this.getDescription();
    result = result * PRIME + ($description == null ? 43 : $description.hashCode());
    final Object $attributes = this.getAttributes();
    result = result * PRIME + ($attributes == null ? 43 : $attributes.hashCode());
    final Object $attributeNamesMap = this.getAttributeNamesMap();
    result = result * PRIME + ($attributeNamesMap == null ? 43 : $attributeNamesMap.hashCode());
    final Object $meta = this.getMeta();
    result = result * PRIME + ($meta == null ? 43 : $meta.hashCode());
    return result;
  }

  public String toString() {
    return "Schema(id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", attributes=" + this.getAttributes() + ", attributeNamesMap=" + this.getAttributeNamesMap() + ", meta=" + this.getMeta() + ")";
  }

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
  public static class Attribute implements AttributeContainer {

    private static final long serialVersionUID = 1683400114899587851L;

    String getPath() {
      return this.path;
    }

    Attribute setPath(String path) {
      this.path = path;
      return this;
    }

    public @Pattern(regexp = "\\p{Alpha}(-|_|\\p{Alnum})*") String getName() {
      return this.name;
    }

    public Attribute setName(@Pattern(regexp = "\\p{Alpha}(-|_|\\p{Alnum})*") String name) {
      this.name = name;
      return this;
    }

    public Type getType() {
      return this.type;
    }

    public Attribute setType(Type type) {
      this.type = type;
      return this;
    }

    public String getSchemaUrn() {
      return this.schemaUrn;
    }

    public Attribute setSchemaUrn(String schemaUrn) {
      this.schemaUrn = schemaUrn;
      return this;
    }

    public Set<Attribute> getSubAttributes() {
      return this.subAttributes;
    }

    public Attribute setSubAttributes(Set<Attribute> subAttributes) {
      this.subAttributes = subAttributes;
      return this;
    }

    public Map<String, Attribute> getSubAttributeNamesMap() {
      return this.subAttributeNamesMap;
    }

    public Attribute setSubAttributeNamesMap(Map<String, Attribute> subAttributeNamesMap) {
      this.subAttributeNamesMap = subAttributeNamesMap;
      return this;
    }

    public boolean isMultiValued() {
      return this.multiValued;
    }

    public Attribute setMultiValued(boolean multiValued) {
      this.multiValued = multiValued;
      return this;
    }

    public String getDescription() {
      return this.description;
    }

    public Attribute setDescription(String description) {
      this.description = description;
      return this;
    }

    public boolean isRequired() {
      return this.required;
    }

    public Attribute setRequired(boolean required) {
      this.required = required;
      return this;
    }

    public Set<String> getCanonicalValues() {
      return this.canonicalValues;
    }

    public Attribute setCanonicalValues(Set<String> canonicalValues) {
      this.canonicalValues = canonicalValues;
      return this;
    }

    public boolean isCaseExact() {
      return this.caseExact;
    }

    public Attribute setCaseExact(boolean caseExact) {
      this.caseExact = caseExact;
      return this;
    }

    public Mutability getMutability() {
      return this.mutability;
    }

    public Attribute setMutability(Mutability mutability) {
      this.mutability = mutability;
      return this;
    }

    public Returned getReturned() {
      return this.returned;
    }

    public Attribute setReturned(Returned returned) {
      this.returned = returned;
      return this;
    }

    public Uniqueness getUniqueness() {
      return this.uniqueness;
    }

    public Attribute setUniqueness(Uniqueness uniqueness) {
      this.uniqueness = uniqueness;
      return this;
    }

    public List<String> getReferenceTypes() {
      return this.referenceTypes;
    }

    public Attribute setReferenceTypes(List<String> referenceTypes) {
      this.referenceTypes = referenceTypes;
      return this;
    }

    public AttributeAccessor getAccessor() {
      return this.accessor;
    }

    public Attribute setAccessor(AttributeAccessor accessor) {
      this.accessor = accessor;
      return this;
    }

    public boolean isScimResourceIdReference() {
      return this.scimResourceIdReference;
    }

    public Attribute setScimResourceIdReference(boolean scimResourceIdReference) {
      this.scimResourceIdReference = scimResourceIdReference;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Attribute)) return false;
      final Attribute other = (Attribute) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$name = this.getName();
      final Object other$name = other.getName();
      if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
      final Object this$type = this.getType();
      final Object other$type = other.getType();
      if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
      final Object this$schemaUrn = this.getSchemaUrn();
      final Object other$schemaUrn = other.getSchemaUrn();
      if (this$schemaUrn == null ? other$schemaUrn != null : !this$schemaUrn.equals(other$schemaUrn)) return false;
      final Object this$path = this.getPath();
      final Object other$path = other.getPath();
      if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
      final Object this$subAttributes = this.getSubAttributes();
      final Object other$subAttributes = other.getSubAttributes();
      if (this$subAttributes == null ? other$subAttributes != null : !this$subAttributes.equals(other$subAttributes))
        return false;
      final Object this$subAttributeNamesMap = this.getSubAttributeNamesMap();
      final Object other$subAttributeNamesMap = other.getSubAttributeNamesMap();
      if (this$subAttributeNamesMap == null ? other$subAttributeNamesMap != null : !this$subAttributeNamesMap.equals(other$subAttributeNamesMap))
        return false;
      if (this.isMultiValued() != other.isMultiValued()) return false;
      final Object this$description = this.getDescription();
      final Object other$description = other.getDescription();
      if (this$description == null ? other$description != null : !this$description.equals(other$description))
        return false;
      if (this.isRequired() != other.isRequired()) return false;
      final Object this$canonicalValues = this.getCanonicalValues();
      final Object other$canonicalValues = other.getCanonicalValues();
      if (this$canonicalValues == null ? other$canonicalValues != null : !this$canonicalValues.equals(other$canonicalValues))
        return false;
      if (this.isCaseExact() != other.isCaseExact()) return false;
      final Object this$mutability = this.getMutability();
      final Object other$mutability = other.getMutability();
      if (this$mutability == null ? other$mutability != null : !this$mutability.equals(other$mutability)) return false;
      final Object this$returned = this.getReturned();
      final Object other$returned = other.getReturned();
      if (this$returned == null ? other$returned != null : !this$returned.equals(other$returned)) return false;
      final Object this$uniqueness = this.getUniqueness();
      final Object other$uniqueness = other.getUniqueness();
      if (this$uniqueness == null ? other$uniqueness != null : !this$uniqueness.equals(other$uniqueness)) return false;
      final Object this$referenceTypes = this.getReferenceTypes();
      final Object other$referenceTypes = other.getReferenceTypes();
      if (this$referenceTypes == null ? other$referenceTypes != null : !this$referenceTypes.equals(other$referenceTypes))
        return false;
      if (this.isScimResourceIdReference() != other.isScimResourceIdReference()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Attribute;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $name = this.getName();
      result = result * PRIME + ($name == null ? 43 : $name.hashCode());
      final Object $type = this.getType();
      result = result * PRIME + ($type == null ? 43 : $type.hashCode());
      final Object $schemaUrn = this.getSchemaUrn();
      result = result * PRIME + ($schemaUrn == null ? 43 : $schemaUrn.hashCode());
      final Object $path = this.getPath();
      result = result * PRIME + ($path == null ? 43 : $path.hashCode());
      final Object $subAttributes = this.getSubAttributes();
      result = result * PRIME + ($subAttributes == null ? 43 : $subAttributes.hashCode());
      final Object $subAttributeNamesMap = this.getSubAttributeNamesMap();
      result = result * PRIME + ($subAttributeNamesMap == null ? 43 : $subAttributeNamesMap.hashCode());
      result = result * PRIME + (this.isMultiValued() ? 79 : 97);
      final Object $description = this.getDescription();
      result = result * PRIME + ($description == null ? 43 : $description.hashCode());
      result = result * PRIME + (this.isRequired() ? 79 : 97);
      final Object $canonicalValues = this.getCanonicalValues();
      result = result * PRIME + ($canonicalValues == null ? 43 : $canonicalValues.hashCode());
      result = result * PRIME + (this.isCaseExact() ? 79 : 97);
      final Object $mutability = this.getMutability();
      result = result * PRIME + ($mutability == null ? 43 : $mutability.hashCode());
      final Object $returned = this.getReturned();
      result = result * PRIME + ($returned == null ? 43 : $returned.hashCode());
      final Object $uniqueness = this.getUniqueness();
      result = result * PRIME + ($uniqueness == null ? 43 : $uniqueness.hashCode());
      final Object $referenceTypes = this.getReferenceTypes();
      result = result * PRIME + ($referenceTypes == null ? 43 : $referenceTypes.hashCode());
      result = result * PRIME + (this.isScimResourceIdReference() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "Schema.Attribute(name=" + this.getName() + ", type=" + this.getType() + ", schemaUrn=" + this.getSchemaUrn() + ", path=" + this.getPath() + ", subAttributes=" + this.getSubAttributes() + ", subAttributeNamesMap=" + this.getSubAttributeNamesMap() + ", multiValued=" + this.isMultiValued() + ", description=" + this.getDescription() + ", required=" + this.isRequired() + ", canonicalValues=" + this.getCanonicalValues() + ", caseExact=" + this.isCaseExact() + ", mutability=" + this.getMutability() + ", returned=" + this.getReturned() + ", uniqueness=" + this.getUniqueness() + ", referenceTypes=" + this.getReferenceTypes() + ", accessor=" + this.getAccessor() + ", scimResourceIdReference=" + this.isScimResourceIdReference() + ")";
    }

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

    String path;

    @XmlElement
    Set<Attribute> subAttributes;
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

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof FieldAttributeAccessor)) return false;
      final FieldAttributeAccessor other = (FieldAttributeAccessor) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$field = this.field;
      final Object other$field = other.field;
      if (this$field == null ? other$field != null : !this$field.equals(other$field)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof FieldAttributeAccessor;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $field = this.field;
      result = result * PRIME + ($field == null ? 43 : $field.hashCode());
      return result;
    }
  }
}
