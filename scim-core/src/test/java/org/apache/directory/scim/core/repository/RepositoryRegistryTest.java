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

import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RepositoryRegistryTest {

  @Test
  public void initializeWithException() throws InvalidRepositoryException {
    SchemaRegistry schemaRegistry = new SchemaRegistry();
    Repository<ScimUser> repository = mock(Repository.class);

    doThrow(new InvalidRepositoryException("test exception")).when(repository).getExtensionList();

    assertThrows(ScimResourceInvalidException.class, () -> new RepositoryRegistry(schemaRegistry).registerRepositories(List.of(repository)));
  }

  @Test
  public void registerRepository() throws InvalidRepositoryException, ResourceException {
    SchemaRegistry schemaRegistry = spy(new SchemaRegistry());
    Repository<StubResource> repository = mock(Repository.class);
    RepositoryRegistry repositoryRegistry = spy(new RepositoryRegistry(schemaRegistry));

    when(repository.getExtensionList()).thenReturn(List.of(StubExtension.class));

    repositoryRegistry.registerRepository(StubResource.class, repository);
    assertThat(schemaRegistry.getExtensionClass(StubResource.class, StubExtension.URN)).isNotNull();

    assertThat(repositoryRegistry.getRepository(StubResource.class)).isEqualTo(repository);
  }

  @ScimResourceType(id = StubResource.NAME, endpoint = "/Stub", schema = StubResource.URN)
  static class StubResource extends ScimResource {

    final static String URN = "urn:test:stub";
    final static String NAME = "Stub";

    public StubResource() {
      super(URN, NAME);
    }
  }

  @ScimExtensionType(name = StubExtension.NAME, id = StubExtension.URN)
  static class StubExtension implements ScimExtension {
    final static String URN = "urn:test:stub:extension";
    final static String NAME = "StubExtension";

    @Override
    public String getUrn() {
      return URN;
    }
  }
}
