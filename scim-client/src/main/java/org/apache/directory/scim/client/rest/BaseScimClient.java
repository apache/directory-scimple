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

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.spec.adapter.FilterWrapper;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.protocol.BaseResourceTypeResource;
import org.apache.directory.scim.spec.protocol.Constants;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.ListResponse;
import org.apache.directory.scim.spec.protocol.data.PatchRequest;
import org.apache.directory.scim.spec.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.protocol.search.SortOrder;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.ws.common.RestCall;
import org.apache.directory.scim.ws.common.RestClientUtil;
import org.apache.directory.scim.ws.common.exception.RestClientException;

public abstract class BaseScimClient<T extends ScimResource> implements AutoCloseable {

  static final String ATTRIBUTES_QUERY_PARAM = "attributes";
  static final String EXCLUDED_ATTRIBUTES_QUERY_PARAM = "excludedAttributes";

  private final Client client;
  private final Class<T> scimResourceClass;
  private final GenericType<ListResponse<T>> scimResourceListResponseGenericType;
  private final WebTarget target;
  private final InternalScimClient scimClient;
  private RestCall invoke = Invocation::invoke;

  public BaseScimClient(Client client, String baseUrl, Class<T> scimResourceClass, GenericType<ListResponse<T>> scimResourceListGenericType) {
    ScimResourceType scimResourceType = scimResourceClass.getAnnotation(ScimResourceType.class);
    String endpoint = scimResourceType != null ? scimResourceType.endpoint() : null;

    if (endpoint == null) {
      throw new IllegalArgumentException("scimResourceClass: " + scimResourceClass.getSimpleName() + " must have annotation " + ScimResourceType.class.getSimpleName() + " and annotation must have non-null endpoint");
    }
    this.client = client;
    this.scimResourceClass = scimResourceClass;
    this.scimResourceListResponseGenericType = scimResourceListGenericType;
    this.target = this.client.target(baseUrl).path(endpoint);
    this.scimClient = new InternalScimClient();
  }

  public BaseScimClient(Client client, String baseUrl, Class<T> scimResourceClass, GenericType<ListResponse<T>> scimResourceListGenericType, RestCall invoke) {
    this(client, baseUrl, scimResourceClass, scimResourceListGenericType);

    this.invoke = invoke;
  }

  @Override
  public void close() {
    this.client.close();
  }

  public Optional<T> getById(String id) throws ScimException {
    return this.getById(id, null, null);
  }

  public Optional<T> getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Optional<T> resource;
    Response response = this.scimClient.getById(id, attributes, excludedAttributes);

    try {
      if (RestClientUtil.isSuccessful(response)) {
        resource = Optional.of(response.readEntity(this.scimResourceClass));
      } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
        resource = Optional.empty();
      } else {
        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);

        throw new ScimException(errorResponse, Status.fromStatusCode(response.getStatus()));
      }
    } finally {
      RestClientUtil.close(response);
    }
    return resource;
  }

  public ListResponse<T> query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) throws ScimException {
    ListResponse<T> listResponse;
    FilterWrapper filterWrapper = new FilterWrapper(filter);
    Response response = this.scimClient.query(attributes, excludedAttributes, filterWrapper, sortBy, sortOrder, startIndex, count);
    listResponse = handleResponse(response, scimResourceListResponseGenericType, response::readEntity);

    return listResponse;
  }

  public T create(T resource) throws ScimException {
    return this.create(resource, null, null);
  }

  public T create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.scimClient.create(resource, attributes, excludedAttributes);
    
    return handleResponse(response, scimResourceClass, response::readEntity);
  }

  public ListResponse<T> find(SearchRequest searchRequest) throws ScimException {
    ListResponse<T> listResponse;
    Response response = this.scimClient.find(searchRequest);
    listResponse = handleResponse(response, scimResourceListResponseGenericType, response::readEntity);

    return listResponse;
  }

  public T update(String id, T resource) throws ScimException {
    return this.update(id, resource, null, null);
  }

  public T update(String id, T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.scimClient.update(resource, id, attributes, excludedAttributes);

    return handleResponse(response, scimResourceClass, response::readEntity);
  }

  public T patch(String id, PatchRequest patchRequest) throws ScimException {
    return this.patch(id, patchRequest, null, null);
  }
  
  public T patch(String id, PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.scimClient.patch(patchRequest, id, attributes, excludedAttributes);

    return handleResponse(response, scimResourceClass, response::readEntity);
  }

  public void delete(String id) throws ScimException {
    Response response = this.scimClient.delete(id);

    handleResponse(response);
  }

  static <E, T> E handleResponse(Response response, T type, Function<T, E> readEntity) throws ScimException {
    E entity;

    try {
      if (RestClientUtil.isSuccessful(response)) {
        entity = readEntity.apply(type);
      } else {
        Status status = Status.fromStatusCode(response.getStatus());
        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);

        throw new ScimException(errorResponse, status);
      }
    } finally {
      RestClientUtil.close(response);
    }
    return entity;
  }

  static void handleResponse(Response response) throws ScimException {
    try {
      if (!RestClientUtil.isSuccessful(response)) {
        Status status = Status.fromStatusCode(response.getStatus());
        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);

        throw new ScimException(errorResponse, status);
      }
    } catch (ProcessingException e) {
      ErrorResponse er = new ErrorResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
      throw new ScimException(er, Status.INTERNAL_SERVER_ERROR);
    } finally {
      RestClientUtil.close(response);
    }
  }

  static ScimException toScimException(RestClientException restClientException) {
    ScimException scimException;
    Status status = restClientException.getErrorMessage().getStatus();
    ErrorResponse errorResponse = new ErrorResponse(status, String.join("\n", restClientException.getErrorMessage().getErrorMessageList()));
    scimException = new ScimException(errorResponse, status);

    return scimException;
  }

  public RestCall getInvoke() {
    return this.invoke;
  }

  public void setInvoke(RestCall invoke) {
    this.invoke = invoke;
  }

  private class InternalScimClient implements BaseResourceTypeResource<T> {

    private static final String FILTER_QUERY_PARAM = "filter";
    private static final String SORT_BY_QUERY_PARAM = "sortBy";
    private static final String SORT_ORDER_QUERY_PARAM = "sortOrder";
    private static final String START_INDEX_QUERY_PARAM = "startIndex";
    private static final String COUNT_QUERY_PARAM = "count";

    @Override
    public Response getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .path(id)
          .queryParam(ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(attributes))
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(excludedAttributes))
          .request(getContentType())
          .buildGet();

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, FilterWrapper filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .queryParam(ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(attributes))
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(excludedAttributes))
          .queryParam(FILTER_QUERY_PARAM, filter.getFilter())
          .queryParam(SORT_BY_QUERY_PARAM, sortBy)
          .queryParam(SORT_ORDER_QUERY_PARAM, sortOrder != null ? sortOrder.name() : null)
          .queryParam(START_INDEX_QUERY_PARAM, startIndex)
          .queryParam(COUNT_QUERY_PARAM, count)
          .request(getContentType())
          .buildGet();

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .queryParam(ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(attributes))
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(excludedAttributes))
          .request(getContentType())
          .buildPost(Entity.entity(resource, getContentType()));

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response find(SearchRequest searchRequest) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .path(".search")
          .request(getContentType())
          .buildPost(Entity.entity(searchRequest, getContentType()));

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response update(T resource, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .path(id)
          .queryParam(ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(attributes))
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(excludedAttributes))
          .request(getContentType())
          .buildPut(Entity.entity(resource, getContentType()));

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response patch(PatchRequest patchRequest, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .path(id)
          .queryParam(ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(attributes))
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, nullOutQueryParamIfListIsNullOrEmpty(excludedAttributes))
          .request(getContentType())
          .build("PATCH", Entity.entity(patchRequest, getContentType()));

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response delete(String id) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .path(id)
          .request(getContentType())
          .buildDelete();

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }
    
    private AttributeReferenceListWrapper nullOutQueryParamIfListIsNullOrEmpty(AttributeReferenceListWrapper wrapper) {
      if (wrapper == null) {
        return null;
      }
      Set<AttributeReference> attributeReferences = wrapper.getAttributeReferences();
      if (attributeReferences == null || attributeReferences.isEmpty()) {
        return null;
      }
      
      return wrapper;
    }
  }

  protected String getContentType() {
    return Constants.SCIM_CONTENT_TYPE;
  }
}
