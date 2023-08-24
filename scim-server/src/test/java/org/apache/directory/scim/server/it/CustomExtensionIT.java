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
