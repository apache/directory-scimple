package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@XmlType(propOrder={"opreration", "path", "value"})
@XmlAccessorType(XmlAccessType.NONE)
public class PatchOperation {
  
  @XmlEnum(String.class)
  public enum Type {
    @XmlEnumValue("add") ADD,
    @XmlEnumValue("remove") REMOVE,
    @XmlEnumValue("replace") REPLACE;
  }
  
  @XmlElement(name="op")
  private Type operation;
  
  @XmlElement
  @XmlJavaTypeAdapter(PatchOperationPathAdapter.class)
  private PatchOperationPath path;
  
  @XmlElement
  private Object value;
  
}
