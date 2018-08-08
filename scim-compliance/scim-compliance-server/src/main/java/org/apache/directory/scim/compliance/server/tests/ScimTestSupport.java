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

package org.apache.directory.scim.compliance.server.tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.compliance.server.configuration.Configuration;
import org.junit.Before;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public abstract class ScimTestSupport {

  protected static final String SCIM_MEDIA_TYPE = "application/scim+json";

  private final Configuration configuration = Configuration.fromEnvironment();

  public ScimTestSupport() {
    // enable logging on failures
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  protected Configuration getConfiguration() {
    return configuration;
  }

  protected String basicGet(String path) {
    return given()
      .when()
        .get(path)
      .then()
        .statusCode(200)
        .contentType(SCIM_MEDIA_TYPE)
        .extract().asString();
  }

  protected JsonPath jsonGet(String path) {
    return JsonPath.from(given()
      .when()
        .get(path)
      .then()
        .statusCode(200)
        .contentType(SCIM_MEDIA_TYPE)
        .extract().asString());
  }

  @Before
  public void configureRestAssuredGlobals() {

    RestAssured.filters((requestSpec, responseSpec, ctx) -> {

      Configuration config = getConfiguration();

      requestSpec.baseUri(config.getBaseUrl())
        .accept(SCIM_MEDIA_TYPE)
        .redirects().follow(false);

      if (requestSpec.getBody() != null) {
        requestSpec.contentType(SCIM_MEDIA_TYPE);
      }

      if (StringUtils.isNotEmpty(config.getAuthHeaderValue())) {
        requestSpec.header("Authorization", config.getAuthHeaderValue());
      }

      return ctx.next(requestSpec, responseSpec);
    });
  }

  protected void validateNotAllowed(Response response) {
    response.then()
        .statusCode(405)
        .contentType(SCIM_MEDIA_TYPE)
        .body("schemas", hasItem("urn:ietf:params:scim:api:messages:2.0:Error"))
        .body("status", is("405"));
  }
}
