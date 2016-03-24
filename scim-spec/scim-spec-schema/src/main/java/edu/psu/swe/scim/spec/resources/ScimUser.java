package edu.psu.swe.scim.spec.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "User")
@XmlType(name = "User", propOrder = {
    "userName",
    "name",
    "displayName",
    "nickName",
    "profileUrl",
    "title",
    "userType",
    "preferredLanguage",
    "locale",
    "timezone",
    "active",
    "password",
    "emails",
    "phoneNumbers",
    "ims",
    "photos",
    "addresses",
    "groups",
    "entitlements",
    "roles",
    "x509Certificates"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class ScimUser extends ScimResource {

  public static final String SCHEMAURI = "urn:ietf:params:scim:schemas:core:2.0:User";

  @XmlElement(name = "active")
  private boolean active_ = true;
  
//  @XmlElement(name = "addresses")
//  private List<ScimAddress> addresses_;
  
  @XmlElement(name = "displayName")
  private String displayName_;
  
//  @XmlElement(name = "emails")
//  private List<ScimEmail> emails_;
  
//  @XmlElement(name = "entitlements")
//  private List<ScimEntitlement> entitlements_;
  
  @XmlElement(name = "groups")
  private List<ScimGroup> groups_;
  
//  @XmlElement(name = "ims")
//  private List<ScimIm> ims_;
  
  @XmlElement(name = "locale")
  private String locale_;
  
//  @XmlElement(name = "name")
//  private ScimName name_;
  
  @XmlElement(name = "nickName")
  private String nickName_;
  
  @XmlElement(name = "password")
  private String password_;
  
//  @XmlElement(name = "phoneNumbers")
//  private List<ScimPhoneNumber> phoneNumbers_;
  
//  @XmlElement(name = "photos")
//  private List<ScimPhoto> photos_;

  @XmlElement(name = "profileUrl")
  private String profileUrl_;
  
  @XmlElement(name = "preferredLanguage")
  private String preferredLanguage_;
  
//  @XmlElement(name = "roles")
//  private List<ScimRole> roles_;
  
  @XmlElement(name = "timezone")
  private String timezone_;
  
  @XmlElement(name = "title")
  private String title_;
  
  @XmlElement(name = "userName")
  private String userName_;
  
  @XmlElement(name = "userType")
  private String userType_;
  
//  @XmlElement(name = "x509Certificates")
//  private List<ScimX509Certificate> x509Certificates_;

}
