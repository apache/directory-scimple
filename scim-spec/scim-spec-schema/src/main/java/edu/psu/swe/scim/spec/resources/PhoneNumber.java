/**
 * 
 */
package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import lombok.Data;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2>section 4.1.2</a>
 *
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class PhoneNumber {
  
  @XmlElement
  @ScimAttribute(description="Phone number of the User")
  private String value;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  private String display;
  
  @XmlElement
  @ScimAttribute(type=Type.BOOLEAN, canonicalValues={"work", "home", "mobile", "fax", "pager", "other"}, description="A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.")
  private String type;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no more than once.")
  private boolean primary = false;
  
}
