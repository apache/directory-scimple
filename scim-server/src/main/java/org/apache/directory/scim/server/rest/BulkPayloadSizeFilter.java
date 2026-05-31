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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import org.apache.directory.scim.server.configuration.ServerConfiguration;

/**
 * A JAX-RS {@link ContainerRequestFilter} that enforces the
 * {@code bulkMaxPayloadSize} limit from {@link ServerConfiguration}.
 *
 * <p>The filter wraps the request entity stream in a {@link LimitingInputStream},
 * which counts bytes as the entity parser reads them. When the running total
 * exceeds the configured limit, a {@code BulkPayloadTooLargeException} is thrown
 * and mapped to HTTP 413 (RFC 7644 §3.7.4). A limit of {@code 0} or less disables
 * the check.</p>
 *
 * <p>The filter is registered only for the {@code POST /Bulk} endpoint by
 * {@link BulkPayloadSizeDynamicFeature}.</p>
 */
class BulkPayloadSizeFilter implements ContainerRequestFilter {

  private final ServerConfiguration serverConfiguration;

  BulkPayloadSizeFilter(ServerConfiguration serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
  }

  @Override
  public void filter(ContainerRequestContext ctx) {
    long limit = serverConfiguration.getBulkMaxPayloadSize();
    if (limit <= 0) {
      return;
    }
    ctx.setEntityStream(new LimitingInputStream(ctx.getEntityStream(), limit));
  }
}
