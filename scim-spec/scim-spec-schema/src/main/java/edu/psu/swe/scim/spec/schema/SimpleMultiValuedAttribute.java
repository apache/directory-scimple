/**
 * 
 */
package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public abstract class SimpleMultiValuedAttribute extends MultiValuedAttribute {
  
  @XmlElement(name = "value")
  private String value;
}
