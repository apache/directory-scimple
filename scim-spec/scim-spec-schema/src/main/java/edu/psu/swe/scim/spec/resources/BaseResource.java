package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import edu.psu.swe.scim.spec.validator.Urn;

/**
 * All the different variations of SCIM responses require that the object
 * contains a list of the schemas it conforms to.
 * 
 * @author crh5255
 *
 */
@Data
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource implements Serializable {

  private static final long serialVersionUID = -7603956873008734403L;

  @XmlElement(name="schemas")
  @Size(min = 1)
  @Urn
  Set<String> schemas;
  
  public BaseResource(String urn) {
    addSchema(urn);
  }
  
  public void addSchema(String urn) {
    if (schemas == null){
      schemas = new HashSet<>();
    }
    
    schemas.add(urn);
  }
  
}
