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

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension.ScimServerUri;
import org.hamcrest.Matcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.instanceOf;

public class ScimpleITSupport {

  static final String SCIM_MEDIA_TYPE = "application/scim+json";
  static final String SCHEMA_LIST_RESPONSE = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
  static final String SCHEMA_ERROR_RESPONSE = "urn:ietf:params:scim:api:messages:2.0:Error";

  @ScimServerUri
  private URI uri = URI.create("http://localhost:8080/v2");

  private final boolean loggingEnabled = Boolean.getBoolean("scim.tests.logging.enabled");

  private final Map<String, String> requestHeaders = Map.of(
    "User-Agent", "Apache SCIMple Compliance Tests",
    "Accept-Charset", "utf-8",
    "Authorization", "TODO"
  );

  protected URI uri() {
    return uri;
  }

  protected URI uri(String path) {
    return uri(path, emptyMap());
  }


  protected URI uri(String path, Map<String, String> query) {
    URI uri = uri();
    String queryString = query.isEmpty() ? null :
      query.keySet()
        .stream().map(key -> key + "=" + query.get(key))
        .collect(Collectors.joining("&"));
    try {
      return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath() + path, queryString, null);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected ValidatableResponse get(String path) {
    return get(path, emptyMap());
  }

  protected ValidatableResponse get(String path, Map<String, String> query) {
    ValidatableResponse responseSpec =
      given()
        .urlEncodingEnabled(false) // URL encoding is handled but the URI
        .redirects().follow(false)
        .accept(SCIM_MEDIA_TYPE)
        .headers(requestHeaders)
      .when()
        .filter(logging(loggingEnabled))
        .get(uri(path, query))
      .then()
        .contentType(SCIM_MEDIA_TYPE);

      if (loggingEnabled) {
        responseSpec.log().everything();
      }
      return responseSpec;
  }

  protected ValidatableResponse post(String path, String body) {
    ValidatableResponse responseSpec =
      given()
        .urlEncodingEnabled(false) // URL encoding is handled but the URI
        .redirects().follow(false)
        .accept(SCIM_MEDIA_TYPE)
        .contentType(SCIM_MEDIA_TYPE)
        .headers(requestHeaders)
      .when()
        .filter(logging(loggingEnabled))
        .body(body)
        .post(uri(path))
      .then()
        .contentType(SCIM_MEDIA_TYPE);

    if (loggingEnabled) {
      responseSpec.log().everything();
    }
    return responseSpec;
  }

  static Filter logging(boolean enabled) {
    return enabled
      ? new RequestLoggingFilter(LogDetail.ALL)
      : new NoOpFilter();
  }

  static Matcher<?> isBoolean() {
    return instanceOf(Boolean.class);
  }

  static Matcher<?> isNumber() {
    return instanceOf(Number.class);
  }

  static class NoOpFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
      return ctx.next(requestSpec, responseSpec);
    }
  }
}
