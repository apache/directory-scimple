/*
 * The Pennsylvania State University © 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.client.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

/**
 * Corresponds to {@link java.util.function.Function} but specific to REST calls.
 */
@FunctionalInterface
public interface RestCall {
  Response apply(Invocation request) throws RestException;
}
