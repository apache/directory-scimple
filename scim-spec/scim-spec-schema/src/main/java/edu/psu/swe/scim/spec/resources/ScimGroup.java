package edu.psu.swe.scim.spec.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.api.schema.ResourceReference;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "Group")
@XmlAccessorType(XmlAccessType.NONE)
public class ScimGroup extends ScimResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Group";

  String displayName;
  
  List<ResourceReference> members;

}
