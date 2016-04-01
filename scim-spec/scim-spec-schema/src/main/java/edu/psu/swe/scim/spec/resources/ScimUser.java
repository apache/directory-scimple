package edu.psu.swe.scim.spec.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.schema.ResourceReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ScimResourceType(name="User", schemaList={ScimUser.SCHEMA_URI})
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScimUser extends ScimResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

  @XmlElement(name = "active")
  boolean active = true;
  
  @XmlElement(name = "addresses")
  List<Address> addresses;
  
  @XmlElement(name = "displayName")
  String displayName;
  
  @XmlElement(name = "emails")
  List<Email> emails;
  
    @XmlElement(name = "entitlements")
  List<Entitlement> entitlements;
  
  @XmlElement(name = "groups")
  List<ResourceReference> groups;
  
  @XmlElement(name = "ims")
  List<Im> ims;
  
  @XmlElement(name = "locale")
  String locale;
  
  @XmlElement(name = "name")
  Name name;
  
  @XmlElement(name = "nickName")
  String nickName;
  
  @XmlElement(name = "password")
  String password;
  
  @XmlElement(name = "phoneNumbers")
  List<PhoneNumber> phoneNumbers;
  
  @XmlElement(name = "photos")
  List<Photo> photos;

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
  
  @XmlElement(name = "x509Certificates")
  List<X509Certificate> x509Certificates;
  
  public ScimUser() {
    super(SCHEMA_URI);
  }
}
