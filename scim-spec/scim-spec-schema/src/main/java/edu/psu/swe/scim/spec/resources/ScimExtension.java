package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ScimExtension")
public interface ScimExtension {
  
  String getUrn();

}
