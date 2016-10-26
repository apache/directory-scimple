/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.schema;

import lombok.Data;

/**
 * @author smoyer1
 *
 */
@Data
public class Attribute {
  
  public enum Mutability {
    READ_ONLY,
    READ_WRITE,
    IMMUTABLE,
    WRITE_ONLY,
    ;
  }
  
  public enum Returned {
    ALWAYS,
    NEVER,
    DEFAULT,
    REQUEST,
    ;
  }
  
  public enum Uniqueness {
    NONE,
    SERVER,
    GLOBAL,
    ;
  }
  
  public enum Type {
    STRING,
    BOOLEAN,
    DECIMAL,
    INTEGER,
    DATE_TIME,
    REFERENCE,
    COMPLEX,
    ;
  }
  
  String name;
  Type type;
  Attribute[] subAttributes;
  boolean multivalued;
  String description;
  boolean required = false;
  String[] canonicalValues;
  boolean caseExact;
  Mutability mutability;
  Returned returned;
  Uniqueness uniqueness;
  String[] referenceTypes;

}
