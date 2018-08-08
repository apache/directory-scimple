package org.apache.directory.scim.example.memory;

import io.restassured.RestAssured;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

@RunWith(Arquillian.class)
public class BasicIT {

  @ArquillianResource
  private URL url;

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    return Deployments.projectWar();
  }

  @Test
  public void foo() {
    System.out.println("wtf: " + url);

    System.out.println(RestAssured.given().log().everything().get(url + "/v2/Users").asString());


  }
}
