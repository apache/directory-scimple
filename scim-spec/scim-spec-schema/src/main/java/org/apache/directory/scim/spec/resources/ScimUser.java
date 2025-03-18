/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.resources;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Uniqueness;


@ScimResourceType(id = ScimUser.RESOURCE_NAME, name = ScimUser.RESOURCE_NAME, schema = ScimUser.SCHEMA_URI, description = "Top level ScimUser", endpoint = "/Users")
@XmlRootElement(name = ScimUser.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class ScimUser extends ScimResource implements Serializable {

  private static final long serialVersionUID = -2306547717245071997L;
  public static final String RESOURCE_NAME = "User";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the User's administrative status.")
  Boolean active = true;

  @XmlElement
  @ScimAttribute(description="A physical mailing address for this User, as described in (address Element). Canonical Type Values of work, home, and other. The value attribute is a complex type with the following sub-attributes.")
  List<Address> addresses;

  @XmlElement
  @ScimAttribute(description="The name of the User, suitable for display to end-users. The name SHOULD be the full name of the User being described if known")
  String displayName;

  @XmlElement
  @ScimAttribute(description="E-mail addresses for the user. The value SHOULD be canonicalized by the Service Provider, e.g. bjensen@example.com instead of bjensen@EXAMPLE.COM. Canonical Type values of work, home, and other.")
  List<Email> emails;

  @XmlElement
  @ScimAttribute(description="An entitlement may be an additional right to a thing, object, or service")
  List<Entitlement> entitlements;

  @XmlElement
  @ScimAttribute(description="A list of groups that the user belongs to, either thorough direct membership, nested groups, or dynamically calculated")
  List<UserGroup> groups;

  @XmlElement
  @ScimAttribute(description="Instant messaging address for the User.")
  List<Im> ims;

  @XmlElement
  @ScimAttribute(description="Used to indicate the User's default location for purposes of localizing items such as currency, date time format, numerical representations, etc.")
  String locale;

  @XmlElement
  @ScimAttribute(description="The components of the user's real name. Providers MAY return just the full name as a single string in the formatted sub-attribute, or they MAY return just the individual component attributes using the other sub-attributes, or they MAY return both. If both variants are returned, they SHOULD be describing the same name, with the formatted name indicating how the component attributes should be combined.")
  Name name;

  @XmlElement
  @ScimAttribute(description="The casual way to address the user in real life, e.g.'Bob' or 'Bobby' instead of 'Robert'. This attribute SHOULD NOT be used to represent a User's username (e.g. bjensen or mpepperidge)")
  String nickName;

  @XmlElement
  @ScimAttribute(returned = Returned.NEVER, description="The User's clear text password.  This attribute is intended to be used as a means to specify an initial password when creating a new User or to reset an existing User's password.")
  String password;

  @XmlElement
  @ScimAttribute(description="Phone numbers for the User.  The value SHOULD be canonicalized by the Service Provider according to format in RFC3966 e.g. 'tel:+1-201-555-0123'.  Canonical Type values of work, home, mobile, fax, pager and other.")
  List<PhoneNumber> phoneNumbers;

  @XmlElement
  @ScimAttribute(description="URLs of photos of the User.")
  List<Photo> photos;

  @XmlElement
  @ScimAttribute(description="A fully qualified URL to a page representing the User's online profile", referenceTypes={"external"})
  String profileUrl;

  @XmlElement
  @ScimAttribute(description="Indicates the User's preferred written or spoken language.  Generally used for selecting a localized User interface. e.g., 'en_US' specifies the language English and country US.")
  String preferredLanguage;

  @XmlElement
  @ScimAttribute(description="A list of roles for the User that collectively represent who the User is; e.g., 'Student', 'Faculty'.")
  List<Role> roles;

  @XmlElement
  @ScimAttribute(description="The User's time zone in the 'Olson' timezone database format; e.g.,'America/Los_Angeles'")
  String timezone;

  @XmlElement
  @ScimAttribute(description="The user's title, such as \"Vice President.\"")
  String title;

  @XmlElement
  @ScimAttribute(required=true, uniqueness=Uniqueness.SERVER, description="Unique identifier for the User typically used by the user to directly authenticate to the service provider. Each User MUST include a non-empty userName value.  This identifier MUST be unique across the Service Consumer's entire set of Users.  REQUIRED")
  String userName;

  @XmlElement
  @ScimAttribute(description="Used to identify the organization to user relationship. Typical values used might be 'Contractor', 'Employee', 'Intern', 'Temp', 'External', and 'Unknown' but any value may be used.")
  String userType;

  @XmlElement
  @ScimAttribute(description="A list of certificates issued to the User.")
  List<X509Certificate> x509Certificates;

  public ScimUser() {
    super(SCHEMA_URI, RESOURCE_NAME);
  }

  public Optional<Address> getPrimaryAddress() {
    if (addresses == null) {
      return Optional.empty();
    }

    return addresses.stream()
                    .filter(Address::getPrimary)
                    .findFirst();
  }

  public Optional<Email> getPrimaryEmailAddress() {
    if (emails == null) {
      return Optional.empty();
    }

    return emails.stream()
                 .filter(Email::getPrimary)
                 .findFirst();
  }

  public Optional<PhoneNumber> getPrimaryPhoneNumber() {
    if (phoneNumbers == null) {
      return Optional.empty();
    }

    return phoneNumbers.stream()
                       .filter(PhoneNumber::getPrimary)
                       .findFirst();
  }

  @Override
  public ScimUser setSchemas(Set<String> schemas) {
    return (ScimUser) super.setSchemas(schemas);
  }

  @Override
  public ScimUser setMeta(@NotNull Meta meta) {
    return (ScimUser) super.setMeta(meta);
  }

  @Override
  public ScimUser setId(@Size(min = 1) String id) {
    return (ScimUser) super.setId(id);
  }

  @Override
  public ScimUser setExternalId(String externalId) {
    return (ScimUser) super.setExternalId(externalId);
  }

  @Override
  public ScimUser setExtensions(Map<String, ScimExtension> extensions) {
    return (ScimUser) super.setExtensions(extensions);
  }

  @Override
  public ScimUser addSchema(String urn) {
    return (ScimUser) super.addSchema(urn);
  }

  @Override
  public ScimUser addExtension(ScimExtension extension) {
    return (ScimUser) super.addExtension(extension);
  }

  public Boolean getActive() {
    return this.active;
  }

  public ScimUser setActive(Boolean active) {
    this.active = active;
    return this;
  }

  public List<Address> getAddresses() {
    return this.addresses;
  }

  public ScimUser setAddresses(List<Address> addresses) {
    this.addresses = addresses;
    return this;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public List<Email> getEmails() {
    return this.emails;
  }

  public ScimUser setEmails(List<Email> emails) {
    this.emails = emails;
    return this;
  }

  public List<Entitlement> getEntitlements() {
    return this.entitlements;
  }

  public List<UserGroup> getGroups() {
    return this.groups;
  }

  public List<Im> getIms() {
    return this.ims;
  }

  public String getLocale() {
    return this.locale;
  }

  public Name getName() {
    return this.name;
  }

  public String getNickName() {
    return this.nickName;
  }

  public ScimUser setNickName(String nickName) {
    this.nickName = nickName;
    return this;
  }

  public String getPassword() {
    return this.password;
  }

  public ScimUser setPassword(String password) {
    this.password = password;
    return this;
  }

  public List<PhoneNumber> getPhoneNumbers() {
    return this.phoneNumbers;
  }

  public ScimUser setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
    return this;
  }

  public List<Photo> getPhotos() {
    return this.photos;
  }

  public ScimUser setPhotos(List<Photo> photos) {
    this.photos = photos;
    return this;
  }

  public String getProfileUrl() {
    return this.profileUrl;
  }

  public ScimUser setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
    return this;
  }

  public String getPreferredLanguage() {
    return this.preferredLanguage;
  }

  public ScimUser setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
    return this;
  }

  public List<Role> getRoles() {
    return this.roles;
  }

  public ScimUser setRoles(List<Role> roles) {
    this.roles = roles;
    return this;
  }

  public String getTimezone() {
    return this.timezone;
  }

  public ScimUser setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getTitle() {
    return this.title;
  }

  public ScimUser setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getUserName() {
    return this.userName;
  }

  public ScimUser setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  public String getUserType() {
    return this.userType;
  }

  public ScimUser setUserType(String userType) {
    this.userType = userType;
    return this;
  }

  public List<X509Certificate> getX509Certificates() {
    return this.x509Certificates;
  }

  public ScimUser setX509Certificates(List<X509Certificate> x509Certificates) {
    this.x509Certificates = x509Certificates;
    return this;
  }

  public ScimUser setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public ScimUser setEntitlements(List<Entitlement> entitlements) {
    this.entitlements = entitlements;
    return this;
  }

  public ScimUser setGroups(List<UserGroup> groups) {
    this.groups = groups;
    return this;
  }

  public ScimUser setIms(List<Im> ims) {
    this.ims = ims;
    return this;
  }

  public ScimUser setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  public ScimUser setName(Name name) {
    this.name = name;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ScimUser)) return false;
    final ScimUser other = (ScimUser) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$active = this.getActive();
    final Object other$active = other.getActive();
    if (this$active == null ? other$active != null : !this$active.equals(other$active)) return false;
    final Object this$addresses = this.getAddresses();
    final Object other$addresses = other.getAddresses();
    if (this$addresses == null ? other$addresses != null : !this$addresses.equals(other$addresses)) return false;
    final Object this$displayName = this.getDisplayName();
    final Object other$displayName = other.getDisplayName();
    if (this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName))
      return false;
    final Object this$emails = this.getEmails();
    final Object other$emails = other.getEmails();
    if (this$emails == null ? other$emails != null : !this$emails.equals(other$emails)) return false;
    final Object this$entitlements = this.getEntitlements();
    final Object other$entitlements = other.getEntitlements();
    if (this$entitlements == null ? other$entitlements != null : !this$entitlements.equals(other$entitlements))
      return false;
    final Object this$groups = this.getGroups();
    final Object other$groups = other.getGroups();
    if (this$groups == null ? other$groups != null : !this$groups.equals(other$groups)) return false;
    final Object this$ims = this.getIms();
    final Object other$ims = other.getIms();
    if (this$ims == null ? other$ims != null : !this$ims.equals(other$ims)) return false;
    final Object this$locale = this.getLocale();
    final Object other$locale = other.getLocale();
    if (this$locale == null ? other$locale != null : !this$locale.equals(other$locale)) return false;
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
    final Object this$nickName = this.getNickName();
    final Object other$nickName = other.getNickName();
    if (this$nickName == null ? other$nickName != null : !this$nickName.equals(other$nickName)) return false;
    final Object this$phoneNumbers = this.getPhoneNumbers();
    final Object other$phoneNumbers = other.getPhoneNumbers();
    if (this$phoneNumbers == null ? other$phoneNumbers != null : !this$phoneNumbers.equals(other$phoneNumbers))
      return false;
    final Object this$photos = this.getPhotos();
    final Object other$photos = other.getPhotos();
    if (this$photos == null ? other$photos != null : !this$photos.equals(other$photos)) return false;
    final Object this$profileUrl = this.getProfileUrl();
    final Object other$profileUrl = other.getProfileUrl();
    if (this$profileUrl == null ? other$profileUrl != null : !this$profileUrl.equals(other$profileUrl)) return false;
    final Object this$preferredLanguage = this.getPreferredLanguage();
    final Object other$preferredLanguage = other.getPreferredLanguage();
    if (this$preferredLanguage == null ? other$preferredLanguage != null : !this$preferredLanguage.equals(other$preferredLanguage))
      return false;
    final Object this$roles = this.getRoles();
    final Object other$roles = other.getRoles();
    if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
    final Object this$timezone = this.getTimezone();
    final Object other$timezone = other.getTimezone();
    if (this$timezone == null ? other$timezone != null : !this$timezone.equals(other$timezone)) return false;
    final Object this$title = this.getTitle();
    final Object other$title = other.getTitle();
    if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
    final Object this$userName = this.getUserName();
    final Object other$userName = other.getUserName();
    if (this$userName == null ? other$userName != null : !this$userName.equals(other$userName)) return false;
    final Object this$userType = this.getUserType();
    final Object other$userType = other.getUserType();
    if (this$userType == null ? other$userType != null : !this$userType.equals(other$userType)) return false;
    final Object this$x509Certificates = this.getX509Certificates();
    final Object other$x509Certificates = other.getX509Certificates();
    if (this$x509Certificates == null ? other$x509Certificates != null : !this$x509Certificates.equals(other$x509Certificates))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ScimUser;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $active = this.getActive();
    result = result * PRIME + ($active == null ? 43 : $active.hashCode());
    final Object $addresses = this.getAddresses();
    result = result * PRIME + ($addresses == null ? 43 : $addresses.hashCode());
    final Object $displayName = this.getDisplayName();
    result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
    final Object $emails = this.getEmails();
    result = result * PRIME + ($emails == null ? 43 : $emails.hashCode());
    final Object $entitlements = this.getEntitlements();
    result = result * PRIME + ($entitlements == null ? 43 : $entitlements.hashCode());
    final Object $groups = this.getGroups();
    result = result * PRIME + ($groups == null ? 43 : $groups.hashCode());
    final Object $ims = this.getIms();
    result = result * PRIME + ($ims == null ? 43 : $ims.hashCode());
    final Object $locale = this.getLocale();
    result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $nickName = this.getNickName();
    result = result * PRIME + ($nickName == null ? 43 : $nickName.hashCode());
    final Object $phoneNumbers = this.getPhoneNumbers();
    result = result * PRIME + ($phoneNumbers == null ? 43 : $phoneNumbers.hashCode());
    final Object $photos = this.getPhotos();
    result = result * PRIME + ($photos == null ? 43 : $photos.hashCode());
    final Object $profileUrl = this.getProfileUrl();
    result = result * PRIME + ($profileUrl == null ? 43 : $profileUrl.hashCode());
    final Object $preferredLanguage = this.getPreferredLanguage();
    result = result * PRIME + ($preferredLanguage == null ? 43 : $preferredLanguage.hashCode());
    final Object $roles = this.getRoles();
    result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
    final Object $timezone = this.getTimezone();
    result = result * PRIME + ($timezone == null ? 43 : $timezone.hashCode());
    final Object $title = this.getTitle();
    result = result * PRIME + ($title == null ? 43 : $title.hashCode());
    final Object $userName = this.getUserName();
    result = result * PRIME + ($userName == null ? 43 : $userName.hashCode());
    final Object $userType = this.getUserType();
    result = result * PRIME + ($userType == null ? 43 : $userType.hashCode());
    final Object $x509Certificates = this.getX509Certificates();
    result = result * PRIME + ($x509Certificates == null ? 43 : $x509Certificates.hashCode());
    return result;
  }

  public String toString() {
    return "ScimUser(super=" + super.toString() + ", active=" + this.getActive() + ", addresses=" + this.getAddresses() + ", displayName=" + this.getDisplayName() + ", emails=" + this.getEmails() + ", entitlements=" + this.getEntitlements() + ", groups=" + this.getGroups() + ", ims=" + this.getIms() + ", locale=" + this.getLocale() + ", name=" + this.getName() + ", nickName=" + this.getNickName() + ", phoneNumbers=" + this.getPhoneNumbers() + ", photos=" + this.getPhotos() + ", profileUrl=" + this.getProfileUrl() + ", preferredLanguage=" + this.getPreferredLanguage() + ", roles=" + this.getRoles() + ", timezone=" + this.getTimezone() + ", title=" + this.getTitle() + ", userName=" + this.getUserName() + ", userType=" + this.getUserType() + ", x509Certificates=" + this.getX509Certificates() + ")";
  }
}
