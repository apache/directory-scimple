package edu.psu.swe.scim.api.schema;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

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
public class Attribute {

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

  // The attribute name must match the ABNF pattern defined in section 2.1 of
  // the SCIM Schema specification.
  @XmlElement
  @Pattern(regexp = "\\p{Alpha}(-|_|\\p{Alnum})*")
  String name;
  
  @XmlElement
  Type type;
  
  @XmlElement
  List<Attribute> subAttributes;
  
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

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * @return the subAttributes
   */
  public List<Attribute> getSubAttributes() {
    return subAttributes;
  }

  /**
   * @param subAttributes the subAttributes to set
   */
  public void setSubAttributes(List<Attribute> subAttributes) {
    this.subAttributes = subAttributes;
  }

  /**
   * @return the multiValued
   */
  public boolean isMultiValued() {
    return multiValued;
  }

  /**
   * @param multiValued the multiValued to set
   */
  public void setMultiValued(boolean multiValued) {
    this.multiValued = multiValued;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * @param required the required to set
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * @return the canonicalValues
   */
  public Set<String> getCanonicalValues() {
    return canonicalValues;
  }

  /**
   * @param canonicalValues the canonicalValues to set
   */
  public void setCanonicalValues(Set<String> canonicalValues) {
    this.canonicalValues = canonicalValues;
  }

  /**
   * @return the caseExact
   */
  public boolean isCaseExact() {
    return caseExact;
  }

  /**
   * @param caseExact the caseExact to set
   */
  public void setCaseExact(boolean caseExact) {
    this.caseExact = caseExact;
  }

  /**
   * @return the mutability
   */
  public Mutability getMutability() {
    return mutability;
  }

  /**
   * @param mutability the mutability to set
   */
  public void setMutability(Mutability mutability) {
    this.mutability = mutability;
  }

  /**
   * @return the returned
   */
  public Returned getReturned() {
    return returned;
  }

  /**
   * @param returned the returned to set
   */
  public void setReturned(Returned returned) {
    this.returned = returned;
  }

  /**
   * @return the uniqueness
   */
  public Uniqueness getUniqueness() {
    return uniqueness;
  }

  /**
   * @param uniqueness the uniqueness to set
   */
  public void setUniqueness(Uniqueness uniqueness) {
    this.uniqueness = uniqueness;
  }

  /**
   * @return the referenceTypes
   */
  public List<String> getReferenceTypes() {
    return referenceTypes;
  }

  /**
   * @param referenceTypes the referenceTypes to set
   */
  public void setReferenceTypes(List<String> referenceTypes) {
    this.referenceTypes = referenceTypes;
  }

}
