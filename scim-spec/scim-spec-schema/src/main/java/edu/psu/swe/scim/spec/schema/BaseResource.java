package edu.psu.swe.scim.spec.schema;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author chrisharm
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource {
  
  @XmlElement(name="schemas")
  @Size(min = 1)
  @Urn
  List<String> schemaUrnList;
  
  public void addSchema(String urn) {
    if (schemaUrnList == null){
      schemaUrnList = new ArrayList<>();
    }
    
    schemaUrnList.add(urn);
  }
  
}
