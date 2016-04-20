package edu.psu.swe.scim.spec.protocol.data;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.BaseResource;

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
  Set<AttributeReference> attributes;

  @XmlElement
  Set<AttributeReference> excludedAttributes;

  @XmlElement
  Filter filter;

  @XmlElement
  AttributeReference sortBy;

  @XmlElement
  SortOrder sortOrder;

  @XmlElement
  Integer startIndex;

  @XmlElement
  Integer count;
  
  public SearchRequest() {
    super(SCHEMA_URI);
  }
  
  public PageRequest getPageRequest() {
    PageRequest pageRequest = new PageRequest();
    pageRequest.setStartIndex(startIndex);
    pageRequest.setCount(count);
    return pageRequest;
  }
  
  public SortRequest getSortRequest() {
    SortRequest sortRequest = new SortRequest();
    sortRequest.setSortBy(sortBy);
    sortRequest.setSortOrder(sortOrder);
    return sortRequest;
  }

}
