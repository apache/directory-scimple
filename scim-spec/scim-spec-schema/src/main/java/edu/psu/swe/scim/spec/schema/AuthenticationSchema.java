package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlEnumValue;

public class AuthenticationSchema {

  public enum Type {
    @XmlEnumValue("oauth") OAUTH,
    @XmlEnumValue("oauth2") OAUTH2,
    @XmlEnumValue("oauthbearertoken") OAUTH_BEARER,
    @XmlEnumValue("httpbasic") HTTP_BASIC,
    @XmlEnumValue("httpdigest") HTTP_DIGEST;
  }
  
  Type type;
  
  String name;
  
  String description;
  
  String specUri;
  
  String documentationUri;
  
}
