package edu.psu.swe.scim.spec.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.schema.ResourceReference;

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
  boolean active = true;
  
//  @XmlElement(name = "addresses")
//  List<ScimAddress> addresses;
  
  @XmlElement(name = "displayName")
  String displayName;
  
//  @XmlElement(name = "emails")
//  List<ScimEmail> emails;
  
//  @XmlElement(name = "entitlements")
//  List<ScimEntitlement> entitlements;
  
  @XmlElement(name = "groups")
  List<ResourceReference> groups;
  
//  @XmlElement(name = "ims")
//  List<ScimIm> ims;
  
  @XmlElement(name = "locale")
  String locale;
  
  @XmlElement(name = "name")
  Name name;
  
  @XmlElement(name = "nickName")
  String nickName;
  
  @XmlElement(name = "password")
  String password;
  
//  @XmlElement(name = "phoneNumbers")
//  List<ScimPhoneNumber> phoneNumbers;
  
//  @XmlElement(name = "photos")
//  List<ScimPhoto> photos;

  @XmlElement(name = "profileUrl")
  String profileUrl;
  
  @XmlElement(name = "preferredLanguage")
  String preferredLanguage;
  
//  @XmlElement(name = "roles")
//  List<ScimRole> roles;
  
  @XmlElement(name = "timezone")
  String timezone;
  
  @XmlElement(name = "title")
  String title;
  
  @XmlElement(name = "userName")
  String userName;
  
  @XmlElement(name = "userType")
  String userType;
  
//  @XmlElement(name = "x509Certificates")
//  List<ScimX509Certificate> x509Certificates;

}
