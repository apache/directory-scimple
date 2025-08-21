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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResourceWithOptionalId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceProviderConfiguration extends ScimResourceWithOptionalId {

  public static final String RESOURCE_NAME = "ServiceProviderConfig";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";
  private static final long serialVersionUID = -6526116522184446474L;

  public String getDocumentationUrl() {
    return this.documentationUrl;
  }

  public ServiceProviderConfiguration setDocumentationUrl(String documentationUrl) {
    this.documentationUrl = documentationUrl;
    return this;
  }

  public SupportedConfiguration getPatch() {
    return this.patch;
  }

  public ServiceProviderConfiguration setPatch(SupportedConfiguration patch) {
    this.patch = patch;
    return this;
  }

  public BulkConfiguration getBulk() {
    return this.bulk;
  }

  public ServiceProviderConfiguration setBulk(BulkConfiguration bulk) {
    this.bulk = bulk;
    return this;
  }

  public FilterConfiguration getFilter() {
    return this.filter;
  }

  public ServiceProviderConfiguration setFilter(FilterConfiguration filter) {
    this.filter = filter;
    return this;
  }

  public SupportedConfiguration getChangePassword() {
    return this.changePassword;
  }

  public ServiceProviderConfiguration setChangePassword(SupportedConfiguration changePassword) {
    this.changePassword = changePassword;
    return this;
  }

  public SupportedConfiguration getSort() {
    return this.sort;
  }

  public ServiceProviderConfiguration setSort(SupportedConfiguration sort) {
    this.sort = sort;
    return this;
  }

  public SupportedConfiguration getEtag() {
    return this.etag;
  }

  public ServiceProviderConfiguration setEtag(SupportedConfiguration etag) {
    this.etag = etag;
    return this;
  }

  public List<AuthenticationSchema> getAuthenticationSchemes() {
    return this.authenticationSchemes;
  }

  public ServiceProviderConfiguration setAuthenticationSchemes(List<AuthenticationSchema> authenticationSchemes) {
    this.authenticationSchemes = authenticationSchemes;
    return this;
  }

  public String toString() {
    return "ServiceProviderConfiguration(documentationUrl=" + this.getDocumentationUrl() + ", patch=" + this.getPatch() + ", bulk=" + this.getBulk() + ", filter=" + this.getFilter() + ", changePassword=" + this.getChangePassword() + ", sort=" + this.getSort() + ", etag=" + this.getEtag() + ", authenticationSchemes=" + this.getAuthenticationSchemes() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ServiceProviderConfiguration)) return false;
    final ServiceProviderConfiguration other = (ServiceProviderConfiguration) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$documentationUrl = this.getDocumentationUrl();
    final Object other$documentationUrl = other.getDocumentationUrl();
    if (this$documentationUrl == null ? other$documentationUrl != null : !this$documentationUrl.equals(other$documentationUrl))
      return false;
    final Object this$patch = this.getPatch();
    final Object other$patch = other.getPatch();
    if (this$patch == null ? other$patch != null : !this$patch.equals(other$patch)) return false;
    final Object this$bulk = this.getBulk();
    final Object other$bulk = other.getBulk();
    if (this$bulk == null ? other$bulk != null : !this$bulk.equals(other$bulk)) return false;
    final Object this$filter = this.getFilter();
    final Object other$filter = other.getFilter();
    if (this$filter == null ? other$filter != null : !this$filter.equals(other$filter)) return false;
    final Object this$changePassword = this.getChangePassword();
    final Object other$changePassword = other.getChangePassword();
    if (this$changePassword == null ? other$changePassword != null : !this$changePassword.equals(other$changePassword))
      return false;
    final Object this$sort = this.getSort();
    final Object other$sort = other.getSort();
    if (this$sort == null ? other$sort != null : !this$sort.equals(other$sort)) return false;
    final Object this$etag = this.getEtag();
    final Object other$etag = other.getEtag();
    if (this$etag == null ? other$etag != null : !this$etag.equals(other$etag)) return false;
    final Object this$authenticationSchemes = this.getAuthenticationSchemes();
    final Object other$authenticationSchemes = other.getAuthenticationSchemes();
    if (this$authenticationSchemes == null ? other$authenticationSchemes != null : !this$authenticationSchemes.equals(other$authenticationSchemes))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ServiceProviderConfiguration;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $documentationUrl = this.getDocumentationUrl();
    result = result * PRIME + ($documentationUrl == null ? 43 : $documentationUrl.hashCode());
    final Object $patch = this.getPatch();
    result = result * PRIME + ($patch == null ? 43 : $patch.hashCode());
    final Object $bulk = this.getBulk();
    result = result * PRIME + ($bulk == null ? 43 : $bulk.hashCode());
    final Object $filter = this.getFilter();
    result = result * PRIME + ($filter == null ? 43 : $filter.hashCode());
    final Object $changePassword = this.getChangePassword();
    result = result * PRIME + ($changePassword == null ? 43 : $changePassword.hashCode());
    final Object $sort = this.getSort();
    result = result * PRIME + ($sort == null ? 43 : $sort.hashCode());
    final Object $etag = this.getEtag();
    result = result * PRIME + ($etag == null ? 43 : $etag.hashCode());
    final Object $authenticationSchemes = this.getAuthenticationSchemes();
    result = result * PRIME + ($authenticationSchemes == null ? 43 : $authenticationSchemes.hashCode());
    return result;
  }

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  public static class AuthenticationSchema implements Serializable {

    private static final long serialVersionUID = 1286852277186580002L;

    public Type getType() {
      return this.type;
    }

    public AuthenticationSchema setType(Type type) {
      this.type = type;
      return this;
    }

    public String getName() {
      return this.name;
    }

    public AuthenticationSchema setName(String name) {
      this.name = name;
      return this;
    }

    public String getDescription() {
      return this.description;
    }

    public AuthenticationSchema setDescription(String description) {
      this.description = description;
      return this;
    }

    public String getSpecUri() {
      return this.specUri;
    }

    public AuthenticationSchema setSpecUri(String specUri) {
      this.specUri = specUri;
      return this;
    }

    public String getDocumentationUri() {
      return this.documentationUri;
    }

    public AuthenticationSchema setDocumentationUri(String documentationUri) {
      this.documentationUri = documentationUri;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof AuthenticationSchema)) return false;
      final AuthenticationSchema other = (AuthenticationSchema) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$type = this.getType();
      final Object other$type = other.getType();
      if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
      final Object this$name = this.getName();
      final Object other$name = other.getName();
      if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
      final Object this$description = this.getDescription();
      final Object other$description = other.getDescription();
      if (this$description == null ? other$description != null : !this$description.equals(other$description))
        return false;
      final Object this$specUri = this.getSpecUri();
      final Object other$specUri = other.getSpecUri();
      if (this$specUri == null ? other$specUri != null : !this$specUri.equals(other$specUri)) return false;
      final Object this$documentationUri = this.getDocumentationUri();
      final Object other$documentationUri = other.getDocumentationUri();
      if (this$documentationUri == null ? other$documentationUri != null : !this$documentationUri.equals(other$documentationUri))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof AuthenticationSchema;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $type = this.getType();
      result = result * PRIME + ($type == null ? 43 : $type.hashCode());
      final Object $name = this.getName();
      result = result * PRIME + ($name == null ? 43 : $name.hashCode());
      final Object $description = this.getDescription();
      result = result * PRIME + ($description == null ? 43 : $description.hashCode());
      final Object $specUri = this.getSpecUri();
      result = result * PRIME + ($specUri == null ? 43 : $specUri.hashCode());
      final Object $documentationUri = this.getDocumentationUri();
      result = result * PRIME + ($documentationUri == null ? 43 : $documentationUri.hashCode());
      return result;
    }

    public String toString() {
      return "ServiceProviderConfiguration.AuthenticationSchema(type=" + this.getType() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", specUri=" + this.getSpecUri() + ", documentationUri=" + this.getDocumentationUri() + ")";
    }

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

  public static class SupportedConfiguration implements Serializable {
    private static final long serialVersionUID = 3646886915978382920L;
    boolean supported;

    public boolean isSupported() {
      return this.supported;
    }

    public SupportedConfiguration setSupported(boolean supported) {
      this.supported = supported;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof SupportedConfiguration)) return false;
      final SupportedConfiguration other = (SupportedConfiguration) o;
      if (!other.canEqual((Object) this)) return false;
      if (this.isSupported() != other.isSupported()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof SupportedConfiguration;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = result * PRIME + (this.isSupported() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "ServiceProviderConfiguration.SupportedConfiguration(supported=" + this.isSupported() + ")";
    }
  }

  public static class BulkConfiguration extends SupportedConfiguration {
    private static final long serialVersionUID = 8312025367100671778L;
    int maxOperations;
    int maxPayloadSize;

    public int getMaxOperations() {
      return this.maxOperations;
    }

    public int getMaxPayloadSize() {
      return this.maxPayloadSize;
    }

    public BulkConfiguration setMaxOperations(int maxOperations) {
      this.maxOperations = maxOperations;
      return this;
    }

    public BulkConfiguration setMaxPayloadSize(int maxPayloadSize) {
      this.maxPayloadSize = maxPayloadSize;
      return this;
    }

    public String toString() {
      return "ServiceProviderConfiguration.BulkConfiguration(maxOperations=" + this.getMaxOperations() + ", maxPayloadSize=" + this.getMaxPayloadSize() + ")";
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof BulkConfiguration)) return false;
      final BulkConfiguration other = (BulkConfiguration) o;
      if (!other.canEqual((Object) this)) return false;
      if (!super.equals(o)) return false;
      if (this.getMaxOperations() != other.getMaxOperations()) return false;
      if (this.getMaxPayloadSize() != other.getMaxPayloadSize()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof BulkConfiguration;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = super.hashCode();
      result = result * PRIME + this.getMaxOperations();
      result = result * PRIME + this.getMaxPayloadSize();
      return result;
    }
  }

  public static class FilterConfiguration extends SupportedConfiguration {
    private static final long serialVersionUID = 1887771731291732875L;
    int maxResults;

    public int getMaxResults() {
      return this.maxResults;
    }

    public FilterConfiguration setMaxResults(int maxResults) {
      this.maxResults = maxResults;
      return this;
    }

    public String toString() {
      return "ServiceProviderConfiguration.FilterConfiguration(maxResults=" + this.getMaxResults() + ")";
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof FilterConfiguration)) return false;
      final FilterConfiguration other = (FilterConfiguration) o;
      if (!other.canEqual((Object) this)) return false;
      if (!super.equals(o)) return false;
      if (this.getMaxResults() != other.getMaxResults()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof FilterConfiguration;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = super.hashCode();
      result = result * PRIME + this.getMaxResults();
      return result;
    }
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


  @Override
  public ServiceProviderConfiguration setSchemas(Set<String> schemas) {
    return (ServiceProviderConfiguration) super.setSchemas(schemas);
  }

  @Override
  public ServiceProviderConfiguration setExtensions(Map<String, ScimExtension> extensions) {
    return (ServiceProviderConfiguration) super.setExtensions(extensions);
  }

  @Override
  public ServiceProviderConfiguration setExternalId(String externalId) {
    return (ServiceProviderConfiguration) super.setExternalId(externalId);
  }

  @Override
  public ServiceProviderConfiguration setMeta(@NotNull Meta meta) {
    return (ServiceProviderConfiguration) super.setMeta(meta);
  }

  @Override
  public ServiceProviderConfiguration setId(String id) {
    return (ServiceProviderConfiguration) super.setId(id);
  }

  @Override
  public ServiceProviderConfiguration addSchema(String urn) {
    return (ServiceProviderConfiguration) super.addSchema(urn);
  }

  @Override
  public ServiceProviderConfiguration addExtension(ScimExtension extension) {
    return (ServiceProviderConfiguration) super.addExtension(extension);
  }
}
