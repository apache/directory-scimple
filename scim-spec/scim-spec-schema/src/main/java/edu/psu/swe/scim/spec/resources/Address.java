package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.schema.TypedMultiValuedAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2>section 4.1.2</a>
 *
 */
@XmlType(name = "address")
@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper=true)
public class Address extends TypedMultiValuedAttribute {
  
  @XmlElement(nillable=true)
  private String country;
  
  @XmlElement(nillable=true)
  private String formatted;
  
  @XmlElement(nillable=true)
  private String locality;
  
  @XmlElement(nillable=true)
  private String postalCode;
  
  @XmlElement(nillable=true)
  private String region;
  
  @XmlElement(nillable=true)
  private String streetAddress;
}
