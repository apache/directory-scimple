package edu.psu.swe.scim.client.rest;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import edu.psu.swe.commons.jaxrs.exceptions.RestClientException;
import edu.psu.swe.commons.jaxrs.utilities.RestClientUtil;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import edu.psu.swe.scim.spec.protocol.Constants;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.resources.ScimResource;

public abstract class BaseScimClient<T extends ScimResource> implements AutoCloseable {

  private final Client client;
  private final Class<T> scimResourceClass;
  private final GenericType<List<T>> scimResourceListGenericType;
  private final WebTarget target;
  private final ScimClient scimClient;

  public BaseScimClient(Client client, String baseUrl, Class<T> scimResourceClass, GenericType<List<T>> scimResourceListGenericType) throws IllegalArgumentException {
    ScimResourceType scimResourceType = scimResourceClass.getAnnotation(ScimResourceType.class);
    String endpoint = scimResourceType != null ? scimResourceType.endpoint() : null;

    if (endpoint == null) {
      throw new IllegalArgumentException("scimResourceClass: " + scimResourceType.getClass().getSimpleName() + " must have annotation " + ScimResourceType.class.getSimpleName() + " and annotation must have non-null endpoint");
    }
    this.client = client;
    this.scimResourceClass = scimResourceClass;
    this.scimResourceListGenericType = scimResourceListGenericType;
    this.target = this.client.target(baseUrl).path(endpoint);
    this.scimClient = new ScimClient();
  }

  @Override
  public void close() {
    this.client.close();
  }

  public Optional<T> getById(String id) throws RestClientException {
    Optional<T> resource = this.getById(id, null, null);

    return resource;
  }

  public Optional<T> getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
    Optional<T> resource;
    Response response = this.scimClient.getById(id, attributes, excludedAttributes);

    try {
      resource = RestClientUtil.readEntity(response, scimResourceClass);
    } finally {
      RestClientUtil.close(response);
    }
    return resource;
  }

  public List<T> query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) throws RestClientException {
    List<T> resourceList;
    Response response = this.scimClient.query(attributes, excludedAttributes, filter, sortBy, sortOrder, startIndex, count);
    resourceList = handleResponse(response, this.scimResourceListGenericType, response::readEntity);

    return resourceList;
  }

  public void create(T resource) throws RestClientException {
    this.create(resource, null, null);
  }

  public void create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
    Response response = this.scimClient.create(resource, attributes, excludedAttributes);

    handleResponse(response);
  }

  public List<T> find(SearchRequest searchRequest) throws RestClientException {
    List<T> resourceList;
    Response response = this.scimClient.find(searchRequest);
    resourceList = handleResponse(response, this.scimResourceListGenericType, response::readEntity);

    return resourceList;
  }

  public void update(String id, T resource) throws RestClientException {
    this.update(id, resource, null, null);
  }

  public void update(String id, T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
    Response response = this.scimClient.update(resource, id, attributes, excludedAttributes);

    handleResponse(response);
  }

  public void patch(PatchRequest patchRequest) throws RestClientException {
    Response response = this.scimClient.patch(patchRequest);

    handleResponse(response);
  }

  public void delete(String id) throws RestClientException {
    Response response = this.scimClient.delete(id);

    handleResponse(response);
  }

  private static <E, T> E handleResponse(Response response, T type, Function<T, E> readEntity) throws RestClientException {
    E entity;

    try {
      RestClientUtil.checkForSuccess(response);

      entity = readEntity.apply(type);
    } finally {
      RestClientUtil.close(response);
    }
    return entity;
  }

  private static void handleResponse(Response response) throws RestClientException {
    try {
      RestClientUtil.checkForSuccess(response);
    } finally {
      RestClientUtil.close(response);
    }
  }

  private class ScimClient implements BaseResourceTypeResource<T> {

    @Override
    public Response getById(String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
      Response response = BaseScimClient.this.target
          .path(id)
          .queryParam("attributes", attributes)
          .queryParam("excludedAttributes", excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .get();

      return response;
    }

    @Override
    public Response query(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, Filter filter, AttributeReference sortBy, SortOrder sortOrder, Integer startIndex, Integer count) throws RestClientException {
      Response response = BaseScimClient.this.target
          .queryParam("attributes", attributes)
          .queryParam("excludedAttributes", excludedAttributes)
          .queryParam("filter", filter)
          .queryParam("sortBy", sortBy)
          .queryParam("sortOrder", sortOrder != null ? sortOrder.name() : null)
          .queryParam("startIndex", startIndex)
          .queryParam("count", count)
          .request(Constants.SCIM_CONTENT_TYPE)
          .get();

      return response;
    }

    @Override
    public Response create(T resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
      Response response = BaseScimClient.this.target
          .queryParam("attributes", attributes)
          .queryParam("excludedAttributes", excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .post(Entity.entity(resource, Constants.SCIM_CONTENT_TYPE));

      return response;
    }

    @Override
    public Response find(SearchRequest searchRequest) throws RestClientException {
      Response response = BaseScimClient.this.target
          .path(".search")
          .request(Constants.SCIM_CONTENT_TYPE)
          .post(Entity.entity(searchRequest, Constants.SCIM_CONTENT_TYPE));

      return response;
    }

    @Override
    public Response update(T resource, String id, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws RestClientException {
      Response response = BaseScimClient.this.target
          .path(id)
          .queryParam("attributes", attributes)
          .queryParam("excludedAttributes", excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .put(Entity.entity(resource, Constants.SCIM_CONTENT_TYPE));

      return response;
    }

    @Override
    public Response patch(PatchRequest patchRequest) throws RestClientException {
      Response response = BaseScimClient.this.target
          .request(Constants.SCIM_CONTENT_TYPE)
          .method("PATCH", Entity.entity(patchRequest, Constants.SCIM_CONTENT_TYPE));

      return response;
    }

    @Override
    public Response delete(String id) throws RestClientException {
      Response response = BaseScimClient.this.target
          .path(id)
          .request(Constants.SCIM_CONTENT_TYPE)
          .delete();

      return response;
    }
  }
}
