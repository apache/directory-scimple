package edu.psu.swe.scim.server.rest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import edu.psu.swe.scim.server.configuration.ServerConfiguration;
import edu.psu.swe.scim.spec.protocol.ServiceProviderConfigResource;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

@Singleton
@ApplicationScoped
public class ServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {
  private static final List<String> SCHEMA_URN_LIST = Arrays.asList(ServiceProviderConfiguration.SCHEMA_URI);

  @Inject
  ServerConfiguration serverConfiguration;

  @Override
  public Response getServiceProviderConfiguration(HttpServletRequest request) {
    ServiceProviderConfiguration serviceProviderConfiguration = new ServiceProviderConfiguration();
    List<AuthenticationSchema> authenticationSchemas = serverConfiguration.getAuthenticationSchemas();
    BulkConfiguration bulk = serverConfiguration.getBulkConfiguration();
    SupportedConfiguration changePassword = serverConfiguration.getChangePasswordConfiguration();
    SupportedConfiguration etag = serverConfiguration.getEtagConfiguration();
    FilterConfiguration filter = serverConfiguration.getFilterConfiguration();
    SupportedConfiguration patch = serverConfiguration.getPatchConfiguration();
    SupportedConfiguration sort = serverConfiguration.getSortConfiguration();
    String documentationUrl = "<SERVICE_CONFIG_PROVIDER_CONFIGURATION_URL>";
    String externalId = "<EXTERNAL_ID>"; // TODO needed?
    String id = "<ID>"; // TODO needed?
    Meta meta = new Meta();
    String scheme = request.getScheme();
    String host = request.getServerName();
    int port = request.getLocalPort();
    String portString = port == 80 ? "" : ":" + port;
    String requestUri = request.getRequestURI();
    String location = String.format("%s://%s%s%s", scheme, host, portString, requestUri);
    String resourceType = "ServiceProviderConfig";
    String version = "<META_VERSION>";

    meta.setCreated(new Date());
    meta.setLastModified(new Date());
    meta.setLocation(location);
    meta.setResourceType(resourceType);
    meta.setVersion(version);
    serviceProviderConfiguration.setAuthenticationSchemes(authenticationSchemas);
    serviceProviderConfiguration.setBulk(bulk);
    serviceProviderConfiguration.setChangePassword(changePassword);
    serviceProviderConfiguration.setDocumentationUrl(documentationUrl);
    serviceProviderConfiguration.setEtag(etag);
    serviceProviderConfiguration.setExternalId(externalId);
    serviceProviderConfiguration.setFilter(filter);
    serviceProviderConfiguration.setId(id);
    serviceProviderConfiguration.setMeta(meta);
    serviceProviderConfiguration.setPatch(patch);
    serviceProviderConfiguration.setSchemaUrnList(SCHEMA_URN_LIST);
    serviceProviderConfiguration.setSort(sort);

    return Response.ok(serviceProviderConfiguration).build();
  }
}
