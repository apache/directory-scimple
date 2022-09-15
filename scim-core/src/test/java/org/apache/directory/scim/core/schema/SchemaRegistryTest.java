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

package org.apache.directory.scim.core.schema;

import org.apache.directory.scim.core.repository.utility.ExampleObjectExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schemas;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaRegistryTest {

  @Test
  public void registerSchema() {

    SchemaRegistry schemaRegistry = new SchemaRegistry();
    Schema userSchema = Schemas.schemaFor(ScimUser.class);
    Schema groupsSchema = Schemas.schemaFor(ScimGroup.class);
    Schema extSchema = Schemas.schemaForExtension(ExampleObjectExtension.class);

    ResourceType userType = new ResourceType();
    userType.setId(ScimUser.RESOURCE_NAME);
    userType.setEndpoint("/Users");
    userType.setSchemaUrn(ScimUser.SCHEMA_URI);
    userType.setName(ScimUser.RESOURCE_NAME);
    userType.setDescription("Top level ScimUser");
    userType.setSchemaExtensions(List.of(new ResourceType.SchemaExtentionConfiguration().setSchemaUrn(ExampleObjectExtension.URN)));

    ResourceType groupType = new ResourceType();
    groupType.setId(ScimGroup.RESOURCE_NAME);
    groupType.setEndpoint("/Groups");
    groupType.setSchemaUrn(ScimGroup.SCHEMA_URI);
    groupType.setName(ScimGroup.RESOURCE_NAME);
    groupType.setDescription("Top level ScimGroup");

    schemaRegistry.addSchema(ScimUser.class, List.of(ExampleObjectExtension.class));
    schemaRegistry.addSchema(ScimGroup.class, null);

    assertThat(schemaRegistry.getSchema(ScimUser.SCHEMA_URI)).isEqualTo(userSchema);
    assertThat(schemaRegistry.getAllSchemas()).containsOnly(userSchema, groupsSchema, extSchema);
    assertThat(schemaRegistry.getAllSchemaUrns()).containsOnly(ScimUser.SCHEMA_URI, ScimGroup.SCHEMA_URI, ExampleObjectExtension.URN);
    assertThat(schemaRegistry.getAllResourceTypes()).containsOnly(userType, groupType);
    assertThat(schemaRegistry.getResourceType(ScimUser.RESOURCE_NAME)).isEqualTo(userType);
    assertThat(schemaRegistry.getScimResourceClassFromEndpoint("/Users")).isEqualTo(ScimUser.class);
    assertThat(schemaRegistry.getScimResourceClassFromEndpoint("/Groups")).isEqualTo(ScimGroup.class);
    assertThat(schemaRegistry.getScimResourceClass(ScimUser.SCHEMA_URI)).isEqualTo(ScimUser.class);
    assertThat(schemaRegistry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).isEqualTo(Schemas.schemaFor(ScimUser.class));
  }
}
