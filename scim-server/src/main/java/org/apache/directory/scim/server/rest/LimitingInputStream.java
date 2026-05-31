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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.directory.scim.server.exception.BulkPayloadTooLargeException;

/**
 * An {@link InputStream} wrapper that counts the bytes read from the underlying
 * stream and throws {@link BulkPayloadTooLargeException} as soon as the running
 * total exceeds {@code limit}. Exactly {@code limit} bytes are permitted; the
 * {@code limit+1}-th byte triggers the exception.
 *
 * <p>Because it counts as the consumer reads, the request body is never fully
 * buffered: it streams straight into the entity parser and is rejected the
 * moment it crosses the configured ceiling.</p>
 */
class LimitingInputStream extends FilterInputStream {

  private final long limit;
  private long count;

  LimitingInputStream(InputStream in, long limit) {
    super(in);
    this.limit = limit;
  }

  @Override
  public int read() throws IOException {
    int b = in.read();
    if (b != -1 && ++count > limit) {
      throw new BulkPayloadTooLargeException(limit);
    }
    return b;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int read = in.read(b, off, len);
    if (read > 0) {
      count += read;
      if (count > limit) {
        throw new BulkPayloadTooLargeException(limit);
      }
    }
    return read;
  }
}
