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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
      @XmlEnumValue("oauth")
      OAUTH, @XmlEnumValue("oauth2")
      OAUTH2, @XmlEnumValue("oauthbearertoken")
      OAUTH_BEARER, @XmlEnumValue("httpbasic")
      HTTP_BASIC, @XmlEnumValue("httpdigest")
      HTTP_DIGEST;
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
    super(SCHEMA_URI);
  }

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
  }

}
