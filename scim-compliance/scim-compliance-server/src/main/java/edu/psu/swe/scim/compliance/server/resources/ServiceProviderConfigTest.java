package edu.psu.swe.scim.compliance.server.resources;

import static com.eclipsesource.restfuse.Assert.assertOk;

import org.junit.Rule;
import org.junit.Test;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;

import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration;

public class ServiceProviderConfigTest {

  @Rule
  public Destination destination = new Destination(this, "https://acceptance.apps.psu.edu/tier/v2"); 

  @Context
  private Response response;
  
  @Test
  @HttpTest(method = Method.GET, path = "/ServiceProviderConfig")
  public void testGetToServiceProviderConfigForRootReturnsOkWithListResponse() {
    assertOk(response);
    System.out.println(response.getBody());
//    ServiceProviderConfiguration config = (ServiceProviderConfiguration) response.getBody(ServiceProviderConfiguration.class);
  }
  
}
