package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimResource;

@Data
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkOperation {

  public enum Method {
    @XmlEnumValue("POST") POST,
    @XmlEnumValue("PUT") PUT,
    @XmlEnumValue("PATCH") PATCH,
    @XmlEnumValue("DELETE") DELETE;
  }
  
  @Data
  @XmlAccessorType(XmlAccessType.NONE)
  public static class Status {
    @XmlElement
    String code;
  }

  @XmlElement
  Method method;
  
  @XmlElement
  String bulkId;
  
  @XmlElement
  String version;
  
  @XmlElement
  String path;
  
  @XmlElement
  ScimResource data;
  
  @XmlElement
  String location;
  
  @XmlElement
  BaseResource response;
  
  @XmlElement
  Status status;
}
