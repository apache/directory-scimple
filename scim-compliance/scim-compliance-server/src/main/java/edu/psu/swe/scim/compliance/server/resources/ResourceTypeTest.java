package edu.psu.swe.scim.compliance.server.resources;

import static com.eclipsesource.restfuse.Assert.assertMethodNotAllowed;
import static com.eclipsesource.restfuse.Assert.assertOk;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;

public class ResourceTypeTest {
  
  @Rule
  public Destination destination = new Destination(this, "https://acceptance.apps.psu.edu/tier/v2"); 

  @Context
  private Response response;
  
  @Test
  @HttpTest(method = Method.GET, path = "/ResourceTypes")
  public void testGetToResourceTypeForRootReturnsOkWithListResponse() {
    assertOk(response);
  }
  
  @Test
  @HttpTest(method = Method.GET, path = "/ResourceTypes/User")
  public void testGetToResourceTypeForUserReturnsOkWithUser() {
    assertOk(response);
  }
  
  @Test
  @Ignore
  @HttpTest(method = Method.POST, path = "/ResourceTypes")
  public void testPostToResourceTypeReturnsMethodNotAllowed() {
    assertMethodNotAllowed(response);
  }
  
  @Test
  @Ignore
  @HttpTest(method = Method.PUT, path = "/ResourceTypes")
  public void testPutToResourceTypeReturnsMethodNotAllowed() {
    assertMethodNotAllowed(response);
  }
  
//  @Test
//  @HttpTest(method = Method.PATCH, path = "/ResourceTypes/Users")
//  public void testPatchToResourceTypeReturnsMethodNotAllowed() {
//    assertMethodNotAllowed(response);
//  }
  
  @Test
  @Ignore
  @HttpTest(method = Method.DELETE, path = "/ResourceTypes/Users")
  public void testDeleteToResourceTypeReturnsMethodNotAllowed() {
    assertMethodNotAllowed(response);
  }
  
}
