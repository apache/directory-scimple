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
package org.apache.directory.scim.server.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.core.SecurityContext;
import org.apache.directory.scim.server.exception.UnableToResolveIdResourceException;
import org.apache.directory.scim.server.provider.SelfIdResolver;
import org.apache.directory.scim.spec.protocol.UserResource;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.junit.jupiter.api.Test;

public class SelfResourceImplTest {

  @Test
  public void noSelfIdResolverTest() {

    Principal principal = mock(Principal.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    @SuppressWarnings("unchecked")
    Instance<SelfIdResolver> selfIdResolverInstance = mock(Instance.class);

    when(securityContext.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-user");
    when(selfIdResolverInstance.isUnsatisfied()).thenReturn(true);

    SelfResourceImpl selfResource = new SelfResourceImpl(null, selfIdResolverInstance, new RequestContext().setSecurityContext(securityContext));

    Response response = selfResource.getSelf(null, null);
    assertThat(response.getEntity(), instanceOf(ErrorResponse.class));
    List<String> messages = ((ErrorResponse)response.getEntity()).getErrorMessageList();
    assertThat(messages, hasItem("Caller SelfIdResolver not available"));
    assertThat(messages, hasSize(1));
  }

  @Test
  public void withSelfIdResolverTest() throws UnableToResolveIdResourceException, ScimException {

    String internalId = "test-user-resolved";
    Principal principal = mock(Principal.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    @SuppressWarnings("unchecked")
	Instance<SelfIdResolver> selfIdResolverInstance = mock(Instance.class);
    SelfIdResolver selfIdResolver = mock(SelfIdResolver.class);
    UserResource userResource = mock(UserResource.class);
    Response mockResponse = mock(Response.class);

    when(securityContext.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-user");
    when(selfIdResolverInstance.isUnsatisfied()).thenReturn(false);
    when(selfIdResolverInstance.get()).thenReturn(selfIdResolver);
    when(selfIdResolver.resolveToInternalId(principal)).thenReturn(internalId);
    when(userResource.getById(internalId, null, null)).thenReturn(mockResponse);

    SelfResourceImpl selfResource = new SelfResourceImpl(userResource, selfIdResolverInstance, new RequestContext().setSecurityContext(securityContext));

    // the response is just a passed along from the UserResource, so just validate it is the same instance.
    assertThat(selfResource.getSelf(null, null), sameInstance(mockResponse));
  }
}
