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
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.emptyString;

@ExtendWith(EmbeddedServerExtension.class)
public class GroupsIT extends ScimpleITSupport {

  @Test
  @DisplayName("Verify Groups endpoint")
  public void groupsEndpoint() {
    get("/Groups")
      .statusCode(200)
      .body(
        "Resources", not(empty()),
        "schemas", hasItem(SCHEMA_LIST_RESPONSE),
        "itemsPerPage", isNumber(),
        "startIndex", isNumber(),
        "totalResults", isNumber(),
        "Resources[0].id", not(emptyString())
      );
  }

  @Test
  @Order(10)
  @DisplayName("Create group with member")
  public void createGroup() {

    String email = randomEmail("createGroup");
    String userId = createUser(randomName("createGroupTest"), email);
    String groupName = randomName("group-createGroup");
    String body = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": [{" +
        "\"value\": \"" + userId + "\"," +
        "\"display\": \"" + email + "\"" +
      "}]}";

    String id = post("/Groups", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "id", not(emptyString()),
        "displayName", is(groupName),
        "members[0].value", is(userId),
        "members[0].display", is(email)
      )
      .extract().jsonPath().get("id");

    // retrieve the group by id
    get("/Groups/" + id)
      .statusCode(200)
      .header("Location", matchesRegex(".*/Groups/" + id))
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "id", not(emptyString()),
        "displayName", is(groupName),
        "members[0].value", is(userId),
        "members[0].display", is(email)
      );

    // posting same content again should return a conflict (409)
    post("/Groups", body)
      .statusCode(409)
      .body(
        "schemas", hasItem(SCHEMA_ERROR_RESPONSE),
        "detail", not(emptyString())
      );
  }

  @Test
  @DisplayName("Test invalid Group by ID")
  public void invalidUserId() {
    String invalidId = randomName("invalidUserId");

    get("/Groups/" + invalidId)
      .statusCode(404)
      .body(
        "schemas", hasItem(SCHEMA_ERROR_RESPONSE),
        "detail", not(emptyString())
      );
  }

  @Test
  @DisplayName("Delete Group")
  public void deleteGroup() {

    String groupName = randomName("group-deleteGroup");
    String body = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": []}";

    String id = post("/Groups", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "id", not(emptyString())
      )
      .extract().jsonPath().get("id");

    delete("/Groups/" + id)
      .statusCode(204);
  }

  @Test
  @DisplayName("Update Group")
  public void updateGroup() {
    String email = randomEmail("updateGroup");
    String userId = createUser(randomName("updateGroupTest"), email);
    String groupName = randomName("group-updateGroup");
    String body = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": [{" +
        "\"value\": \"" + userId + "\"," +
        "\"display\": \"" + email + "\"" +
      "}]}";

    String updatedBody = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": []}";

    String id = post("/Groups", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "id", not(emptyString()),
        "displayName", is(groupName),
        "members[0].value", is(userId),
        "members[0].display", is(email)
      )
      .extract().jsonPath().get("id");

    // update Group,
    put("/Groups/" + id, updatedBody)
      .header("Location", matchesRegex(".*/Groups/" + id))
      .statusCode(200)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "members", empty()
      );
  }

  @Test
  @DisplayName("Update Group with PATCH")
  public void updateGroupWithPatch() {
    String email = randomEmail("updateGroupWithPatch");
    String userId = createUser(randomName("updateGroupWithPatchTest"), email);
    String groupName = randomName("group-updateGroupWithPatch");
    String body = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": [{" +
      "\"value\": \"" + userId + "\"," +
      "\"display\": \"" + email + "\"" +
      "}]}";

    String patchBody = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
      "\"Operations\": [{" +
        "\"op\": \"remove\"," +
        "\"path\": \"members[value eq \\\"" + userId + "\\\"]\"" +
      "}]}";

    ValidatableResponse response = post("/Groups", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "id", not(emptyString()),
        "displayName", is(groupName),
        "members[0].value", is(userId),
        "members[0].display", is(email)
      );
    String id = response.extract().jsonPath().get("id");
    response.header("Location", matchesRegex(".*/Groups/" + id));

    // update Group,
    patch("/Groups/" + id, patchBody)
      .statusCode(200)
      .header("Location", matchesRegex(".*/Groups/" + id))
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:Group"),
        "members", empty()
      );
  }

  @Test
  @DisplayName("Update Invalid Group")
  void updateInvalidGroup() {

    String id = randomName("group-invalidGroup-id");
    String groupName = randomName("group-invalidGroup");

    String updatedBody = "{" +
      "\"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"]," +
      "\"displayName\": \"" + groupName + "\"," +
      "\"members\": []}";

    put("/Groups/" + id, updatedBody)
      .statusCode(404)
      .body(
        "schemas", contains("urn:ietf:params:scim:api:messages:2.0:Error"),
        "status", is("404"),
        "detail", is("Resource " + id + " not found.")
      );
  }

  @Test
  @DisplayName("Delete Invalid Group")
  void deleteInvalidGroup() {
    String id = randomName("group-invalidGroup-id");

    delete("/Groups/" + id)
      .statusCode(404)
      .body(
        "schemas", contains("urn:ietf:params:scim:api:messages:2.0:Error"),
        "status", is("404"),
        "detail", is("Resource " + id + " not found.")
      );
  }

  String createUser(String name, String email) {
    String body = "{" +
      "\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
      "\"userName\":\"" + email + "\"," +
      "\"name\":{" +
        "\"givenName\":\"Given-" + name + "\"," +
        "\"familyName\":\"Family-" + name + "\"}," +
      "\"emails\":[{" +
        "\"primary\":true," +
        "\"value\":\"" + email + "\"," +
        "\"type\":\"work\"}]," +
      "\"displayName\":\"Given-" + name + " Family-" + name + "\"," +
      "\"active\":true" +
      "}";

    return post("/Users", body)
      .statusCode(201)
      .body(
        "schemas", contains("urn:ietf:params:scim:schemas:core:2.0:User"),
        "active", is(true),
        "id", not(emptyString())
      )
      .extract().jsonPath().get("id");
  }
}
