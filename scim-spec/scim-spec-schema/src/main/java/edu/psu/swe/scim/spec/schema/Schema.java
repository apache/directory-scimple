package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.validator.Urn;

/**
 * Defines the structure of the SCIM schemas as defined by section 7 of the SCIM
 * schema specification. See
 * https://tools.ietf.org/html/draft-ietf-scim-core-schema-17#section-7 for more
 * details.
 * 
 * @author Steve Moyer <smoyer@psu.edu>
 */
@XmlRootElement(name = "schema")
@XmlAccessorType(XmlAccessType.NONE)
public class Schema {

  @Urn
  @NotNull
  @Size(min = 1, max = 65535)
  @XmlElement
  String id;

  @XmlElement
  String name;

  @XmlElement
  String description;

  @Size(min = 1, max = 65535)
  @XmlElement
  @XmlElementWrapper(name = "attributes")
  List<Attribute> attributes;
  
  @XmlElement
  Meta meta;

  /**
   * Get the schema's id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the schema's id.
   * 
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the schema's name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the schema's name.
   * 
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the schema's description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the schema's description.
   * 
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the schema's attributes.
   * 
   * @return the attributes
   */
  public List<Attribute> getAttributes() {
    return attributes;
  }

  /**
   * Set the schema's attributes.
   * 
   * @param attributes the attributes to set
   */
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Get the schema's meta object.
   * 
   * @return the meta
   */
  public Meta getMeta() {
    return meta;
  }

  /**
   * Set the schema's meta object.
   * 
   * @param meta the meta to set
   */
  public void setMeta(Meta meta) {
    this.meta = meta;
  }

}