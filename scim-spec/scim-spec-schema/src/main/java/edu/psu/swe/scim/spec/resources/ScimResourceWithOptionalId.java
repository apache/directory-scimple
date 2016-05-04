package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class overrides the required id element in ScimResource for use as a
 * base class for some of the odd SCIM resources.
 * 
 * @author crh5255
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResourceWithOptionalId extends ScimResource {
  
  private static final long serialVersionUID = -379538554565387791L;

  @XmlElement
  String id;
  
  public ScimResourceWithOptionalId(String urn) {
    super(urn);
  }
  
}
