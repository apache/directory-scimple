package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.schema.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ListResponse extends BaseResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

  @XmlElement
  int totalResults;
  
  @XmlElement
  int startIndex;
  
  @XmlElement
  int itemsPerPage;

  @XmlElement(name = "Resources")
  List<Object> resources;

}
