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
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.apache.directory.scim.spec.protocol.Constants;
import org.apache.directory.scim.spec.protocol.SelfResource;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.protocol.data.PatchRequest;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.ScimUser;

// purposefully does not extend BaseScimClient, has a different utility than other clients

public class ScimSelfClient implements AutoCloseable {

  private Client client;
  private WebTarget target;
  private SelfResourceClient selfResourceClient;
  private RestCall invoke = Invocation::invoke;

  public ScimSelfClient(Client client, String baseUrl) {
    this.client = client;
    this.target = this.client.target(baseUrl).path(SelfResource.PATH);
    this.selfResourceClient = new SelfResourceClient();
  }

  public ScimSelfClient(Client client, String baseUrl, RestCall invoke) {
    this(client, baseUrl);

    this.invoke = invoke;
  }

  public ScimUser get(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    ScimUser self;
    Response response = this.selfResourceClient.get(attributes, excludedAttributes);
    self = BaseScimClient.handleResponse(response, ScimUser.class, response::readEntity);

    return self;
  }

  public ScimUser get() throws ScimException {
    ScimUser self = this.get(null, null);

    return self;
  }

  public void update(ScimUser scimUser, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.selfResourceClient.update(scimUser, attributes, excludedAttributes);

    BaseScimClient.handleResponse(response);
  }

  public void update(ScimUser scimUser) throws ScimException {
    this.update(scimUser, null, null);
  }

  public void patch(PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.selfResourceClient.patch(patchRequest, attributes, excludedAttributes);

    BaseScimClient.handleResponse(response);
  }

  public void patch(PatchRequest patchRequest) throws ScimException {
    this.patch(patchRequest, null, null);
  }

  public void delete() throws ScimException {
    Response response = this.selfResourceClient.delete();

    BaseScimClient.handleResponse(response);
  }

  public RestCall getInvoke() {
    return this.invoke;
  }

  @Override
  public void close() throws Exception {
    this.client.close();
  }

  private class SelfResourceClient implements SelfResource {

    @Override
    public Response get(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = ScimSelfClient.this.target
          .queryParam(BaseScimClient.ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(BaseScimClient.EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildGet();

      try {
        response = ScimSelfClient.this.invoke.apply(request);
      } catch (RestException restException) {
        throw BaseScimClient.toScimException(restException);
      }
      return response;
    }

    @Override
    public Response update(ScimUser scimUser, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = ScimSelfClient.this.target
          .queryParam(BaseScimClient.ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(BaseScimClient.EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildPut(Entity.entity(scimUser, Constants.SCIM_CONTENT_TYPE));

      try {
        response = ScimSelfClient.this.invoke.apply(request);
      } catch (RestException restException) {
        throw BaseScimClient.toScimException(restException);
      }
      return response;
    }

    @Override
    public Response patch(PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = ScimSelfClient.this.target
          .queryParam(BaseScimClient.ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(BaseScimClient.EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .build(Constants.PATCH, Entity.entity(patchRequest, Constants.SCIM_CONTENT_TYPE));

      try {
        response = ScimSelfClient.this.invoke.apply(request);
      } catch (RestException restException) {
        throw BaseScimClient.toScimException(restException);
      }
      return response;
    }

    @Override
    public Response delete() throws ScimException {
      Response response;
      Invocation request = ScimSelfClient.this.target
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildDelete();

      try {
        response = ScimSelfClient.this.invoke.apply(request);
      } catch (RestException restException) {
        throw BaseScimClient.toScimException(restException);
      }
      return response;
    }
  }
}
