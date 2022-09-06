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

import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;

@ExtendWith(EmbeddedServerExtension.class)
public class ServiceProviderConfigIT extends ScimpleITSupport {

  @Test
  @DisplayName("ServiceProviderConfig endpoint")
  public void config() {
    get("/ServiceProviderConfig")
      .statusCode(200)
      .body(
        "schemas", hasItem("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
        "patch.supported", isBoolean(),
        "bulk.supported", isBoolean(),
        "bulk.maxOperations", isNumber(),
        "bulk.maxPayloadSize", isNumber(),
        "filter.supported", isBoolean(),
        "filter.maxResults", isNumber(),
        "changePassword.supported", isBoolean(),
        "sort.supported", isBoolean(),
        "etag.supported", isBoolean(),
        "authenticationSchemes", not(empty()),
        "authenticationSchemes[0].type", not(emptyString()),
        "authenticationSchemes[0].description", not(emptyString())
      );
  }

  @Test
  @DisplayName("Check if ServiceProviderConfig is read-only")
  public void endpoingIsReadOnly() {
    post("/ServiceProviderConfig", "")
      .statusCode(405);
  }
}
