package edu.psu.swe.scim.client.rest;

import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.commons.jaxrs.RestCall;
import edu.psu.swe.commons.jaxrs.exceptions.BackingStoreChangedException;
import edu.psu.swe.commons.jaxrs.exceptions.ConflictingDataException;
import edu.psu.swe.commons.jaxrs.exceptions.RestClientException;
import edu.psu.swe.commons.jaxrs.exceptions.RestServerException;
import edu.psu.swe.commons.jaxrs.exceptions.ServiceAuthException;
import edu.psu.swe.commons.jaxrs.utilities.RestClientUtil;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.exception.ScimException;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.Constants;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.ListResponse;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.schema.ErrorResponse;

public abstract class BaseScimClient<T extends ScimResource> implements AutoCloseable {

  private final Client client;
  private final Class<T> scimResourceClass;
  private final GenericType<ListResponse<T>> scimResourceListResponseGenericType;
  private final WebTarget target;
  private final ScimClient scimClient;
  private RestCall invoke = Invocation::invoke;

  public BaseScimClient(Client client, String baseUrl, Class<T> scimResourceClass, GenericType<ListResponse<T>> scimResourceListGenericType) throws IllegalArgumentException {
    ScimResourceType scimResourceType = scimResourceClass.getAnnotation(ScimResourceType.class);
    String endpoint = scimResourceType != null ? scimResourceType.endpoint() : null;

    if (endpoint == null) {
      throw new IllegalArgumentException("scimResourceClass: " + scimResourceClass.getSimpleName() + " must have annotation " + ScimResourceType.class.getSimpleName() + " and annotation must have non-null endpoint");
    }
    this.client = client;
    this.scimResourceClass = scimResourceClass;
    this.scimResourceListResponseGenericType = scimResourceListGenericType;
    this.target = this.client.target(baseUrl).path(endpoint);
    this.scimClient = new ScimClient();
  }

  public BaseScimClient(Client client, String baseUrl, Class<T> scimResourceClass, GenericType<ListResponse<T>> scimResourceListGenericType, RestCall invoke) throws IllegalArgumentException {
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
    ListResponse<T> listResponse;;
    Response response = this.scimClient.query(attributes, excludedAttributes, filter, sortBy, sortOrder, startIndex, count);
    listResponse = handleResponse(response, scimResourceListResponseGenericType, response::readEntity);

    return listResponse;
  }

  public void create(T resource) throws ScimException {
    this.create(resource, null, null);
  }

  public void create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.scimClient.create(resource, attributes, excludedAttributes);

    handleResponse(response);
  }

  public ListResponse<T> find(SearchRequest searchRequest) throws ScimException {
    ListResponse<T> listResponse;
    Response response = this.scimClient.find(searchRequest);
    listResponse = handleResponse(response, scimResourceListResponseGenericType, response::readEntity);

    return listResponse;
  }

  public void update(String id, T resource) throws ScimException {
    this.update(id, resource, null, null);
  }

  public void update(String id, T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.scimClient.update(resource, id, attributes, excludedAttributes);

    handleResponse(response);
  }

  public void patch(PatchRequest patchRequest) throws ScimException {
    Response response = this.scimClient.patch(patchRequest);

    handleResponse(response);
  }

  public void delete(String id) throws ScimException {
    Response response = this.scimClient.delete(id);

    handleResponse(response);
  }

  private static <E, T> E handleResponse(Response response, T type, Function<T, E> readEntity) throws ScimException {
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

  private static void handleResponse(Response response) throws ScimException {
    try {
      if (!RestClientUtil.isSuccessful(response)) {
        Status status = Status.fromStatusCode(response.getStatus());
        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);

        throw new ScimException(errorResponse, status);
      }
    } finally {
      RestClientUtil.close(response);
    }
  }

  public RestCall getInvoke() {
    return this.invoke;
  }

  public void setInvoke(RestCall invoke) {
    this.invoke = invoke;
  }

  private static ScimException toScimException(RestClientException restClientException) {
    ScimException scimException;
    ErrorResponse errorResponse = new ErrorResponse();
    Status status = restClientException.getErrorMessage().getStatus();
    String statusCode = Integer.toString(status.getStatusCode());

    errorResponse.setDetail(String.join("\n", restClientException.getErrorMessage().getErrorMessageList()));
    errorResponse.setStatus(statusCode);

    scimException = new ScimException(errorResponse, status);

    return scimException;
  }

  private class ScimClient implements BaseResourceTypeResource<T> {

    private static final String ATTRIBUTES_QUERY_PARAM = "attributes";
    private static final String EXCLUDED_ATTRIBUTES_QUERY_PARAM = "excludedAttributes";
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
          .queryParam(ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildGet();

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .queryParam(ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .queryParam(FILTER_QUERY_PARAM, filter)
          .queryParam(SORT_BY_QUERY_PARAM, sortBy)
          .queryParam(SORT_ORDER_QUERY_PARAM, sortOrder != null ? sortOrder.name() : null)
          .queryParam(START_INDEX_QUERY_PARAM, startIndex)
          .queryParam(COUNT_QUERY_PARAM, count)
          .request(Constants.SCIM_CONTENT_TYPE)
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
          .queryParam(ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildPost(Entity.entity(resource, Constants.SCIM_CONTENT_TYPE));

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
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildPost(Entity.entity(searchRequest, Constants.SCIM_CONTENT_TYPE));

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
          .queryParam(ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildPut(Entity.entity(resource, Constants.SCIM_CONTENT_TYPE));

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }

    @Override
    public Response patch(PatchRequest patchRequest) throws ScimException {
      Response response;
      Invocation request = BaseScimClient.this.target
          .request(Constants.SCIM_CONTENT_TYPE)
          .build("PATCH", Entity.entity(patchRequest, Constants.SCIM_CONTENT_TYPE));

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
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildDelete();

      try {
        response = BaseScimClient.this.invoke.apply(request);

        return response;
      } catch (RestClientException restClientException) {
        throw toScimException(restClientException);
      }
    }
  }
}
