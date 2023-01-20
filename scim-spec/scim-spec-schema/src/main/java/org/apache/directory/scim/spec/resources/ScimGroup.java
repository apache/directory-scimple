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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
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
  List<ResourceReference> members;

  public ScimGroup addMember(ResourceReference resourceReference) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(resourceReference);

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
}
