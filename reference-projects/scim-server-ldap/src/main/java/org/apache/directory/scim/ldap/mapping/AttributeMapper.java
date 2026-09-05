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

package org.apache.directory.scim.ldap.mapping;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.scim.ldap.ldap.ScimLdapConfig;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts between SCIM resources ({@link ScimUser}, {@link ScimGroup}) and Apache Directory
 * LDAP {@link Entry} objects using configurable attribute mappings from {@link ScimLdapConfig}.
 *
 * <p>This CDI bean reads its SCIM-to-LDAP attribute mapping at startup and provides
 * bidirectional conversion methods as well as lookup helpers used by
 * {@link FilterTranslator} to resolve SCIM attribute paths to their LDAP counterparts.
 * DN construction follows RFC 4514 escaping via {@link Rdn}.</p>
 */
@ApplicationScoped
public class AttributeMapper {

  private static final Logger LOG = LoggerFactory.getLogger(AttributeMapper.class);

  /**
   * Formatter for LDAP GeneralizedTime (RFC 4517), e.g. {@code "20240115143000Z"} or
   * {@code "20240115143000.12Z"}. {@code appendFraction} handles 1–3 fractional-second
   * digits uniformly; a simple pattern string cannot do this correctly.
   */
  private static final java.time.format.DateTimeFormatter GENERALIZED_TIME =
    new DateTimeFormatterBuilder()
      .appendPattern("yyyyMMddHHmmss")
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 1, 3, true)
      .optionalEnd()
      .appendPattern("X")
      .toFormatter();

  @Inject
  ScimLdapConfig properties;

  private Map<String, String> scimToLdapUser;
  private Map<String, String> scimToLdapGroup;

  private List<String> userObjectClasses;
  private String userRdnAttribute;
  private List<String> groupObjectClasses;
  private String groupRdnAttribute;

  protected AttributeMapper() {}

  @PostConstruct
  void init() {
    userObjectClasses = properties.getUserObjectClasses();
    userRdnAttribute = properties.getUserRdnAttribute();
    scimToLdapUser = new LinkedHashMap<>(properties.getUserAttributes());

    groupObjectClasses = properties.getGroupObjectClasses();
    groupRdnAttribute = properties.getGroupRdnAttribute();
    scimToLdapGroup = new LinkedHashMap<>(properties.getGroupAttributes());

    LOG.info("Attribute mapping initialized: user objectClasses={}, group objectClasses={}",
      userObjectClasses, groupObjectClasses);
  }

  /**
   * Maps an LDAP {@link Entry} to a {@link ScimUser}, populating standard SCIM core
   * attributes (userName, name, displayName, emails, phoneNumbers, addresses, title,
   * userType, and active) according to the configured attribute mapping.
   *
   * <p>The SCIM {@code id} is taken from the LDAP operational attribute {@code entryUUID}.</p>
   *
   * @param entry the LDAP entry to convert
   * @return a populated {@link ScimUser}; fields for which no LDAP value exists are left unset
   * @throws LdapInvalidAttributeValueException if an LDAP attribute value cannot be read
   */
  public ScimUser toScimUser(Entry entry) throws LdapInvalidAttributeValueException {
    ScimUser user = new ScimUser();

    // id from entryUUID (operational attribute)
    String entryUuid = getStringAttribute(entry, "entryUUID");
    if (entryUuid != null) {
      user.setId(entryUuid);
    }

    // userName
    String uid = getStringAttribute(entry, scimToLdapUser.get("userName"));
    if (uid != null) {
      user.setUserName(uid);
    }

    // name
    Name name = new Name();
    boolean hasName = false;
    String givenName = getStringAttribute(entry, scimToLdapUser.get("name.givenName"));
    if (givenName != null) {
      name.setGivenName(givenName);
      hasName = true;
    }
    String familyName = getStringAttribute(entry, scimToLdapUser.get("name.familyName"));
    if (familyName != null) {
      name.setFamilyName(familyName);
      hasName = true;
    }
    String formatted = getStringAttribute(entry, scimToLdapUser.get("name.formatted"));
    if (formatted != null) {
      name.setFormatted(formatted);
      hasName = true;
    }
    if (hasName) {
      user.setName(name);
    }

    // displayName
    String displayName = getStringAttribute(entry, scimToLdapUser.get("displayName"));
    if (displayName != null) {
      user.setDisplayName(displayName);
    }

    // emails (multi-valued)
    List<Email> emails = getMultiValuedAsEmails(entry, scimToLdapUser.get("emails.value"));
    if (!emails.isEmpty()) {
      user.setEmails(emails);
    }

    // phoneNumbers (multi-valued)
    List<PhoneNumber> phones = getMultiValuedAsPhoneNumbers(entry, scimToLdapUser.get("phoneNumbers.value"));
    if (!phones.isEmpty()) {
      user.setPhoneNumbers(phones);
    }

    // addresses
    Address address = new Address();
    boolean hasAddress = false;
    String street = getStringAttribute(entry, scimToLdapUser.get("addresses.streetAddress"));
    if (street != null) {
      address.setStreetAddress(street);
      hasAddress = true;
    }
    String locality = getStringAttribute(entry, scimToLdapUser.get("addresses.locality"));
    if (locality != null) {
      address.setLocality(locality);
      hasAddress = true;
    }
    String postalCode = getStringAttribute(entry, scimToLdapUser.get("addresses.postalCode"));
    if (postalCode != null) {
      address.setPostalCode(postalCode);
      hasAddress = true;
    }
    if (hasAddress) {
      user.setAddresses(List.of(address));
    }

    // title
    String title = getStringAttribute(entry, scimToLdapUser.get("title"));
    if (title != null) {
      user.setTitle(title);
    }

    // userType
    String userType = getStringAttribute(entry, scimToLdapUser.get("userType"));
    if (userType != null) {
      user.setUserType(userType);
    }

    // active (stored as string "true"/"false" in custom attribute)
    String activeStr = getStringAttribute(entry, "scimActive");
    user.setActive(activeStr == null || Boolean.parseBoolean(activeStr));

    // meta — version (ETag), created, lastModified from LDAP operational attributes
    String modifyTs = getStringAttribute(entry, "modifyTimestamp");
    String createTs = getStringAttribute(entry, "createTimestamp");
    Meta meta = new Meta()
      .setResourceType("User")
      .setCreated(parseLdapTimestamp(createTs))
      .setLastModified(parseLdapTimestamp(modifyTs));
    // Fall back to createTimestamp when entry has never been modified
    String etagSource = modifyTs != null ? modifyTs : createTs;
    if (etagSource != null) {
      meta.setVersion("W/\"" + etagSource + "\"");
    }
    user.setMeta(meta);

    return user;
  }

  /**
   * Converts a {@link ScimUser} into an LDAP {@link Entry} suitable for an add operation.
   *
   * <p>The entry's DN is constructed from the configured RDN attribute and the user's
   * {@code userName}, placed under the given {@code baseDn}. Required
   * {@code inetOrgPerson} attributes ({@code sn}, {@code cn}) are filled with sensible
   * defaults when not explicitly mapped. Phone-number type metadata is stored in a
   * custom {@code scimPhoneTypes} attribute for round-trip fidelity.</p>
   *
   * @param user   the SCIM user to convert
   * @param baseDn the LDAP base DN under which the entry will be created
   * @return a fully populated {@link Entry} ready to be added to the directory
   * @throws LdapException              if the DN cannot be constructed or an attribute is invalid
   * @throws IllegalArgumentException   if the user's {@code userName} is {@code null} or blank
   */
  public Entry toEntry(ScimUser user, String baseDn) throws LdapException {
    String rdnValue = user.getUserName();
    if (rdnValue == null || rdnValue.isBlank()) {
      throw new IllegalArgumentException("userName is required to create an LDAP entry");
    }

    Dn dn = buildDn(userRdnAttribute, rdnValue, baseDn);
    Entry entry = new DefaultEntry(dn);
    entry.add("objectClass", userObjectClasses.toArray(new String[0]));

    entry.add(scimToLdapUser.get("userName"), user.getUserName());

    if (user.getName() != null) {
      if (user.getName().getGivenName() != null) {
        entry.add(scimToLdapUser.get("name.givenName"), user.getName().getGivenName());
      }
      if (user.getName().getFamilyName() != null) {
        entry.add(scimToLdapUser.get("name.familyName"), user.getName().getFamilyName());
      }
      if (user.getName().getFormatted() != null) {
        entry.add(scimToLdapUser.get("name.formatted"), user.getName().getFormatted());
      }
    }

    // Ensure required inetOrgPerson attributes
    if (!entry.containsAttribute("sn")) {
      entry.add("sn", user.getUserName());
    }
    if (!entry.containsAttribute("cn")) {
      String cn = user.getDisplayName() != null ? user.getDisplayName() : user.getUserName();
      entry.add("cn", cn);
    }

    if (user.getDisplayName() != null) {
      entry.add(scimToLdapUser.get("displayName"), user.getDisplayName());
    }

    if (user.getEmails() != null) {
      for (Email email : user.getEmails()) {
        if (email.getValue() != null) {
          entry.add(scimToLdapUser.get("emails.value"), email.getValue());
        }
      }
    }

    if (user.getPhoneNumbers() != null) {
      for (PhoneNumber phone : user.getPhoneNumbers()) {
        if (phone.getValue() != null) {
          entry.add(scimToLdapUser.get("phoneNumbers.value"), phone.getValue());
        }
      }
      // Store phone type metadata in a custom attribute for round-trip fidelity
      StringBuilder phoneTypes = new StringBuilder();
      for (PhoneNumber phone : user.getPhoneNumbers()) {
        if (phoneTypes.length() > 0) {
          phoneTypes.append(",");
        }
        phoneTypes.append(phone.getType() != null ? phone.getType() : "");
      }
      entry.add("scimPhoneTypes", phoneTypes.toString());
    }

    if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
      Address addr = user.getAddresses().get(0);
      if (addr.getStreetAddress() != null) {
        entry.add(scimToLdapUser.get("addresses.streetAddress"), addr.getStreetAddress());
      }
      if (addr.getLocality() != null) {
        entry.add(scimToLdapUser.get("addresses.locality"), addr.getLocality());
      }
      if (addr.getPostalCode() != null) {
        entry.add(scimToLdapUser.get("addresses.postalCode"), addr.getPostalCode());
      }
    }

    if (user.getTitle() != null) {
      entry.add(scimToLdapUser.get("title"), user.getTitle());
    }
    if (user.getUserType() != null) {
      entry.add(scimToLdapUser.get("userType"), user.getUserType());
    }
    if (user.getPassword() != null) {
      entry.add(scimToLdapUser.get("password"), user.getPassword());
    }

    // active flag (custom attribute)
    if (user.getActive() != null) {
      entry.add("scimActive", user.getActive().toString());
    }

    return entry;
  }

  /**
   * Maps an LDAP {@link Entry} to a {@link ScimGroup}, populating the display name and
   * group members according to the configured attribute mapping.
   *
   * <p>Member DNs are stored as {@link GroupMembership#setValue(String) membership values};
   * the repository layer is responsible for resolving them to {@code entryUUID} identifiers
   * if needed. Empty member placeholders (used by some LDAP servers for
   * {@code groupOfNames} compliance) are skipped.</p>
   *
   * @param entry the LDAP entry to convert
   * @return a populated {@link ScimGroup}
   * @throws LdapInvalidAttributeValueException if an LDAP attribute value cannot be read
   */
  public ScimGroup toScimGroup(Entry entry) throws LdapInvalidAttributeValueException {
    ScimGroup group = new ScimGroup();

    String entryUuid = getStringAttribute(entry, "entryUUID");
    if (entryUuid != null) {
      group.setId(entryUuid);
    }

    String displayName = getStringAttribute(entry, scimToLdapGroup.get("displayName"));
    if (displayName != null) {
      group.setDisplayName(displayName);
    }

    // members (multi-valued DNs)
    String memberAttr = scimToLdapGroup.get("members.value");
    Attribute members = entry.get(memberAttr);
    if (members != null) {
      List<GroupMembership> memberList = new ArrayList<>();
      for (Value val : members) {
        String memberDn = val.getString();
        // Skip empty member placeholder used by some LDAP servers
        if (memberDn != null && !memberDn.isBlank()) {
          GroupMembership membership = new GroupMembership();
          // Store the DN as the value; the repository will resolve to entryUUID if needed
          membership.setValue(memberDn);
          memberList.add(membership);
        }
      }
      group.setMembers(memberList);
    }

    // meta — version (ETag), created, lastModified from LDAP operational attributes
    String modifyTs = getStringAttribute(entry, "modifyTimestamp");
    String createTs = getStringAttribute(entry, "createTimestamp");
    Meta meta = new Meta()
      .setResourceType("Group")
      .setCreated(parseLdapTimestamp(createTs))
      .setLastModified(parseLdapTimestamp(modifyTs));
    String etagSource = modifyTs != null ? modifyTs : createTs;
    if (etagSource != null) {
      meta.setVersion("W/\"" + etagSource + "\"");
    }
    group.setMeta(meta);

    return group;
  }

  /**
   * Converts a {@link ScimGroup} into an LDAP {@link Entry} suitable for an add operation.
   *
   * <p>The entry's DN is constructed from the configured group RDN attribute and the
   * group's {@code displayName}, placed under the given {@code baseDn}. If the group
   * has no members, an empty placeholder value is added to satisfy the
   * {@code groupOfNames} schema requirement for at least one {@code member} attribute.</p>
   *
   * @param group  the SCIM group to convert
   * @param baseDn the LDAP base DN under which the entry will be created
   * @return a fully populated {@link Entry} ready to be added to the directory
   * @throws LdapException            if the DN cannot be constructed or an attribute is invalid
   * @throws IllegalArgumentException if the group's {@code displayName} is {@code null} or blank
   */
  public Entry toEntry(ScimGroup group, String baseDn) throws LdapException {
    String rdnValue = group.getDisplayName();
    if (rdnValue == null || rdnValue.isBlank()) {
      throw new IllegalArgumentException("displayName is required to create an LDAP group entry");
    }

    Dn dn = buildDn(groupRdnAttribute, rdnValue, baseDn);
    Entry entry = new DefaultEntry(dn);
    entry.add("objectClass", groupObjectClasses.toArray(new String[0]));
    entry.add(scimToLdapGroup.get("displayName"), group.getDisplayName());

    if (group.getMembers() != null && !group.getMembers().isEmpty()) {
      for (GroupMembership member : group.getMembers()) {
        if (member.getValue() != null) {
          entry.add(scimToLdapGroup.get("members.value"), member.getValue());
        }
      }
    } else {
      // groupOfNames requires at least one member; use a placeholder
      entry.add(scimToLdapGroup.get("members.value"), "");
    }

    return entry;
  }

  /**
   * Resolves a SCIM user attribute path (e.g. {@code "userName"} or {@code "name.givenName"})
   * to the corresponding LDAP attribute name.
   *
   * @param scimAttribute the SCIM attribute path
   * @return the mapped LDAP attribute name, or {@code null} if no mapping exists
   */
  public String getLdapUserAttribute(String scimAttribute) {
    return scimToLdapUser.get(scimAttribute);
  }

  /**
   * Resolves a SCIM group attribute path (e.g. {@code "displayName"} or {@code "members.value"})
   * to the corresponding LDAP attribute name.
   *
   * @param scimAttribute the SCIM attribute path
   * @return the mapped LDAP attribute name, or {@code null} if no mapping exists
   */
  public String getLdapGroupAttribute(String scimAttribute) {
    return scimToLdapGroup.get(scimAttribute);
  }

  /** Returns the LDAP attribute used as the RDN for user entries (e.g. {@code "uid"}). */
  public String getUserRdnAttribute() {
    return userRdnAttribute;
  }

  /** Returns the LDAP attribute used as the RDN for group entries (e.g. {@code "cn"}). */
  public String getGroupRdnAttribute() {
    return groupRdnAttribute;
  }

  /** Returns the LDAP object classes assigned to user entries. */
  public List<String> getUserObjectClasses() {
    return userObjectClasses;
  }

  /** Returns the LDAP object classes assigned to group entries. */
  public List<String> getGroupObjectClasses() {
    return groupObjectClasses;
  }

  /**
   * Parses an LDAP GeneralizedTime string (RFC 4517) to a UTC {@link LocalDateTime}.
   *
   * @param value the GeneralizedTime string, e.g. {@code "20240115143000Z"}
   * @return the parsed UTC instant as a {@link LocalDateTime}, or {@code null} if the value
   *         is blank or cannot be parsed
   */
  private static LocalDateTime parseLdapTimestamp(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return ZonedDateTime.parse(value, GENERALIZED_TIME)
        .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    } catch (DateTimeParseException e) {
      LOG.warn("Failed to parse LDAP GeneralizedTime '{}': {}", value, e.getMessage());
      return null;
    }
  }

  private static Dn buildDn(String rdnAttribute, String rdnValue, String baseDn) throws LdapException {
    return new Dn(new Rdn(rdnAttribute, rdnValue), new Dn(baseDn));
  }

  private String getStringAttribute(Entry entry, String ldapAttrName) throws LdapInvalidAttributeValueException {
    if (ldapAttrName == null) {
      return null;
    }
    Attribute attr = entry.get(ldapAttrName);
    if (attr != null) {
      return attr.getString();
    }
    return null;
  }

  private List<Email> getMultiValuedAsEmails(Entry entry, String ldapAttrName) throws LdapInvalidAttributeValueException {
    List<Email> result = new ArrayList<>();
    if (ldapAttrName == null) {
      return result;
    }
    Attribute attr = entry.get(ldapAttrName);
    if (attr != null) {
      boolean first = true;
      for (Value val : attr) {
        Email email = new Email();
        email.setValue(val.getString());
        if (first) {
          email.setPrimary(true);
          first = false;
        }
        result.add(email);
      }
    }
    return result;
  }

  private List<PhoneNumber> getMultiValuedAsPhoneNumbers(Entry entry, String ldapAttrName) throws LdapInvalidAttributeValueException {
    List<PhoneNumber> result = new ArrayList<>();
    if (ldapAttrName == null) {
      return result;
    }
    Attribute attr = entry.get(ldapAttrName);
    if (attr != null) {
      // Read stored type metadata if available
      String[] types = null;
      String typeStr = getStringAttribute(entry, "scimPhoneTypes");
      if (typeStr != null) {
        types = typeStr.split(",", -1);
      }

      boolean first = true;
      int idx = 0;
      for (Value val : attr) {
        try {
          PhoneNumber phone = new PhoneNumber();
          phone.setValue(val.getString());
          if (types != null && idx < types.length && !types[idx].isEmpty()) {
            phone.setType(types[idx]);
          }
          if (first) {
            phone.setPrimary(true);
            first = false;
          }
          result.add(phone);
        } catch (Exception e) {
          LOG.warn("Skipping unparseable phone number: {}", val.getString(), e);
        }
        idx++;
      }
    }
    return result;
  }
}
