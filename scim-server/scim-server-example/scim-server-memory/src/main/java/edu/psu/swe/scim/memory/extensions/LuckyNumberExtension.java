package edu.psu.swe.scim.memory.extensions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import lombok.Data;

/**
 * Allows a User's lucky number to be passed as part of the User's entry via
 * the SCIM protocol.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@XmlRootElement( name = "LuckyNumberExtension", namespace = "http://www.psu.edu/schemas/psu-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = LuckyNumberExtension.SCHEMA_URN, description="Lucky Numbers", name="LuckyNumbers", required=true)
public class LuckyNumberExtension implements ScimExtension {
  
  public static final String  SCHEMA_URN = "urn:mem:params:scim:schemas:extension:LuckyNumberExtension";

  @ScimAttribute(type=Type.INTEGER, returned=Returned.DEFAULT, required=true)
  @XmlElement
  private long luckyNumber;
  
  /**
   * Provides the URN associated with this extension which, as defined by the
   * SCIM specification is the extension's unique identifier.
   * 
   * @return The extension's URN.
   */
  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

}
