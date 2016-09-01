package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class PatchValue {

  @XmlElement(name="display")
  private String display;
  
  @XmlElement(name="$ref")
  private String reference;
  
  @XmlElement(name="value")
  private String value;
}
