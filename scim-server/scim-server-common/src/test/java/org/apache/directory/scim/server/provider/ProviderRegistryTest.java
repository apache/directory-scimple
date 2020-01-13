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

package org.apache.directory.scim.server.provider;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Instance;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProviderRegistryTest {

  
  Registry registry;
  
  @Mock
  Instance<Provider<ScimUser>> providerInstance;
  
  @Mock
  Provider<ScimUser> provider;
  
  ProviderRegistry providerRegistry;
  
  public ProviderRegistryTest() {
    providerRegistry = new ProviderRegistry();
    registry = new Registry();
    providerRegistry.registry = registry;
  }
  
  @BeforeEach
  public void initialize() {
    Mockito.when(providerInstance.get()).thenReturn(provider);
//  Mockito.when(provider.getExtensionList()).thenReturn(Collections.singletonList(Enterprise));
  }
  
  @Test
  public void testAddProvider() throws Exception {
    providerRegistry.registerProvider(ScimUser.class, providerInstance);
    
    Schema schema = registry.getSchema(ScimUser.SCHEMA_URI);
    
    assertThat(schema).isNotNull();
    assertThat(schema.getId()).isEqualTo(ScimUser.SCHEMA_URI);
  }

}
