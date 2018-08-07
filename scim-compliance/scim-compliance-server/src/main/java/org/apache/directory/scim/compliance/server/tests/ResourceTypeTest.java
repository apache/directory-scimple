package org.apache.directory.scim.compliance.server.tests;

import io.restassured.path.json.JsonPath;
import org.apache.directory.scim.compliance.server.configuration.ScimTestCategories;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResourceTypeTest extends ScimTestSupport {

  public static final String RESOURCE_PATH = "/ResourceTypes";

  @Test
  public void getResourceTypes() {
    JsonPath jsonPath = jsonGet(RESOURCE_PATH);
    int totalResults = jsonPath.getInt("totalResults");
    assertThat(jsonPath.getList("Resources.id"), hasSize(totalResults));
  }

  @Test
  @Category(ScimTestCategories.User.class)
  public void userResourceFromRoot() {
    validateResourceFromRoot(ScimUser.RESOURCE_NAME, ScimUser.SCHEMA_URI);
  }

  @Test
  @Category(ScimTestCategories.User.class)
  public void userResource() {
    validateResource(ScimUser.RESOURCE_NAME, ScimUser.SCHEMA_URI);
  }

  @Test
  @Category(ScimTestCategories.Group.class)
  public void groupResourceFromRoot() {
    validateResourceFromRoot(ScimGroup.RESOURCE_NAME, ScimGroup.SCHEMA_URI);
  }

  @Test
  @Category(ScimTestCategories.Group.class)
  public void groupResource() {
    validateResource(ScimGroup.RESOURCE_NAME, ScimGroup.SCHEMA_URI);
  }

  @Test
  public void postNotAllowed() {
    validateNotAllowed(given().post(RESOURCE_PATH));
  }

  @Test
  public void putNotAllowed() {
    validateNotAllowed(given().put(RESOURCE_PATH));
  }

  @Test
  @Category(ScimTestCategories.User.class)
  public void deleteUserNotAllowed() {
    validateNotAllowed(given().delete(RESOURCE_PATH + "/User"));
  }

  @Test
  @Category(ScimTestCategories.User.class)
  public void patchUserNotAllowed() {
    validateNotAllowed(given().patch(RESOURCE_PATH + "/User"));
  }

  protected void validateResourceFromRoot(String expectedId, String expectedSchema) {
    String body = basicGet(RESOURCE_PATH);

    String actualSchema = JsonPath.from(body)
        .param("resourceId", expectedId)
        .get("Resources.find { it -> it.id == resourceId}.schema");
    assertThat(actualSchema, is(expectedSchema));
  }

  protected void validateResource(String expectedId, String expectedSchema) {
    String body = basicGet(RESOURCE_PATH + "/" + expectedId);

    JsonPath jsonPath = JsonPath.from(body);
    assertThat(jsonPath.get("id"), is(expectedId));
    assertThat(jsonPath.get("schema"), is(expectedSchema));
  }
}
