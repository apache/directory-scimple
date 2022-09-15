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

package org.apache.directory.scim.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RepositorySchemaRegistryTest {

  SchemaRegistry schemaRegistry;
  
  @Mock
  Repository<ScimUser> repository;
  
  RepositoryRegistry repositoryRegistry;
  
  public RepositorySchemaRegistryTest() {
    schemaRegistry = new SchemaRegistry();
    repositoryRegistry = new RepositoryRegistry(schemaRegistry, null);
  }

  @Test
  public void testAddRepository() throws Exception {
    repositoryRegistry.registerRepository(ScimUser.class, repository);
    
    Schema schema = schemaRegistry.getSchema(ScimUser.SCHEMA_URI);
    
    assertThat(schema).isNotNull();
    assertThat(schema.getId()).isEqualTo(ScimUser.SCHEMA_URI);
  }
}
