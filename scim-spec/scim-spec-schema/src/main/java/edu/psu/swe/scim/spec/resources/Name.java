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

package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;

@Data
@EqualsAndHashCode(callSuper=false,exclude={"formatted"})
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

}
