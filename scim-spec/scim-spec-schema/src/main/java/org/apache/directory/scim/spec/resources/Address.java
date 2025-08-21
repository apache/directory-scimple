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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.annotation.ScimAttribute;

/**
 * Scim core schema, <a href="https://tools.ietf.org/html/rfc7643#section-4.1.2">section 4.1.2</a>
 *
 */
@XmlType(name = "address")
@XmlAccessorType(XmlAccessType.NONE)
public class Address implements Serializable, TypedAttribute {

  private static final long serialVersionUID = 3579689988186914163L;
  
  @XmlElement
  @ScimAttribute(canonicalValueList={"work", "home", "other"}, description="A label indicating the attribute's function; e.g., 'aim', 'gtalk', 'mobile' etc.")
  String type;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;
  
  @XmlElement
  @ScimAttribute(description="A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;
  
  @ScimAttribute(description="The two letter ISO 3166-1 alpha-2 country code")
  @XmlElement
  private String country;
  
  @ScimAttribute(description="The full mailing address, formatted for display or use with a mailing label. This attribute MAY contain newlines.")
  @XmlElement
  private String formatted;
  
  @ScimAttribute(description="The city or locality component.")
  @XmlElement
  private String locality;
  
  @ScimAttribute(description="The zipcode or postal code component.")
  @XmlElement
  private String postalCode;
  
  @ScimAttribute(description="The state or region component.")
  @XmlElement
  private String region;
  
  @ScimAttribute(description="The full street address component, which may include house number, street name, PO BOX, and multi-line extended street address information. This attribute MAY contain newlines.")
  @XmlElement
  private String streetAddress;

  public String getType() {
    return this.type;
  }

  public Address setType(String type) {
    this.type = type;
    return this;
  }

  public String getDisplay() {
    return this.display;
  }

  public Address setDisplay(String display) {
    this.display = display;
    return this;
  }

  public Boolean getPrimary() {
    return this.primary;
  }

  public Address setPrimary(Boolean primary) {
    this.primary = primary;
    return this;
  }

  public String getCountry() {
    return this.country;
  }

  public Address setCountry(String country) {
    this.country = country;
    return this;
  }

  public String getFormatted() {
    return this.formatted;
  }

  public Address setFormatted(String formatted) {
    this.formatted = formatted;
    return this;
  }

  public String getLocality() {
    return this.locality;
  }

  public Address setLocality(String locality) {
    this.locality = locality;
    return this;
  }

  public String getPostalCode() {
    return this.postalCode;
  }

  public Address setPostalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public String getRegion() {
    return this.region;
  }

  public Address setRegion(String region) {
    this.region = region;
    return this;
  }

  public String getStreetAddress() {
    return this.streetAddress;
  }

  public Address setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
    return this;
  }

  public String toString() {
    return "Address(type=" + this.getType() + ", display=" + this.getDisplay() + ", primary=" + this.getPrimary() + ", country=" + this.getCountry() + ", formatted=" + this.getFormatted() + ", locality=" + this.getLocality() + ", postalCode=" + this.getPostalCode() + ", region=" + this.getRegion() + ", streetAddress=" + this.getStreetAddress() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Address)) return false;
    final Address other = (Address) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$display = this.getDisplay();
    final Object other$display = other.getDisplay();
    if (this$display == null ? other$display != null : !this$display.equals(other$display)) return false;
    final Object this$primary = this.getPrimary();
    final Object other$primary = other.getPrimary();
    if (this$primary == null ? other$primary != null : !this$primary.equals(other$primary)) return false;
    final Object this$country = this.getCountry();
    final Object other$country = other.getCountry();
    if (this$country == null ? other$country != null : !this$country.equals(other$country)) return false;
    final Object this$formatted = this.getFormatted();
    final Object other$formatted = other.getFormatted();
    if (this$formatted == null ? other$formatted != null : !this$formatted.equals(other$formatted)) return false;
    final Object this$locality = this.getLocality();
    final Object other$locality = other.getLocality();
    if (this$locality == null ? other$locality != null : !this$locality.equals(other$locality)) return false;
    final Object this$postalCode = this.getPostalCode();
    final Object other$postalCode = other.getPostalCode();
    if (this$postalCode == null ? other$postalCode != null : !this$postalCode.equals(other$postalCode)) return false;
    final Object this$region = this.getRegion();
    final Object other$region = other.getRegion();
    if (this$region == null ? other$region != null : !this$region.equals(other$region)) return false;
    final Object this$streetAddress = this.getStreetAddress();
    final Object other$streetAddress = other.getStreetAddress();
    if (this$streetAddress == null ? other$streetAddress != null : !this$streetAddress.equals(other$streetAddress))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Address;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $display = this.getDisplay();
    result = result * PRIME + ($display == null ? 43 : $display.hashCode());
    final Object $primary = this.getPrimary();
    result = result * PRIME + ($primary == null ? 43 : $primary.hashCode());
    final Object $country = this.getCountry();
    result = result * PRIME + ($country == null ? 43 : $country.hashCode());
    final Object $formatted = this.getFormatted();
    result = result * PRIME + ($formatted == null ? 43 : $formatted.hashCode());
    final Object $locality = this.getLocality();
    result = result * PRIME + ($locality == null ? 43 : $locality.hashCode());
    final Object $postalCode = this.getPostalCode();
    result = result * PRIME + ($postalCode == null ? 43 : $postalCode.hashCode());
    final Object $region = this.getRegion();
    result = result * PRIME + ($region == null ? 43 : $region.hashCode());
    final Object $streetAddress = this.getStreetAddress();
    result = result * PRIME + ($streetAddress == null ? 43 : $streetAddress.hashCode());
    return result;
  }
}
