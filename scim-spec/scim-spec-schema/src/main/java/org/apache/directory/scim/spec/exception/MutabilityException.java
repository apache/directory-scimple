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

package org.apache.directory.scim.spec.exception;

/**
 * Thrown when a client attempts to update a ScimResource's attribute/sub-attribute that does not support being updated.
 * e.g. The attribute is read-only, or is immutable but already has a value.
 */
public class MutabilityException extends RuntimeException {

  public MutabilityException(String message) {
    super(message);
  }

  public MutabilityException(String message, Throwable cause) {
    super(message, cause);
  }
}
