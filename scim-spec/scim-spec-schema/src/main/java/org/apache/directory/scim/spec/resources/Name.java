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

@XmlType(name = "name", propOrder = {
    "formatted",
    "familyName",
    "givenName",
    "middleName",
    "honorificPrefix",
    "honorificSuffix"    
})
@XmlAccessorType(XmlAccessType.NONE)
public class Name implements Serializable  {

  private static final long serialVersionUID = -2761413543859555141L;

  @XmlElement
  @ScimAttribute(description="The full name, including all middle names, titles, and suffixes as appropriate, formatted for display (e.g. Ms. Barbara J Jensen, III.).")
  String formatted;

  @XmlElement
  @ScimAttribute(description="The family name of the User, or Last Name in most Western languages (e.g. Jensen given the full name Ms. Barbara J Jensen, III.).")
  String familyName;

  @XmlElement
  @ScimAttribute(description="The given name of the User, or First Name in most Western languages (e.g. Barbara given the full name Ms. Barbara J Jensen, III.).")
  String givenName;

  @XmlElement
  @ScimAttribute(description="The middle name(s) of the User (e.g. Robert given the full name Ms. Barbara J Jensen, III.).")
  String middleName;

  @XmlElement
  @ScimAttribute(description="The honorific prefix(es) of the User, or Title in most Western languages (e.g. Ms. given the full name Ms. Barbara J Jensen, III.).")
  String honorificPrefix;

  @XmlElement
  @ScimAttribute(description="The honorific suffix(es) of the User, or Suffix in most Western languages (e.g. III. given the full name Ms. Barbara J Jensen, III.).")
  String honorificSuffix;

  public String getFormatted() {
    return this.formatted;
  }

  public Name setFormatted(String formatted) {
    this.formatted = formatted;
    return this;
  }

  public String getFamilyName() {
    return this.familyName;
  }

  public Name setFamilyName(String familyName) {
    this.familyName = familyName;
    return this;
  }

  public String getGivenName() {
    return this.givenName;
  }

  public Name setGivenName(String givenName) {
    this.givenName = givenName;
    return this;
  }

  public String getMiddleName() {
    return this.middleName;
  }

  public Name setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public String getHonorificPrefix() {
    return this.honorificPrefix;
  }

  public String getHonorificSuffix() {
    return this.honorificSuffix;
  }

  public Name setHonorificPrefix(String honorificPrefix) {
    this.honorificPrefix = honorificPrefix;
    return this;
  }

  public Name setHonorificSuffix(String honorificSuffix) {
    this.honorificSuffix = honorificSuffix;
    return this;
  }

  public String toString() {
    return "Name(formatted=" + this.getFormatted() + ", familyName=" + this.getFamilyName() + ", givenName=" + this.getGivenName() + ", middleName=" + this.getMiddleName() + ", honorificPrefix=" + this.getHonorificPrefix() + ", honorificSuffix=" + this.getHonorificSuffix() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Name)) return false;
    final Name other = (Name) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$familyName = this.getFamilyName();
    final Object other$familyName = other.getFamilyName();
    if (this$familyName == null ? other$familyName != null : !this$familyName.equals(other$familyName)) return false;
    final Object this$givenName = this.getGivenName();
    final Object other$givenName = other.getGivenName();
    if (this$givenName == null ? other$givenName != null : !this$givenName.equals(other$givenName)) return false;
    final Object this$middleName = this.getMiddleName();
    final Object other$middleName = other.getMiddleName();
    if (this$middleName == null ? other$middleName != null : !this$middleName.equals(other$middleName)) return false;
    final Object this$honorificPrefix = this.getHonorificPrefix();
    final Object other$honorificPrefix = other.getHonorificPrefix();
    if (this$honorificPrefix == null ? other$honorificPrefix != null : !this$honorificPrefix.equals(other$honorificPrefix))
      return false;
    final Object this$honorificSuffix = this.getHonorificSuffix();
    final Object other$honorificSuffix = other.getHonorificSuffix();
    if (this$honorificSuffix == null ? other$honorificSuffix != null : !this$honorificSuffix.equals(other$honorificSuffix))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Name;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $familyName = this.getFamilyName();
    result = result * PRIME + ($familyName == null ? 43 : $familyName.hashCode());
    final Object $givenName = this.getGivenName();
    result = result * PRIME + ($givenName == null ? 43 : $givenName.hashCode());
    final Object $middleName = this.getMiddleName();
    result = result * PRIME + ($middleName == null ? 43 : $middleName.hashCode());
    final Object $honorificPrefix = this.getHonorificPrefix();
    result = result * PRIME + ($honorificPrefix == null ? 43 : $honorificPrefix.hashCode());
    final Object $honorificSuffix = this.getHonorificSuffix();
    result = result * PRIME + ($honorificSuffix == null ? 43 : $honorificSuffix.hashCode());
    return result;
  }
}
