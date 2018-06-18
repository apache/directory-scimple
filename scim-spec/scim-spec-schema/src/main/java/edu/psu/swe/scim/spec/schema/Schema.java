package edu.psu.swe.scim.spec.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.validator.Urn;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines the structure of the SCIM schemas as defined by section 7 of the SCIM
 * schema specification. See
 * https://tools.ietf.org/html/draft-ietf-scim-core-schema-17#section-7 for more
 * details.
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
@XmlRootElement(name = "schema")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class Schema implements AttributeContainer {

  private static final Logger LOG = LoggerFactory.getLogger(Schema.class);
  
  public static final String RESOURCE_NAME = "Schema";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Schema";
  
  
  /**
   * Defines the structure of attributes included in SCIM schemas as defined by
   * section 7 of the SCIM schema specification. See
   * https://tools.ietf.org/html/draft-ietf-scim-core-schema-17#section-7 for more
   * details.
   * 
   * @author Steve Moyer <smoyer@psu.edu>
   */
  @XmlType(name = "attribute")
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class Attribute implements AttributeContainer {

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

    String urn;

    // The attribute name must match the ABNF pattern defined in section 2.1 of
    // the SCIM Schema specification.
    @XmlElement
    @Pattern(regexp = "\\p{Alpha}(-|_|\\p{Alnum})*")
    String name;
    
    @XmlElement
    Type type;
    
    @XmlElement
    List<Attribute> subAttributes;
    
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
    
    Field field;

    private boolean scimResourceIdReference;

    @Override
    public List<Attribute> getAttributes() {
      return Collections.unmodifiableList(subAttributes);
    }
    
    public void setSubAttributes(List<Attribute> attributes, AddAction action) {
      
      if (action.equals(AddAction.REPLACE)) {
        subAttributeNamesMap.clear();
      }
      
      for (Attribute attribute : attributes) {
        String name = attribute.getName();
        if (name == null) {
          LOG.warn("Attribute name was null, skipping name indexing");
          continue;
        }
        subAttributeNamesMap.put(name.toLowerCase(), attribute);
      }
      
      if(action.equals(AddAction.REPLACE)) {
        this.subAttributes = attributes;
      } else {
        if (subAttributes == null) {
          subAttributes = new ArrayList<>();
        }
        this.subAttributes.addAll(attributes);
      }
    }
    
    public Attribute getAttribute(String name) {
      if (name == null) {
        return null;
      }
      return subAttributeNamesMap.get(name.toLowerCase());
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
  List<Attribute> attributes;
  
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  Map<String, Attribute> attributeNamesMap = new HashMap<>();
  
  @XmlElement
  Meta meta;
  
  public List<Attribute> getAttributes() {
    return Collections.unmodifiableList(attributes);
  }
  
  public void setAttributes(List<Attribute> attributes) {
    attributeNamesMap.clear();
    
    for (Attribute attribute : attributes) {
      String name = attribute.getName();
      if (name == null) {
        LOG.warn("Attribute name was null, skipping name indexing");
        continue;
      }
      attributeNamesMap.put(name.toLowerCase(), attribute);
    }
    
    this.attributes = attributes;
  }
  
  public Attribute getAttribute(String name) {
    if (name == null) {
      return null;
    }
    return attributeNamesMap.get(name.toLowerCase());
  }
}