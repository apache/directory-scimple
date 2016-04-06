package edu.psu.swe.scim.spec.resources;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;

/**
 * All the different variations of SCIM responses require that the object
 * contains a list of the schemas it conforms to.
 * 
 * @author crh5255
 *
 */
@Data
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource {

  @XmlElement(name="schemas")
  @Size(min = 1)
  @Urn
  List<String> schemaUrnList;
  
  public BaseResource(String urn) {
    addSchema(urn);
  }
  
  public void addSchema(String urn) {
    if (schemaUrnList == null){
      schemaUrnList = new ArrayList<>();
    }
    
    schemaUrnList.add(urn);
  }
  
}
