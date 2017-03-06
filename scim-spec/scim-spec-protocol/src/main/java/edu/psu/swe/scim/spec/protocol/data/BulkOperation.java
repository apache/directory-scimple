package edu.psu.swe.scim.spec.protocol.data;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.resources.BaseResource;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.AllArgsConstructor;
import lombok.Data;

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
  @AllArgsConstructor
  @XmlAccessorType(XmlAccessType.NONE)
  public static class StatusWrapper {
    
    public static StatusWrapper wrap(Status code) {
      return new StatusWrapper(code);
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(StatusAdapter.class)
    Status code;
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
  StatusWrapper status;
}
