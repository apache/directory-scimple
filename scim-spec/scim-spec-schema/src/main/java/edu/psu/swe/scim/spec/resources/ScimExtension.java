package edu.psu.swe.scim.spec.resources;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ScimExtension")
public interface ScimExtension {
  
  Class<? extends ScimResource> getBaseResource();
  String getUrn();

}
