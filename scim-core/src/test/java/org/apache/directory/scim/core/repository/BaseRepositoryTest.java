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

import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.exception.ResourceNotFoundException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseRepositoryTest {

  /**
   * Concrete test subclass that delegates abstract methods to mockable lambdas.
   */
  static class TestRepository extends BaseRepository<ScimUser> {

    TestRepository(PatchHandler patchHandler) {
      super(ScimUser.class, patchHandler);
    }

    @SafeVarargs
    TestRepository(PatchHandler patchHandler, Class<? extends ScimExtension>... extensions) {
      super(ScimUser.class, patchHandler, extensions);
    }

    /** No-arg constructor to verify CDI proxy path. */
    TestRepository() {
      super();
    }

    @Override
    public ScimUser create(ScimUser resource, ScimRequestContext requestContext) {
      return null;
    }

    @Override
    public ScimUser update(String id, ScimUser resource, ScimRequestContext requestContext) {
      return resource;
    }

    @Override
    public ScimUser get(String id, ScimRequestContext requestContext) {
      return null;
    }

    @Override
    public FilterResponse<ScimUser> find(Filter filter, ScimRequestContext requestContext) {
      return null;
    }

    @Override
    public void delete(String id) {
    }
  }

  @Test
  void getResourceClass_returnsClassPassedToConstructor() {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = new TestRepository(patchHandler);

    assertThat(repository.getResourceClass()).isEqualTo(ScimUser.class);
  }

  @Test
  void noArgConstructor_doesNotThrow() {
    TestRepository repository = new TestRepository();

    assertThat(repository.getResourceClass()).isNull();
  }

  @Test
  void patch_callsGetThenApplyThenUpdate() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimUser existing = new ScimUser();
    existing.setId("user-1");
    ScimUser patched = new ScimUser();
    patched.setId("user-1-patched");
    ScimUser updated = new ScimUser();
    updated.setId("user-1-updated");

    List<PatchOperation> ops = List.of(new PatchOperation());
    ScimRequestContext ctx = ScimRequestContext.empty();

    when(repository.get("user-1", ctx)).thenReturn(existing);
    when(patchHandler.apply(existing, ops)).thenReturn(patched);
    when(repository.update("user-1", patched, ctx)).thenReturn(updated);

    ScimUser result = repository.patch("user-1", ops, ctx);

    assertThat(result).isSameAs(updated);
    verify(repository).get("user-1", ctx);
    verify(patchHandler).apply(existing, ops);
    verify(repository).update("user-1", patched, ctx);
  }

  @Test
  void patch_throwsResourceNotFoundExceptionWhenGetReturnsNull() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimRequestContext ctx = ScimRequestContext.empty();
    when(repository.get("missing-id", ctx)).thenReturn(null);

    assertThatThrownBy(() -> repository.patch("missing-id", List.of(), ctx))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void patch_passesCorrectIdOperationsAndContext() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimUser existing = new ScimUser();
    ScimUser patched = new ScimUser();
    ScimUser updated = new ScimUser();

    PatchOperation op1 = new PatchOperation();
    PatchOperation op2 = new PatchOperation();
    List<PatchOperation> ops = List.of(op1, op2);
    ScimRequestContext ctx = ScimRequestContext.empty();

    when(repository.get("id-42", ctx)).thenReturn(existing);
    when(patchHandler.apply(existing, ops)).thenReturn(patched);
    when(repository.update("id-42", patched, ctx)).thenReturn(updated);

    repository.patch("id-42", ops, ctx);

    verify(repository).get(eq("id-42"), eq(ctx));
    verify(patchHandler).apply(eq(existing), eq(ops));
    verify(repository).update(eq("id-42"), eq(patched), eq(ctx));
  }

  @Test
  void patch_returnsResultFromUpdate() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimUser existing = new ScimUser();
    ScimUser patched = new ScimUser();
    ScimUser updateResult = new ScimUser();
    updateResult.setId("final-result");

    ScimRequestContext ctx = ScimRequestContext.empty();
    when(repository.get("id-1", ctx)).thenReturn(existing);
    when(patchHandler.apply(any(), any())).thenReturn(patched);
    when(repository.update(any(), any(), any())).thenReturn(updateResult);

    ScimUser result = repository.patch("id-1", List.of(), ctx);

    assertThat(result).isSameAs(updateResult);
    assertThat(result.getId()).isEqualTo("final-result");
  }

  @Test
  void patch_doesNotCallUpdateWhenGetReturnsNull() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimRequestContext ctx = ScimRequestContext.empty();
    when(repository.get("absent", ctx)).thenReturn(null);

    try {
      repository.patch("absent", List.of(), ctx);
    } catch (ResourceNotFoundException e) {
      // expected
    }

    verify(repository, Mockito.never()).update(any(), any(), any());
    verify(patchHandler, Mockito.never()).apply(any(), any());
  }

  @Test
  void patch_doesNotCallApplyWhenGetReturnsNull() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimRequestContext ctx = ScimRequestContext.empty();
    when(repository.get("no-such", ctx)).thenReturn(null);

    try {
      repository.patch("no-such", List.of(), ctx);
    } catch (ResourceNotFoundException e) {
      // expected
    }

    verify(patchHandler, Mockito.never()).apply(any(), any());
  }

  @Test
  void patch_nullPatchHandler_throwsIllegalStateException() {
    TestRepository repository = Mockito.spy(new TestRepository(null));

    assertThatThrownBy(() -> repository.patch("id", List.of(), ScimRequestContext.empty()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("No PatchHandler configured");
  }

  @Test
  void patch_patchHandlerThrowsException_propagates() throws ResourceException {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = Mockito.spy(new TestRepository(patchHandler));

    ScimUser user = new ScimUser();
    user.setId("id1");
    ScimRequestContext ctx = ScimRequestContext.empty();
    when(repository.get("id1", ctx)).thenReturn(user);
    when(patchHandler.apply(any(), any())).thenThrow(new RuntimeException("patch failed"));

    assertThatThrownBy(() -> repository.patch("id1", List.of(), ctx))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("patch failed");
  }

  @Test
  void getExtensionList_defaultsToEmptyList() {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = new TestRepository(patchHandler);

    assertThat(repository.getExtensionList()).isEmpty();
  }

  @Test
  void getExtensionList_returnsProvidedExtensions() {
    PatchHandler patchHandler = Mockito.mock(PatchHandler.class);
    TestRepository repository = new TestRepository(patchHandler, ExtensionA.class, ExtensionB.class);

    assertThat(repository.getExtensionList())
      .containsExactly(ExtensionA.class, ExtensionB.class);
  }

  @Test
  void getExtensionList_noArgConstructor_returnsEmptyList() {
    TestRepository repository = new TestRepository();

    assertThat(repository.getExtensionList()).isEmpty();
  }

  /** Stub extension types for testing. */
  static abstract class ExtensionA implements ScimExtension {}
  static abstract class ExtensionB implements ScimExtension {}
}
