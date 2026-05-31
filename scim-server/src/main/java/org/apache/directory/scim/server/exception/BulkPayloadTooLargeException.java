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

package org.apache.directory.scim.server.exception;

/**
 * Thrown while streaming a {@code /Bulk} request body when the number of bytes
 * read exceeds the configured {@code bulkMaxPayloadSize}. It is raised lazily
 * from the request entity stream as the body is parsed (so the whole payload is
 * never buffered) and mapped to an HTTP 413 response by
 * {@code BulkPayloadTooLargeExceptionMapper} per RFC 7644 §3.7.4.
 */
public class BulkPayloadTooLargeException extends RuntimeException {

  private final long maxPayloadSize;

  /**
   * @param maxPayloadSize the configured maximum payload size, in bytes, that was exceeded
   */
  public BulkPayloadTooLargeException(long maxPayloadSize) {
    super("Bulk request payload exceeds the maximum allowed size of " + maxPayloadSize + " bytes");
    this.maxPayloadSize = maxPayloadSize;
  }

  /**
   * @return the configured maximum payload size, in bytes
   */
  public long getMaxPayloadSize() {
    return maxPayloadSize;
  }
}
