/**
 * 
 */
package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper=true)
public abstract class SimpleTypedMultiValuedAttribute extends TypedMultiValuedAttribute {
  
  @XmlElement(name = "value")
  private String value;

}
