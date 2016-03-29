/**
 * 
 */
package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@XmlAccessorType(XmlAccessType.NONE)
@Data
public abstract class SimpleTypedMultiValuedAttribute<T extends ScimType> extends TypedMultiValuedAttribute<T> {
  
  @XmlElement(name = "value")
  private String value;

}
