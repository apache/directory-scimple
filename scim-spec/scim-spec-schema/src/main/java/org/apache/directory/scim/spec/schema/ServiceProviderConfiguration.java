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

package org.apache.directory.scim.spec.schema;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.resources.ScimResourceWithOptionalId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceProviderConfiguration extends ScimResourceWithOptionalId {

  public static final String RESOURCE_NAME = "ServiceProviderConfig";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

  @Data
  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  public static class AuthenticationSchema {

    public enum Type {
      @XmlEnumValue("oauth") OAUTH(
        "oauth",
        "OAuth 1.0",
        "Authentication scheme using the OAuth 1.0 Standard",
        "https://www.rfc-editor.org/rfc/rfc5849.html"),
      @XmlEnumValue("oauth2") OAUTH2(
        "oauth2",
        "OAuth 2.0",
        "Authentication scheme using the OAuth 2.0 Standard",
        "https://www.rfc-editor.org/rfc/rfc6749.html"),
      @XmlEnumValue("oauthbearertoken") OAUTH_BEARER(
        "oauthbearertoken",
        "OAuth Bearer Token",
        "Authentication scheme using the OAuth Bearer Token Standard",
        "http://www.rfc-editor.org/info/rfc6750"),
      @XmlEnumValue("httpbasic") HTTP_BASIC(
        "httpbasic",
        "HTTP Basic",
        "Authentication scheme using the HTTP Basic Standard",
        "http://www.rfc-editor.org/info/rfc2617"),
      @XmlEnumValue("httpdigest") HTTP_DIGEST(
        "httpdigest",
        "HTTP Digest",
        "Authentication scheme using the HTTP Digest Standard",
        "https://www.rfc-editor.org/rfc/rfc7616.html");

      private final String type;
      private final String specUri;
      private final String defaultName;
      private final String defaultDescription;

      Type(String type, String defaultName, String defaultDescription, String specUri) {
        this.type = type;
        this.defaultName = defaultName;
        this.defaultDescription = defaultDescription;
        this.specUri = specUri;
      }

      @Override
      public String toString() {
        return type;
      }
    }

    @XmlElement
    Type type;

    @XmlElement
    String name;

    @XmlElement
    String description;

    @XmlElement
    String specUri;

    @XmlElement
    String documentationUri;

    public static AuthenticationSchema oauth() {
      return fromType(Type.OAUTH);
    }

    public static AuthenticationSchema oauth2() {
      return fromType(Type.OAUTH2);
    }
    public static AuthenticationSchema oauthBearer() {
      return fromType(Type.OAUTH_BEARER);
    }
    public static AuthenticationSchema httpBasic() {
      return fromType(Type.HTTP_BASIC);
    }
    public static AuthenticationSchema httpDigest() {
      return fromType(Type.HTTP_DIGEST);
    }

    private static AuthenticationSchema fromType(Type type) {
      return new ServiceProviderConfiguration.AuthenticationSchema()
        .setType(type)
        .setName(type.defaultName)
        .setDescription(type.defaultDescription)
        .setSpecUri(type.specUri);
    }
  }

  @Data
  public static class SupportedConfiguration {
    boolean supported;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class BulkConfiguration extends SupportedConfiguration {
    int maxOperations;
    int maxPayloadSize;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class FilterConfiguration extends SupportedConfiguration {
    int maxResults;
  }

  @XmlElement
  String documentationUrl;

  @XmlElement
  SupportedConfiguration patch;

  @XmlElement
  BulkConfiguration bulk;

  @XmlElement
  FilterConfiguration filter;

  @XmlElement
  SupportedConfiguration changePassword;

  @XmlElement
  SupportedConfiguration sort;

  @XmlElement
  SupportedConfiguration etag;

  @XmlElement
  List<AuthenticationSchema> authenticationSchemes;

  public ServiceProviderConfiguration() {
    super(SCHEMA_URI, RESOURCE_NAME);
  }

}
