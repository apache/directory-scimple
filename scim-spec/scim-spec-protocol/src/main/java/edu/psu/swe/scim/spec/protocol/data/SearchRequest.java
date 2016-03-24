package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.schema.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * See Section 3.4.3 Querying Resources Using HTTP POST
 * (https://tools.ietf.org/html/rfc7644#section-3.4.3)
 * 
 * @author crh5255
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class SearchRequest extends BaseResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";

  @XmlElement
  List<String> attributes;

  @XmlElement
  List<String> excludedAttributes;

  @XmlElement
  String filter;

  @XmlElement
  String sortBy;

  @XmlElement
  String sortOrder;

  @XmlElement
  int startIndex;

  @XmlElement
  int count;

}
