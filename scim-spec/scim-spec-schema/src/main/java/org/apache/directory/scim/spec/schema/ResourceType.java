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

package org.apache.directory.scim.spec.schema;

import java.util.List;

import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.resources.ScimResourceWithOptionalId;
import org.apache.directory.scim.spec.validator.Urn;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SCIM ResourceType
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7643#section-6">ResourceType Schema</a>
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceType extends ScimResourceWithOptionalId {
  
  public static final String RESOURCE_NAME = "ResourceType";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

  @Data
  public static class SchemaExtentionConfiguration {

    @XmlElement(name = "schema")
    @Urn
    @Size(min = 1)
    String schemaUrn;

    @XmlElement
    boolean required;
  }

  @XmlElement
  @Size(min = 1)
  String name;

  @XmlElement
  String description;

  @XmlElement
  @Size(min = 1)
  String endpoint;

  @XmlElement(name = "schema")
  @Urn
  @Size(min = 1)
  String schemaUrn;

  @XmlElement
  List<SchemaExtentionConfiguration> schemaExtensions;
  
  public ResourceType() {
    super(SCHEMA_URI, RESOURCE_NAME);
  }
  
  public ResourceType(ScimResourceType annotation) {
    super(SCHEMA_URI, RESOURCE_NAME);
    this.name = annotation.name();
    this.description = annotation.description();
    this.schemaUrn = annotation.schema();
    this.endpoint = annotation.endpoint();
  }
}
