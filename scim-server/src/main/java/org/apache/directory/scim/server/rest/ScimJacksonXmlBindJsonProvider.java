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

import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import org.apache.directory.scim.spec.json.ObjectMapperFactory;
import org.apache.directory.scim.protocol.Constants;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;

/**
 * Adds JacksonJaxbJsonProvider for custom MediaType {@code application/scim+json}.
 */
@Provider
@Consumes(Constants.SCIM_CONTENT_TYPE)
@Produces(Constants.SCIM_CONTENT_TYPE)
public class ScimJacksonXmlBindJsonProvider extends JacksonXmlBindJsonProvider {

  @Inject
  public ScimJacksonXmlBindJsonProvider() {
    super(ObjectMapperFactory.getObjectMapper(), DEFAULT_ANNOTATIONS);
  }
}
