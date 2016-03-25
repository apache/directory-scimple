package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.resources.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorResponse extends BaseResource {
  
  public static String URN = "urn:ietf:params:scim:api:messages:2.0:Error";
  
  public ErrorResponse() {
    super.addSchema(URN);
  }
  
  @XmlElement(nillable=true)
  private String detail;
  
  @XmlElement
  private String status;
}
