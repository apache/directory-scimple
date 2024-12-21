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

package org.apache.directory.scim.compliance.tests;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ExtendWith(EmbeddedServerExtension.class)
public class UsersIT extends ScimpleITSupport {

  private final String givenName = randomName("Given-");
  private final String familyName = randomName("Family-");
  private final String displayName = givenName + " " + familyName;
  private final String email = givenName + "." + familyName + "@example.com";

  @Test
  @DisplayName("Test Users endpoint")
  public void userEndpoint() {
    get("/Users", Map.of("count", "1","startIndex", "1"))
      .statusCode(200)
      .body(
        "Resources", not(empty()),
        "schemas", hasItem(SCHEMA_LIST_RESPONSE),
        "itemsPerPage", isNumber(),
        "startIndex", isNumber(),
        "totalResults", isNumber(),
        "Resources[0].id", not(emptyString()),
        "Resources[0].name.familyName", not(emptyString()),
        "Resources[0].userName", not(emptyString()),
        "Resources[0].active", isBoolean(),
        "Resources[0].name.familyName", not(emptyString()),
        "Resources[0].emails[0].value", not(emptyString())
      );
  }

  @Test
  @DisplayName("Test invalid User by username")
  public void invalidUserNameFilter() {
    String invalidUserName = RandomStringUtils.randomAlphanumeric(10);

    get("/Users", Map.of("filter", "userName eq \"" + invalidUserName + "\""))
      .statusCode(200)
      .body(
        "schemas", hasItem(SCHEMA_LIST_RESPONSE),
        "totalResults", is(0)
      );
  }

  @Test
  @DisplayName("Test invalid User by ID")
  public void invalidUserId() {
    String invalidId = RandomStringUtils.randomAlphanumeric(10);

    get("/Users/" + invalidId)
      .statusCode(404)
      .body(
        "schemas", hasItem(SCHEMA_ERROR_RESPONSE),
        "detail", not(emptyString())
      );
  }

  @Test
  @DisplayName("Create user with realistic values")
  @Order(10)
  public void createUser() {

    String body = "{" +
        "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
        "\"userName\":\"" + email + "\"," +
        "\"name\":{" +
          "\"givenName\":\"" + givenName + "\"," +
          "\"familyName\":\"" + familyName + "\"}," +
        "\"emails\":[{" +
          "\"primary\":true," +
          "\"value\":\"" + email + "\"," +
          "\"type\":\"work\"}]," +
        "\"displayName\":\"" + displayName + "\"," +
        "\"active\":true" +
        "}";

    ValidatableResponse response = post("/Users", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString()),
        "name.givenName", is(givenName),
        "name.familyName", is(familyName),
        "userName", equalToIgnoringCase(email)
      );

    String id = response.extract().jsonPath().get("id");
    response.header("Location", matchesRegex(".*/Users/" + id));

    // retrieve the user by id
    get("/Users/" + id)
      .statusCode(200)
      .header("Location", matchesRegex(".*/Users/" + id))
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString()),
        "name.givenName", is(givenName),
        "name.familyName", is(familyName),
        "userName", equalToIgnoringCase(email)
      );

    // posting same content again should return a conflict (409)
    post("/Users", body)
      .statusCode(409)
      .body(
        "schemas", hasItem(SCHEMA_ERROR_RESPONSE),
        "detail", not(emptyString())
      );
  }

  @Test
  @DisplayName("Update User")
  public void updateUser() {

    String body = "{" +
      "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
      "\"userName\":\"updateUser@example.com\"," +
      "\"name\":{" +
        "\"givenName\":\"Given-updateUser\"," +
        "\"familyName\":\"Family-updateUser\"}," +
      "\"emails\":[{" +
        "\"primary\":true," +
        "\"value\":\"updateUser@example.com\"," +
        "\"type\":\"work\"}]," +
      "\"displayName\":\"Given-updateUser Family-updateUser\"," +
      "\"active\":true" +
      "}";

    String id = post("/Users", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString())
      )
      .extract().jsonPath().get("id");

    String updatedBody = body.replaceFirst("}$",
      ",\"phoneNumbers\": [{\"value\": \"555-555-5555\",\"type\": \"work\"}]}");

    put("/Users/" + id, updatedBody)
      .statusCode(200)
      .header("Location", matchesRegex(".*/Users/" + id))
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString()),
        "name.givenName", is("Given-updateUser"),
        "name.familyName", is("Family-updateUser"),
        "userName", equalToIgnoringCase("updateUser@example.com"),
        "phoneNumbers[0].value", is("555-555-5555"),
        "phoneNumbers[0].type", is("work")
      );
  }

  @Test
  @DisplayName("Username Case Sensitivity Check")
  public void userNameByFilter() {
    String userName = get("/Users", Map.of("count", "1","startIndex", "1"))
      .extract().jsonPath().get("Resources[0].userName");

    get("/Users", Map.of("filter", "userName eq \"" + userName + "\""))
      .statusCode(200)
      .contentType(SCIM_MEDIA_TYPE)
      .body(
        "schemas", contains(SCHEMA_LIST_RESPONSE),
        "totalResults", is(1)
      );

    get("/Users", Map.of("filter", "userName eq \"" + userName.toUpperCase() + "\""))
      .statusCode(200)
      .body(
        "schemas", contains(SCHEMA_LIST_RESPONSE),
        "totalResults", is(1)
      );
  }

  @Test
  @DisplayName("Deactivate user with PATCH")
  public void deactivateWithPatch() {
    String body = "{" +
      "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
      "\"userName\":\"deactivateWithPatch@example.com\"," +
      "\"name\":{" +
        "\"givenName\":\"Given-deactivateWithPatch\"," +
        "\"familyName\":\"Family-deactivateWithPatch\"}," +
      "\"emails\":[{" +
        "\"primary\":true," +
        "\"value\":\"deactivateWithPatch@example.com\"," +
        "\"type\":\"work\"}]," +
      "\"displayName\":\"Given-deactivateWithPatch Family-deactivateWithPatch\"," +
      "\"active\":true" +
      "}";

    String id = post("/Users", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString())
      )
      .extract().jsonPath().get("id");

    String patchBody = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
      "\"Operations\": [{" +
        "\"op\": \"replace\"," +
        "\"value\": {" +
          "\"active\": false" +
        "}}]}";

    patch("/Users/" + id, patchBody)
      .statusCode(200)
      .header("Location", matchesRegex(".*/Users/" + id))
      .body(
        "active", is(false)
      );
  }

  @Test
  @DisplayName("Update Invalid User")
  void updateInvalidUser() {
    String id = randomName("user-invalidGroup-id");

    String updatedBody = "{" +
      "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
      "\"active\":true" +
      "}";

    put("/Users/" + id, updatedBody)
      .statusCode(404)
      .body(
        "schemas", contains("urn:ietf:params:scim:api:messages:2.0:Error"),
        "status", is("404"),
        "detail", is("Resource " + id + " not found.")
      );
  }

  @Test
  @DisplayName("Delete Invalid Users")
  void deleteInvalidUser() {
    String id = randomName("user-invalidGroup-id");

    delete("/Users/" + id)
      .statusCode(404)
      .body(
        "schemas", contains("urn:ietf:params:scim:api:messages:2.0:Error"),
        "status", is("404"),
        "detail", is("Resource " + id + " not found.")
      );
  }
}
