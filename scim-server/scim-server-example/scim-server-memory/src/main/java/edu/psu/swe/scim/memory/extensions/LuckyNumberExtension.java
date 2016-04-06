package edu.psu.swe.scim.memory.extensions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;

@XmlRootElement( name = "LuckyNumberExtension", namespace = "http://www.psu.edu/schemas/psu-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = LuckyNumberExtension.SCHEMA_URN, description="Lucky Numbers", name="LuckyNumbers", required=true)
public class LuckyNumberExtension implements ScimExtension {
  
  public static final String  SCHEMA_URN = "urn:mem:params:scim:schemas:extension:LuckyNumberExtension";

  @ScimAttribute(type=Type.INTEGER, returned=Returned.DEFAULT, required=true)
  @XmlElement
  private long luckyNumber;
  
  @Override
  public Class<? extends ScimResource> getBaseResource() {
    return ScimUser.class;
  }

  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

}
