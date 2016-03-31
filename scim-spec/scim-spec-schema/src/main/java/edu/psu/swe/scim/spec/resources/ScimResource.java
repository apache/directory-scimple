package edu.psu.swe.scim.spec.resources;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.schema.Meta;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author smoyer1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResource extends BaseResource {

  @XmlElement
  @NotNull
  Meta meta;
  
  @XmlElement
  @Size(min = 1)
  String id;
  
  @XmlElement
  String externalId;
  
  // TODO - Figure out JAXB equivalent of JsonAnyGetter and JsonAnySetter (XmlElementAny?)
  private Map<String, ScimExtension> extensions = new HashMap<String, ScimExtension>();
  
  public ScimResource(String urn) {
    super(urn);
  }
  
  public void addExtension(String urn, ScimExtension extension) {
    extensions.put(urn, extension);
  }

}
