package edu.psu.swe.scim.api.schema;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author chrisharm
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource {
  
  @XmlElement
  @Size(min = 1)
  List<Schema> schemas;
  
  /**
   * @return the schemas
   */
  public List<Schema> getSchemas() {
    return schemas;
  }

  /**
   * @param schemas the schemas to set
   */
  public void setSchemas(List<Schema> schemas) {
    this.schemas = schemas;
  }

}
