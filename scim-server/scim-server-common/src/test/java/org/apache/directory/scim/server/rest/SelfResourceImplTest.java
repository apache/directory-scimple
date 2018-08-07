package org.apache.directory.scim.server.rest;

import org.apache.directory.scim.server.exception.UnableToResolveIdException;
import org.apache.directory.scim.server.provider.SelfIdResolver;
import org.apache.directory.scim.spec.protocol.UserResource;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.junit.Test;

import javax.ejb.SessionContext;
import javax.enterprise.inject.Instance;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SelfResourceImplTest {

  @Test
  public void noSelfIdResolverTest() {

    Principal principal = mock(Principal.class);
    SessionContext sessionContext = mock(SessionContext.class);
    Instance selfIdResolverInstance = mock(Instance.class);

    when(sessionContext.getCallerPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-user");
    when(selfIdResolverInstance.isUnsatisfied()).thenReturn(true);

    SelfResourceImpl selfResource = new SelfResourceImpl();
    selfResource.selfIdResolver = selfIdResolverInstance;
    selfResource.sessionContext = sessionContext;

    Response response = selfResource.getSelf(null, null);
    assertThat(response.getEntity(), instanceOf(ErrorResponse.class));
    List<String> messages = ((ErrorResponse)response.getEntity()).getErrorMessageList();
    assertThat(messages, hasItem("Caller SelfIdResolver not available"));
    assertThat(messages, hasSize(1));
  }

  @Test
  public void withSelfIdResolverTest() throws UnableToResolveIdException, ScimException {

    String internalId = "test-user-resolved";
    Principal principal = mock(Principal.class);
    SessionContext sessionContext = mock(SessionContext.class);
    Instance selfIdResolverInstance = mock(Instance.class);
    SelfIdResolver selfIdResolver = mock(SelfIdResolver.class);
    UserResource userResource = mock(UserResource.class);
    Response mockResponse = mock(Response.class);

    when(sessionContext.getCallerPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-user");
    when(selfIdResolverInstance.isUnsatisfied()).thenReturn(false);
    when(selfIdResolverInstance.get()).thenReturn(selfIdResolver);
    when(selfIdResolver.resolveToInternalId(principal)).thenReturn(internalId);
    when(userResource.getById(internalId, null, null)).thenReturn(mockResponse);

    SelfResourceImpl selfResource = new SelfResourceImpl();
    selfResource.selfIdResolver = selfIdResolverInstance;
    selfResource.sessionContext = sessionContext;
    selfResource.userResource = userResource;

    // the response is just a passed along from the UserResource, so just validate it is the same instance.
    assertThat(selfResource.getSelf(null, null), sameInstance(mockResponse));
  }
}
