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

package org.apache.directory.scim.protocol.adapter;

import jakarta.ws.rs.core.Response.Status;
import org.apache.directory.scim.protocol.data.StatusAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusAdapterTest {

  @Test
  public void marshal() throws Exception {
    String result = new StatusAdapter().marshal(Status.CONFLICT);
    assertThat(result).isEqualTo("409");
  }

  @Test
  public void marshalNull() throws Exception {
    String result = new StatusAdapter().marshal(null);
    assertThat(result).isNull();
  }

  @Test
  public void unmarshal() throws Exception {
    Status result = new StatusAdapter().unmarshal("400");
    assertThat(result).isEqualTo(Status.BAD_REQUEST);
  }

  @Test
  public void unmarshalNull() throws Exception {
    Status result = new StatusAdapter().unmarshal(null);
    assertThat(result).isNull();
  }
}
