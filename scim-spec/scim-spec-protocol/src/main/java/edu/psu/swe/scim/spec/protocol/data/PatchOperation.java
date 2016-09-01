package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PatchOperation {
  
  @XmlElement(name="op")
  private String opreration;
  
  @XmlElement(name="path")
  private String path;
  
  @XmlElement
  private PatchValue value;
}
