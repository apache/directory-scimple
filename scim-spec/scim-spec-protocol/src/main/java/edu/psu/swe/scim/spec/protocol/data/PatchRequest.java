package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.resources.BaseResource;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PatchRequest extends BaseResource {
  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:PatchOp";
  
  public PatchRequest() {
    super(SCHEMA_URI);
  }
  
  @XmlElement
  List<PatchOperation> patchOperationList;
}
