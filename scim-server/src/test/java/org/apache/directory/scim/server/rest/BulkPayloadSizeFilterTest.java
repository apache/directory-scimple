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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.container.ContainerRequestContext;

import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.exception.BulkPayloadTooLargeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BulkPayloadSizeFilterTest {

  @Mock
  ContainerRequestContext ctx;

  // -------------------------------------------------------------------------
  // Filter: limit <= 0 -> pass-through (stream untouched, not wrapped)
  // -------------------------------------------------------------------------

  @Test
  public void limitZero_passThrough() {
    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxPayloadSize(0);

    new BulkPayloadSizeFilter(config).filter(ctx);

    verify(ctx, never()).getEntityStream();
    verify(ctx, never()).setEntityStream(any());
  }

  @Test
  public void limitNegative_passThrough() {
    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxPayloadSize(-1);

    new BulkPayloadSizeFilter(config).filter(ctx);

    verify(ctx, never()).getEntityStream();
    verify(ctx, never()).setEntityStream(any());
  }

  // -------------------------------------------------------------------------
  // Filter: positive limit -> wraps the entity stream with a LimitingInputStream
  // (no buffering); a within-limit body still streams through unchanged.
  // -------------------------------------------------------------------------

  @Test
  public void positiveLimit_wrapsStream_andBodyStreamsThrough() throws Exception {
    byte[] body = "small body".getBytes(StandardCharsets.UTF_8);
    ServerConfiguration config = new ServerConfiguration();
    config.setBulkMaxPayloadSize(1024);
    when(ctx.getEntityStream()).thenReturn(new ByteArrayInputStream(body));

    new BulkPayloadSizeFilter(config).filter(ctx);

    ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
    verify(ctx).setEntityStream(streamCaptor.capture());
    assertThat(streamCaptor.getValue()).isInstanceOf(LimitingInputStream.class);
    // The wrapper is transparent for a within-limit body: the parser sees all bytes.
    assertThat(streamCaptor.getValue().readAllBytes()).isEqualTo(body);
  }

  // -------------------------------------------------------------------------
  // LimitingInputStream: counts as it is read and throws at limit+1, without
  // ever buffering the whole body.
  // -------------------------------------------------------------------------

  @Test
  public void stream_exactlyLimitBytes_readsAllWithoutThrowing() throws Exception {
    byte[] body = "1234567890".getBytes(StandardCharsets.UTF_8); // 10 bytes
    LimitingInputStream stream = new LimitingInputStream(new ByteArrayInputStream(body), 10);

    assertThat(stream.readAllBytes()).isEqualTo(body);
  }

  @Test
  public void stream_limitPlusOneBytes_throwsBulkPayloadTooLarge() {
    byte[] body = "12345678901".getBytes(StandardCharsets.UTF_8); // 11 bytes = limit+1
    LimitingInputStream stream = new LimitingInputStream(new ByteArrayInputStream(body), 10);

    assertThatThrownBy(stream::readAllBytes)
      .isInstanceOf(BulkPayloadTooLargeException.class)
      .extracting(e -> ((BulkPayloadTooLargeException) e).getMaxPayloadSize())
      .isEqualTo(10L);
  }

  @Test
  public void stream_singleByteReads_throwAtLimitPlusOne() {
    byte[] body = new byte[4]; // limit is 3
    LimitingInputStream stream = new LimitingInputStream(new ByteArrayInputStream(body), 3);

    assertThatThrownBy(() -> {
      for (int i = 0; i < body.length; i++) {
        stream.read();
      }
    }).isInstanceOf(BulkPayloadTooLargeException.class);
  }

  @Test
  public void stream_hugeLimit_smallBody_passesThrough() throws Exception {
    // A very large configured limit must not cause any pre-allocation; a small
    // body simply streams through.
    byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
    LimitingInputStream stream =
      new LimitingInputStream(new ByteArrayInputStream(body), Integer.MAX_VALUE);

    assertThat(stream.readAllBytes()).isEqualTo(body);
  }
}
