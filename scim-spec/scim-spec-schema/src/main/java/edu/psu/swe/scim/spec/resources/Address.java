package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2>section 4.1.2</a>
 *
 */
@XmlType(name = "address")
@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper=false)
public class Address extends KeyedResource {
    
  @XmlElement(nillable=true)
  @ScimAttribute(canonicalValueList={"work", "home", "other"}, description="A label indicating the attribute's function; e.g., 'aim', 'gtalk', 'mobile' etc.")
  String type;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;
  
  @ScimAttribute(description="The two letter ISO 3166-1 alpha-2 country code")
  @XmlElement(nillable=true)
  private String country;
  
  @ScimAttribute(description="The full mailing address, formatted for display or use with a mailing label. This attribute MAY contain newlines.")
  @XmlElement(nillable=true)
  private String formatted;
  
  @ScimAttribute(description="The city or locality component.")
  @XmlElement(nillable=true)
  private String locality;
  
  @ScimAttribute(description="The zipcode or postal code component.")
  @XmlElement(nillable=true)
  private String postalCode;
  
  @ScimAttribute(description="The state or region component.")
  @XmlElement(nillable=true)
  private String region;
  
  @ScimAttribute(description="The full street address component, which may include house number, street name, PO BOX, and multi-line extended street address information. This attribute MAY contain newlines.")
  @XmlElement(nillable=true)
  private String streetAddress;
}
