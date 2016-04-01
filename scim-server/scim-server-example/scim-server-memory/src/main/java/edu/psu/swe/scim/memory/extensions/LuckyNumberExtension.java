package edu.psu.swe.scim.memory.extensions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;

@XmlRootElement( name = "LuckyNumberExtension", namespace = "http://www.psu.edu/schemas/psu-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class LuckyNumberExtension implements ScimExtension {
  
  private static final String  URN_STRING = "urn:mem:params:scim:schemas:extension:LuckyNumberExtension";

  
  @Override
  public Class<? extends ScimResource> getBaseResource() {
    return ScimUser.class;
  }

  @Override
  public String getUrn() {
    return URN_STRING;
  }

}
