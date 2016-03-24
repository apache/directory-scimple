package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlType(propOrder = {"value","ref","display"})
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceReference {

  @XmlElement
  String value;

  @XmlElement(name = "$ref")
  String ref;

  @XmlElement
  String display;

}
