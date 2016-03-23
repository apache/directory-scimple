package edu.psu.swe.scim.api.schema;

import java.util.Date;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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

  /**
   * @return the resourceType
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * @param resourceType the resourceType to set
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * @return the lastModified
   */
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * @param lastModified the lastModified to set
   */
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

}
