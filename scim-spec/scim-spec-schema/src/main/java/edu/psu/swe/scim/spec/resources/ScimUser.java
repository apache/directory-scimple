package edu.psu.swe.scim.spec.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.schema.ResourceReference;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ScimResourceType(id = ScimUser.RESOURCE_NAME, name = ScimUser.RESOURCE_NAME, schema = ScimUser.SCHEMA_URI, desription = "Top level ScimUser", endpoint = "/Users")
@XmlRootElement(name = ScimUser.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScimUser extends ScimResource {

  public static final String RESOURCE_NAME = "User";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

  @ScimAttribute
  @XmlElement(name = "active")
  boolean active = true;

  @ScimAttribute(canonicalValues = { "home", "work", "other" }, referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "addresses")
  List<Address> addresses;

  @ScimAttribute
  @XmlElement(name = "displayName")
  String displayName;

  @ScimAttribute(canonicalValues = { "home", "work", "other" }, referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "emails")
  List<Email> emails;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "entitlements")
  List<Entitlement> entitlements;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "groups")
  List<ResourceReference> groups;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "ims")
  List<Im> ims;

  @ScimAttribute
  @XmlElement(name = "locale")
  String locale;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "name")
  Name name;

  @ScimAttribute
  @XmlElement(name = "nickName")
  String nickName;

  @ScimAttribute(returned = Returned.NEVER)
  @XmlElement(name = "password")
  String password;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "phoneNumbers")
  List<PhoneNumber> phoneNumbers;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "photos")
  List<Photo> photos;

  @ScimAttribute
  @XmlElement(name = "profileUrl")
  String profileUrl;

  @ScimAttribute
  @XmlElement(name = "preferredLanguage")
  String preferredLanguage;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "roles")
  List<Role> roles;

  @ScimAttribute
  @XmlElement(name = "timezone")
  String timezone;

  @ScimAttribute
  @XmlElement(name = "title")
  String title;

  @ScimAttribute
  @XmlElement(name = "userName")
  String userName;

  @ScimAttribute
  @XmlElement(name = "userType")
  String userType;

  @ScimAttribute(referenceTypes = { Type.COMPLEX })
  @XmlElement(name = "x509Certificates")
  List<X509Certificate> x509Certificates;

  public ScimUser() {
    super(SCHEMA_URI);
  }

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
  }
}
