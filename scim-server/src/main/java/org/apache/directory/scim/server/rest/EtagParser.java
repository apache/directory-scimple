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

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.core.repository.ETag;

public class EtagParser {

  private EtagParser() {
  }

  /**
   * Parse comma-separated entity tags
   *
   * @param value
   * @return
   * @throws IllegalArgumentException
   */
  public static Set<ETag> parseETag(String value) throws IllegalArgumentException {
    if (StringUtils.isNotBlank(value)) {
      Set<ETag> result = new HashSet<>();

      for (String etag : value.split(",")) {
        etag = etag.trim();
        boolean weakTag = false;

        if (etag.startsWith("W/")) {
          weakTag = true;
          etag = etag.substring(2);
        }

        if (etag.startsWith("\"")) {
          etag = etag.substring(1);
        }

        if (etag.endsWith("\"")) {
          etag = etag.substring(0, etag.length() - 1);
        }

        result.add(new ETag(etag, weakTag));
      }
      return result;
    } else {
      return null;
    }
  }
}
