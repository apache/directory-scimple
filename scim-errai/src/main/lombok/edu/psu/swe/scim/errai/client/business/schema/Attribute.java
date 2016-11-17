/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.schema;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO representing the wire format of each of a SCIM Schema's
 * attributes.  See section 7 of the SCIM Schema Specification at:
 * 
 * https://tools.ietf.org/html/rfc7643#section-7
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
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
