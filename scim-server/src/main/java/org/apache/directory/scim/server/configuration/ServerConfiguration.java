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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
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
@Data
public class ServerConfiguration {
  
  static final int BULK_MAXIMUM_OPERATIONS = 100;
  static final int BULK_MAXIMUM_PAYLOAD_SIZE = 1024;
  
  static final int FILTER_MAXIMUM_RESULTS = 100;

  String id = "spc";
  
  boolean supportsChangePassword = false;
  
  @Setter(AccessLevel.NONE)
  boolean supportsBulk = true;
  int bulkMaxOperations = BULK_MAXIMUM_OPERATIONS;
  int bulkMaxPayloadSize = BULK_MAXIMUM_PAYLOAD_SIZE;  //TODO what should this be?
  
  @Setter(AccessLevel.NONE)
  boolean supportsETag = true;
  
  boolean supportsFilter = false;
  int filterMaxResults = FILTER_MAXIMUM_RESULTS;
  
  @Setter(AccessLevel.NONE)
  boolean supportsPatch = true;
  
  boolean supportsSort = false;
  
  String documentationUri;
  
  @Setter(AccessLevel.NONE)
  List<AuthenticationSchema> authenticationSchemas = new ArrayList<>();
  
  public List<AuthenticationSchema> getAuthenticationSchemas() {
    return Collections.unmodifiableList(authenticationSchemas);
  }

  public ServerConfiguration addAuthenticationSchema(AuthenticationSchema authenticationSchema) {
    authenticationSchemas.add(authenticationSchema);
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

}
