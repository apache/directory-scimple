package edu.psu.swe.scim.spec.resources;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@XmlType
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
public class KeyedResource {
  
  private String key;
  
  public KeyedResource() {
    key = UUID.randomUUID().toString();
  }

}
