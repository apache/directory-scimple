package edu.psu.swe.scim.client.rest;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import edu.psu.swe.commons.jaxrs.exceptions.BackingStoreChangedException;
import edu.psu.swe.commons.jaxrs.exceptions.ConflictingDataException;
import edu.psu.swe.commons.jaxrs.exceptions.RestClientException;
import edu.psu.swe.commons.jaxrs.exceptions.RestServerException;
import edu.psu.swe.commons.jaxrs.exceptions.ServiceAuthException;
import edu.psu.swe.commons.jaxrs.utilities.RestClientUtil;
import edu.psu.swe.scim.spec.protocol.ResourceTypesResource;
import edu.psu.swe.scim.spec.schema.ResourceType;

public class ResourceTypesClient implements AutoCloseable {

  private static final GenericType<List<ResourceType>> LIST_RESOURCE_TYPE = new GenericType<List<ResourceType>>(){};

  private final Client client;
  private final WebTarget target;
  private final ResourceTypesResourceClient resourceTypesResourceClient = new ResourceTypesResourceClient();

  public ResourceTypesClient(Client client, String baseUrl) {
    this.client = client;
    this.target = this.client.target(baseUrl).path("ResourceTypes");
  }

  public List<ResourceType> getAllResourceTypes(String filter) throws RestClientException, RestServerException, BackingStoreChangedException, ConflictingDataException, ServiceAuthException {
    List<ResourceType> resourceTypes;
    Response response = this.resourceTypesResourceClient.getAllResourceTypes(filter);

    try {
      RestClientUtil.checkForSuccess(response);

      resourceTypes = response.readEntity(LIST_RESOURCE_TYPE);
    } finally {
      RestClientUtil.close(response);
    }
    return resourceTypes;
  }

  public Optional<ResourceType> getResourceType(String name) throws RestClientException, ProcessingException, IllegalStateException, RestServerException, BackingStoreChangedException, ConflictingDataException, ServiceAuthException {
    Optional<ResourceType> resourceType;
    Response response = this.resourceTypesResourceClient.getResourceType(name);

    try {
      resourceType = RestClientUtil.readEntity(response, ResourceType.class);
    } finally {
      RestClientUtil.close(response);
    }
    return resourceType;
  }

  @Override
  public void close() throws Exception {
    this.client.close();
  }

  private class ResourceTypesResourceClient implements ResourceTypesResource {

    @Override
    public Response getAllResourceTypes(String filter) throws RestClientException {
      Response response = ResourceTypesClient.this.target
          .queryParam("filter", filter)
          .request("application/scim+json")
          .get();

      return response;
    }

    @Override
    public Response getResourceType(String name) throws RestClientException {
      Response response = ResourceTypesClient.this.target
          .path(name)
          .request("application/scim+json")
          .get();

      return response;
    }
  }
}
