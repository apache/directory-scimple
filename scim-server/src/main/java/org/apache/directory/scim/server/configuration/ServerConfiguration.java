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

package org.apache.directory.scim.server.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

/**
 * Provides a default server configuration with the values that are ultimately
 * returned by the ServerProviderConfig end-point.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
public class ServerConfiguration {
  
  static final int BULK_MAXIMUM_OPERATIONS = 100;
  static final int BULK_MAXIMUM_PAYLOAD_SIZE = 1024;
  
  static final int FILTER_MAXIMUM_RESULTS = 100;

  String id = "spc";
  
  boolean supportsChangePassword = false;
  
  boolean supportsBulk = true;
  int bulkMaxOperations = BULK_MAXIMUM_OPERATIONS;
  int bulkMaxPayloadSize = BULK_MAXIMUM_PAYLOAD_SIZE;  //TODO what should this be?
  
  boolean supportsETag = true;
  
  boolean supportsFilter = false;
  int filterMaxResults = FILTER_MAXIMUM_RESULTS;
  
  boolean supportsPatch = true;
  
  boolean supportsSort = false;
  
  String documentationUri;
  
  List<AuthenticationSchema> authenticationSchemas = new ArrayList<>();
  
  public List<AuthenticationSchema> getAuthenticationSchemas() {
    return Collections.unmodifiableList(authenticationSchemas);
  }

  public ServerConfiguration addAuthenticationSchema(AuthenticationSchema authenticationSchema) {
    authenticationSchemas.add(authenticationSchema);
    return this;
  }

  public ServerConfiguration removeAuthenticationSchema(AuthenticationSchema authenticationSchema) {
    authenticationSchemas.remove(authenticationSchema);
    return this;
  }

  public SupportedConfiguration getChangePasswordConfiguration() {
    return createSupportedConfiguration(isSupportsChangePassword());
  }

  public BulkConfiguration getBulkConfiguration() {
    BulkConfiguration bulkConfiguration = new BulkConfiguration();

    bulkConfiguration.setSupported(isSupportsBulk());
    bulkConfiguration.setMaxOperations(getBulkMaxOperations());
    bulkConfiguration.setMaxPayloadSize(getBulkMaxPayloadSize());

    return bulkConfiguration;
  }

  public SupportedConfiguration getEtagConfiguration() {
    return createSupportedConfiguration(isSupportsETag());

  }

  public FilterConfiguration getFilterConfiguration() {
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.setSupported(isSupportsFilter());
    filterConfiguration.setMaxResults(getFilterMaxResults());
    return filterConfiguration;
  }

  public SupportedConfiguration getPatchConfiguration() {
    return createSupportedConfiguration(isSupportsPatch());
  }

  public SupportedConfiguration getSortConfiguration() {
    return createSupportedConfiguration(isSupportsSort());
  }
  
  private SupportedConfiguration createSupportedConfiguration(boolean supported) {
    SupportedConfiguration supportedConfiguration = new SupportedConfiguration();
    supportedConfiguration.setSupported(supported);
    return supportedConfiguration;
  }

  public String getId() {
    return this.id;
  }

  public ServerConfiguration setId(String id) {
    this.id = id;
    return this;
  }

  public boolean isSupportsChangePassword() {
    return this.supportsChangePassword;
  }

  public ServerConfiguration setSupportsChangePassword(boolean supportsChangePassword) {
    this.supportsChangePassword = supportsChangePassword;
    return this;
  }

  public boolean isSupportsBulk() {
    return this.supportsBulk;
  }

  public ServerConfiguration setSupportsBulk(boolean supportsBulk) {
    this.supportsBulk = supportsBulk;
    return this;
  }

  public int getBulkMaxOperations() {
    return this.bulkMaxOperations;
  }

  public ServerConfiguration setBulkMaxOperations(int bulkMaxOperations) {
    this.bulkMaxOperations = bulkMaxOperations;
    return this;
  }

  public int getBulkMaxPayloadSize() {
    return this.bulkMaxPayloadSize;
  }

  public ServerConfiguration setBulkMaxPayloadSize(int bulkMaxPayloadSize) {
    this.bulkMaxPayloadSize = bulkMaxPayloadSize;
    return this;
  }

  public boolean isSupportsETag() {
    return this.supportsETag;
  }

  public ServerConfiguration setSupportsETag(boolean supportsETag) {
    this.supportsETag = supportsETag;
    return this;
  }

  public boolean isSupportsFilter() {
    return this.supportsFilter;
  }

  public ServerConfiguration setSupportsFilter(boolean supportsFilter) {
    this.supportsFilter = supportsFilter;
    return this;
  }

  public int getFilterMaxResults() {
    return this.filterMaxResults;
  }

  public ServerConfiguration setFilterMaxResults(int filterMaxResults) {
    this.filterMaxResults = filterMaxResults;
    return this;
  }

  public boolean isSupportsPatch() {
    return this.supportsPatch;
  }

  public ServerConfiguration setSupportsPatch(boolean supportsPatch) {
    this.supportsPatch = supportsPatch;
    return this;
  }

  public boolean isSupportsSort() {
    return this.supportsSort;
  }

  public ServerConfiguration setSupportsSort(boolean supportsSort) {
    this.supportsSort = supportsSort;
    return this;
  }

  public String getDocumentationUri() {
    return this.documentationUri;
  }

  public ServerConfiguration setAuthenticationSchemas(List<AuthenticationSchema> authenticationSchemas) {
    this.authenticationSchemas = authenticationSchemas;
    return this;
  }

  public ServerConfiguration setDocumentationUri(String documentationUri) {
    this.documentationUri = documentationUri;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ServerConfiguration other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    if (this.isSupportsChangePassword() != other.isSupportsChangePassword()) return false;
    if (this.isSupportsBulk() != other.isSupportsBulk()) return false;
    if (this.getBulkMaxOperations() != other.getBulkMaxOperations()) return false;
    if (this.getBulkMaxPayloadSize() != other.getBulkMaxPayloadSize()) return false;
    if (this.isSupportsETag() != other.isSupportsETag()) return false;
    if (this.isSupportsFilter() != other.isSupportsFilter()) return false;
    if (this.getFilterMaxResults() != other.getFilterMaxResults()) return false;
    if (this.isSupportsPatch() != other.isSupportsPatch()) return false;
    if (this.isSupportsSort() != other.isSupportsSort()) return false;
    final Object this$documentationUri = this.getDocumentationUri();
    final Object other$documentationUri = other.getDocumentationUri();
    if (this$documentationUri == null ? other$documentationUri != null : !this$documentationUri.equals(other$documentationUri))
      return false;
    final Object this$authenticationSchemas = this.getAuthenticationSchemas();
    final Object other$authenticationSchemas = other.getAuthenticationSchemas();
    if (this$authenticationSchemas == null ? other$authenticationSchemas != null : !this$authenticationSchemas.equals(other$authenticationSchemas))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ServerConfiguration;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    result = result * PRIME + (this.isSupportsChangePassword() ? 79 : 97);
    result = result * PRIME + (this.isSupportsBulk() ? 79 : 97);
    result = result * PRIME + this.getBulkMaxOperations();
    result = result * PRIME + this.getBulkMaxPayloadSize();
    result = result * PRIME + (this.isSupportsETag() ? 79 : 97);
    result = result * PRIME + (this.isSupportsFilter() ? 79 : 97);
    result = result * PRIME + this.getFilterMaxResults();
    result = result * PRIME + (this.isSupportsPatch() ? 79 : 97);
    result = result * PRIME + (this.isSupportsSort() ? 79 : 97);
    final Object $documentationUri = this.getDocumentationUri();
    result = result * PRIME + ($documentationUri == null ? 43 : $documentationUri.hashCode());
    final Object $authenticationSchemas = this.getAuthenticationSchemas();
    result = result * PRIME + ($authenticationSchemas == null ? 43 : $authenticationSchemas.hashCode());
    return result;
  }

  public String toString() {
    return "ServerConfiguration(id=" + this.getId() + ", supportsChangePassword=" + this.isSupportsChangePassword() + ", supportsBulk=" + this.isSupportsBulk() + ", bulkMaxOperations=" + this.getBulkMaxOperations() + ", bulkMaxPayloadSize=" + this.getBulkMaxPayloadSize() + ", supportsETag=" + this.isSupportsETag() + ", supportsFilter=" + this.isSupportsFilter() + ", filterMaxResults=" + this.getFilterMaxResults() + ", supportsPatch=" + this.isSupportsPatch() + ", supportsSort=" + this.isSupportsSort() + ", documentationUri=" + this.getDocumentationUri() + ", authenticationSchemas=" + this.getAuthenticationSchemas() + ")";
  }
}
