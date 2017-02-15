package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PatchOperation {
  
  public enum Type {
    @XmlEnumValue("add") ADD,
    @XmlEnumValue("remove") REMOVE,
    @XmlEnumValue("replace") REPLACE;
  }
  
  @XmlElement(name="op")
  private Type opreration;
  
  @XmlElement(name="path")
  private PatchOperationPath path;
  
  @XmlElement
  private PatchValue value;
  
}
