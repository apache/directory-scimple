package org.apache.directory.scim.server.provider;

import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.filter.FilterResponse;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.PageRequest;
import org.apache.directory.scim.spec.protocol.search.SortRequest;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class ProviderTest {

  @Test
  public void handleException_jaxrsExceptionTest() {

    Exception e = new WebApplicationException();
    catchException(new ProviderAdapter()).handleException(e);
    assertThat(caughtException(), sameInstance(e));
  }

  @Test
  public void handleException_runtimeExceptionTest() {

    Exception e = new RuntimeException("fake test exception");
    Response response = new ProviderAdapter().handleException(e);
    assertThat(response.getStatus(), is(500));
    assertThat(((ErrorResponse)response.getEntity()).getDetail(), is("fake test exception"));
  }

  @Test
  public void handleException_nullExceptionTest() {

    Response response = new ProviderAdapter().handleException(null);
    assertThat(response.getStatus(), is(500));
    assertThat(((ErrorResponse)response.getEntity()).getDetail(), is("Unknown Server Error"));
  }

  private class ProviderAdapter implements Provider {

    @Override
    public ScimResource create(ScimResource resource) {
      return null;
    }

    @Override
    public ScimResource update(UpdateRequest updateRequest) {
      return null;
    }

    @Override
    public ScimResource get(String id) {
      return null;
    }

    @Override
    public FilterResponse find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
      return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public List<Class<? extends ScimExtension>> getExtensionList() {
      return null;
    }
  }
}
