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

package org.apache.directory.scim.server.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import org.apache.directory.scim.server.configuration.ServerConfiguration;

/**
 * A JAX-RS {@link DynamicFeature} that registers {@link BulkPayloadSizeFilter}
 * only for the {@code BulkResourceImpl#doBulk} endpoint.
 *
 * <p>Keeping the filter off all other routes ensures that the bounded-read
 * logic does not interfere with ordinary resource payloads.</p>
 */
@Provider
public class BulkPayloadSizeDynamicFeature implements DynamicFeature {

  private final ServerConfiguration serverConfiguration;

  @Inject
  public BulkPayloadSizeDynamicFeature(ServerConfiguration serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
  }

  /** No-arg constructor for CDI proxying. */
  public BulkPayloadSizeDynamicFeature() {
    this(null);
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    if (BulkResourceImpl.class.equals(resourceInfo.getResourceClass())
        && "doBulk".equals(resourceInfo.getResourceMethod().getName())) {
      ServerConfiguration effective = serverConfiguration != null
          ? serverConfiguration
          : new ServerConfiguration();
      context.register(new BulkPayloadSizeFilter(effective));
    }
  }
}
