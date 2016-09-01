/**
 * 
 */
package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;

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

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper=false)
public class PhoneNumber extends KeyedResource implements Serializable {

  private static final long serialVersionUID = 607319505715224096L;

  @XmlElement
  @ScimAttribute(description="Phone number of the User")
  String value;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;
  
  @XmlElement
  @ScimAttribute(canonicalValueList={"work", "home", "mobile", "fax", "pager", "other"}, description="A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.")
  String type;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;
  
  String rawValue;
  String internationalCode;
  String extension;
  
  public void setValue(String value) {
    this.value = value;
    this.rawValue = value;
    
    if (value.startsWith("tel:")) {
      rawValue = value.substring(value.indexOf(':') + 1);
    }
    
    if (rawValue.startsWith("+")) {
      String tmp = rawValue;
      internationalCode = tmp.replaceAll("[- ()].*", "");
    }
    
    if (rawValue.contains(";ext=")) {
      extension = rawValue.substring(rawValue.indexOf("=") + 1);
    }
  }
}
