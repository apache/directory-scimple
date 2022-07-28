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
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Uniqueness;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true, exclude = {"password"})
@EqualsAndHashCode(callSuper = true, exclude = {"password"})
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
  List<ResourceReference> groups;

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
    super(SCHEMA_URI);
  }

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
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
}
