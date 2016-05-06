package edu.psu.swe.scim.spec.schema;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.adapter.LocalDateTimeAdapter;
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
public class Meta implements Serializable {
  
  private static final long serialVersionUID = -9162917034280030708L;

  @XmlElement
  @Size(min = 1)
  String resourceType;
  
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  LocalDateTime created;
  
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  LocalDateTime lastModified;
  
  @XmlElement
  String location;
  
  @XmlElement
  String version;

}
