package edu.psu.swe.scim.client.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import edu.psu.swe.commons.jaxrs.RestCall;
import edu.psu.swe.commons.jaxrs.exceptions.RestClientException;
import edu.psu.swe.scim.spec.protocol.Constants;
import edu.psu.swe.scim.spec.protocol.SelfResource;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.exception.ScimException;
import edu.psu.swe.scim.spec.resources.ScimUser;

// purposefully does not extend BaseScimClient, has a different utility than other clients

public class ScimSelfClient implements AutoCloseable {

  private Client client;
  private WebTarget target;
  private SelfResourceClient selfResourceClient;
  private RestCall invoke = Invocation::invoke;

  public ScimSelfClient(Client client, String baseUrl) {
    this.client = client;
    this.target = this.client.target(SelfResource.PATH);
    this.selfResourceClient = new SelfResourceClient();
  }

  public ScimSelfClient(Client client, String baseUrl, RestCall invoke) {
    this(client, baseUrl);

    this.invoke = invoke;
  }

  public ScimUser getSelf(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    ScimUser self;
    Response response = this.selfResourceClient.getSelf(attributes, excludedAttributes);
    self = BaseScimClient.handleResponse(response, ScimUser.class, response::readEntity);

    return self;
  }

  public ScimUser getSelf() throws ScimException {
    ScimUser self = this.getSelf(null, null);

    return self;
  }

  public void updateSelf(ScimUser scimUser, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.selfResourceClient.update(scimUser, attributes, excludedAttributes);

    BaseScimClient.handleResponse(response);
  }

  public void updateSelf(ScimUser scimUser) throws ScimException {
    this.updateSelf(scimUser, null, null);
  }

  public void patchSelf(PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
    Response response = this.selfResourceClient.patch(patchRequest, attributes, excludedAttributes);

    BaseScimClient.handleResponse(response);
  }

  public void patchSelf(PatchRequest patchRequest) throws ScimException {
    this.patchSelf(patchRequest, null, null);
  }

  public void deleteSelf() throws ScimException {
    Response response = this.selfResourceClient.delete();

    BaseScimClient.handleResponse(response);
  }

  public RestCall getInvoke() {
    return this.invoke;
  }

  public void setInvoke(RestCall invoke) {
    this.invoke = invoke;
  }

  @Override
  public void close() throws Exception {
    this.client.close();
  }

  private class SelfResourceClient implements SelfResource {

    @Override
    public Response getSelf(AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException {
      Response response;
      Invocation request = ScimSelfClient.this.target
          .queryParam(BaseScimClient.ATTRIBUTES_QUERY_PARAM, attributes)
          .queryParam(BaseScimClient.EXCLUDED_ATTRIBUTES_QUERY_PARAM, excludedAttributes)
          .request(Constants.SCIM_CONTENT_TYPE)
          .buildGet();

      try {
        response = ScimSelfClient.this.invoke.apply(request);
      } catch (RestClientException restClientException) {
        throw BaseScimClient.toScimException(restClientException);
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
      } catch (RestClientException restClientException) {
        throw BaseScimClient.toScimException(restClientException);
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
      } catch (RestClientException restClientException) {
        throw BaseScimClient.toScimException(restClientException);
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
      } catch (RestClientException restClientException) {
        throw BaseScimClient.toScimException(restClientException);
      }
      return response;
    }
  }
}
