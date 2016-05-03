package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ScimExtension")
public interface ScimExtension extends Serializable {
  
  String getUrn();

}
