package edu.psu.swe.scim.api.schema;

import java.util.Date;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * Defines the structure of the meta attribute for all SCIM resources as defined
 * by section 3.1 of the SCIM schema specification. See
 * https://tools.ietf.org/html/draft-ietf-scim-core-schema-17#section-3.1 for more
 * details.
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
@XmlType(name = "meta")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class Meta {
  
  @XmlElement
  @Size(min = 1)
  String resourceType;
  
  //@Pattern(regexp = "\\p{Digit}{4}-\\p{Digit}{2}-\\p{Digit}{2}T\\p{Digit}{2}:\\p{Digit}{2}:\\p{Digit}{2}Z")
  @XmlElement
  Date created;
  
  //@Pattern(regexp = "\\p{Digit}{4}-\\p{Digit}{2}-\\p{Digit}{2}T\\p{Digit}{2}:\\p{Digit}{2}:\\p{Digit}{2}Z")
  @XmlElement
  Date lastModified;
  
  @XmlElement
  String location;
  
  @XmlElement
  String version;

}
