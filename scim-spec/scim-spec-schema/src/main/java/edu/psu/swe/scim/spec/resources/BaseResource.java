package edu.psu.swe.scim.spec.resources;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import edu.psu.swe.scim.spec.validator.Urn;
@Data
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BaseResource {

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
