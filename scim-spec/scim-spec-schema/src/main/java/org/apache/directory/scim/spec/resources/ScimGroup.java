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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.schema.Meta;

@ScimResourceType(id = ScimGroup.RESOURCE_NAME, name = ScimGroup.RESOURCE_NAME, schema = ScimGroup.SCHEMA_URI, description = "Top level ScimGroup", endpoint = "/Groups")
@XmlRootElement(name = ScimGroup.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class ScimGroup extends ScimResource implements Serializable {

  private static final long serialVersionUID = 4424638498347469070L;
  public static final String RESOURCE_NAME = "Group";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Group";

  @XmlElement
  @ScimAttribute(description="A human-readable name for the Group.", required=true)
  String displayName;
  
  @XmlElement
  @ScimAttribute(description = "A list of members of the Group.")
  List<GroupMembership> members;

  public ScimGroup addMember(GroupMembership groupMembership) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(groupMembership);

    return this;
  }

  public ScimGroup() {
    super(SCHEMA_URI, RESOURCE_NAME);
  }

  @Override
  public ScimGroup setSchemas(Set<String> schemas) {
    return (ScimGroup) super.setSchemas(schemas);
  }

  @Override
  public ScimGroup setMeta(@NotNull Meta meta) {
    return (ScimGroup) super.setMeta(meta);
  }

  @Override
  public ScimGroup setId(@Size(min = 1) String id) {
    return (ScimGroup) super.setId(id);
  }

  @Override
  public ScimGroup setExternalId(String externalId) {
    return (ScimGroup) super.setExternalId(externalId);
  }

  @Override
  public ScimGroup setExtensions(Map<String, ScimExtension> extensions) {
    return (ScimGroup) super.setExtensions(extensions);
  }

  @Override
  public ScimGroup addSchema(String urn) {
    return (ScimGroup) super.addSchema(urn);
  }

  @Override
  public ScimGroup addExtension(ScimExtension extension) {
    return (ScimGroup) super.addExtension(extension);
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public ScimGroup setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public List<GroupMembership> getMembers() {
    return this.members;
  }

  public ScimGroup setMembers(List<GroupMembership> members) {
    this.members = members;
    return this;
  }

  public String toString() {
    return "ScimGroup(displayName=" + this.getDisplayName() + ", members=" + this.getMembers() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ScimGroup)) return false;
    final ScimGroup other = (ScimGroup) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$displayName = this.getDisplayName();
    final Object other$displayName = other.getDisplayName();
    if (this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName))
      return false;
    final Object this$members = this.getMembers();
    final Object other$members = other.getMembers();
    if (this$members == null ? other$members != null : !this$members.equals(other$members)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ScimGroup;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $displayName = this.getDisplayName();
    result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
    final Object $members = this.getMembers();
    result = result * PRIME + ($members == null ? 43 : $members.hashCode());
    return result;
  }
}
