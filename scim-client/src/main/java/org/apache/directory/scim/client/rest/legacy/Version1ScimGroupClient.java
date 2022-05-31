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

package org.apache.directory.scim.client.rest.legacy;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.directory.scim.client.rest.ScimGroupClient;
import org.apache.directory.scim.ws.common.RestCall;

public class Version1ScimGroupClient extends ScimGroupClient {

  public Version1ScimGroupClient(Client client, String baseUrl) {
    super(client, baseUrl);
  }

  public Version1ScimGroupClient(Client client, String baseUrl, RestCall invoke) {
    super(client, baseUrl, invoke);
  }
  
  @Override
  protected String getContentType() {
    return MediaType.APPLICATION_JSON;
  }
}
