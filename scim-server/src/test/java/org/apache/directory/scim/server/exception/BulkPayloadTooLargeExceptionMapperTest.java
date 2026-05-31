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

import jakarta.ws.rs.core.Response;

import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkPayloadTooLargeExceptionMapperTest {

  /**
   * RFC 7644 §3.7.4: an exceeded payload yields HTTP 413, scimType "tooMany", and a
   * detail that states the maximum payload size.
   */
  @Test
  public void mapsToHttp413_withTooMany_andMaxSizeInDetail() {
    BulkPayloadTooLargeExceptionMapper mapper = new BulkPayloadTooLargeExceptionMapper();

    Response response = mapper.toResponse(new BulkPayloadTooLargeException(2_097_152));

    assertThat(response.getStatus())
      .isEqualTo(Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode());
    assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
    ErrorResponse error = (ErrorResponse) response.getEntity();
    assertThat(error.getScimType()).isEqualTo(ErrorMessageType.TOO_MANY);
    assertThat(error.getDetail()).contains("2097152");
  }
}
