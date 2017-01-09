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
public class ListResponse<T> extends BaseResource {

  private static final long serialVersionUID = -2381780997440673136L;

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
  
  @XmlElement
  int totalResults;
  
  @XmlElement
  Integer startIndex;
  
  @XmlElement
  Integer itemsPerPage;

  @XmlElement(name = "Resources")
  List<T> resources;

  public ListResponse() {
    super(SCHEMA_URI);
  }
}
