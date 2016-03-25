package edu.psu.swe.scim.spec.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.validator.Urn;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author smoyer1
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResource extends BaseResource {

  @XmlElement
  @NotNull
  Meta meta;
  
  @XmlElement
  @Size(min = 1, max = 65535)
  String id;
  
  @XmlElement
  String externalId;
  
  private Map<String, ScimExtension> extensions = new HashMap<String, ScimExtension>();

}
