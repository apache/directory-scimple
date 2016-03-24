package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlType(name = "name", propOrder = {
    "formatted",
    "familyName",
    "givenName",
    "middleName",
    "honorificPrefix",
    "honorificSuffix"    
})
public class Name {

  @XmlElement
  String formatted;

  @XmlElement
  String familyName;

  @XmlElement
  String givenName;

  @XmlElement
  String middleName;

  @XmlElement
  String honorificPrefix;

  @XmlElement
  String honorificSuffix;

}
