package org.apache.directory.scim.server.it;

import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.apache.directory.scim.compliance.tests.ScimpleITSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;

@ExtendWith(EmbeddedServerExtension.class)
public class CustomExtensionIT extends ScimpleITSupport {

  @Test
  public void extensionDataTest() {

    String body = "{" +
      "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
      "\"userName\":\"test@example.com\"," +
      "\"name\":{" +
        "\"givenName\":\"Tester\"," +
        "\"familyName\":\"McTest\"}," +
      "\"emails\":[{" +
        "\"primary\":true," +
        "\"value\":\"test@example.com\"," +
        "\"type\":\"work\"}]," +
      "\"displayName\":\"Tester McTest\"," +
      "\"active\":true," +
      "\"urn:mem:params:scim:schemas:extension:LuckyNumberExtension\": {" +
        "\"luckyNumber\": \"1234\"}" + // This value can be a number or string, but will always be a number in the body
      "}";

    post("/Users", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User", "urn:mem:params:scim:schemas:extension:LuckyNumberExtension"),
        "active", is(true),
        "id", not(emptyString()),
        "'urn:mem:params:scim:schemas:extension:LuckyNumberExtension'", notNullValue(),
        "'urn:mem:params:scim:schemas:extension:LuckyNumberExtension'.luckyNumber", is(1234)
      );
  }
}
