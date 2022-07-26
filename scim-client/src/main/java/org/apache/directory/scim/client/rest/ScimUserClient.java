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

package org.apache.directory.scim.client.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;

import org.apache.directory.scim.spec.protocol.data.ListResponse;
import org.apache.directory.scim.spec.resources.ScimUser;

public class ScimUserClient extends BaseScimClient<ScimUser> {

  private static final GenericType<ListResponse<ScimUser>> LIST_SCIM_USER = new GenericType<ListResponse<ScimUser>>(){};

  public ScimUserClient(Client client, String baseUrl) {
    super(client, baseUrl, ScimUser.class, LIST_SCIM_USER);
  }

  public ScimUserClient(Client client, String baseUrl, RestCall invoke) {
    super(client, baseUrl, ScimUser.class, LIST_SCIM_USER, invoke);
  }
}
