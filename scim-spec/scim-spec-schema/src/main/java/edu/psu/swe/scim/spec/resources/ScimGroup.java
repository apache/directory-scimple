package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.schema.ResourceReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ScimResourceType(id = ScimGroup.RESOURCE_NAME, name = ScimGroup.RESOURCE_NAME, schema = ScimGroup.SCHEMA_URI, description = "Top level ScimGroup", endpoint = "/Groups")
@XmlRootElement(name = ScimGroup.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class ScimGroup extends ScimResource {

  private static final long serialVersionUID = 4424638498347469070L;
  public static final String RESOURCE_NAME = "Group";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Group";

  @XmlElement
  @ScimAttribute(description="A human-readable name for the Group.", required=true)
  String displayName;
  
  @XmlElement
  @ScimAttribute(description = "A list of members of the Group.")
  List<ResourceReference> members;

  public ScimGroup() {
    super(SCHEMA_URI);
  }

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
  }
}
