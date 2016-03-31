package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class AuthenticationSchema {

  public enum Type {
    @XmlEnumValue("oauth") OAUTH,
    @XmlEnumValue("oauth2") OAUTH2,
    @XmlEnumValue("oauthbearertoken") OAUTH_BEARER,
    @XmlEnumValue("httpbasic") HTTP_BASIC,
    @XmlEnumValue("httpdigest") HTTP_DIGEST;
  }
 
  @XmlElement
  Type type;
  
  @XmlElement
  String name;
  
  @XmlElement
  String description;
  
  @XmlElement
  String specUri;
  
  @XmlElement
  String documentationUri;
  
}
