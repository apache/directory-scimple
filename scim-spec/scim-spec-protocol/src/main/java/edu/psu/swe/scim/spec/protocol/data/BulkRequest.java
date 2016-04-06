package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.resources.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkRequest extends BaseResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
  
  @XmlElement
  int failOnErrors;
  
  @XmlElement(name = "Operations")
  List<BulkOperation> operations;
  
  public BulkRequest() {
    super(SCHEMA_URI);
  }
}
