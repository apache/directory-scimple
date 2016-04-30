package edu.psu.swe.scim.server.rest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.psu.swe.scim.server.configuration.ServerConfiguration;
import edu.psu.swe.scim.spec.protocol.ServiceProviderConfigResource;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

@Stateless
public class ServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {
  private static final List<String> SCHEMA_URN_LIST = Arrays.asList(ServiceProviderConfiguration.SCHEMA_URI);

  @Inject
  ServerConfiguration serverConfiguration;

  public ServiceProviderConfigResourceImpl() {
    serverConfiguration = new ServerConfiguration();  
  }
  
  public void registerServerConfiguration(ServerConfiguration configuration) {
    serverConfiguration = configuration;  
  }
  
  @Override
  public Response getServiceProviderConfiguration(UriInfo uriInfo) {
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
    String location = uriInfo.getAbsolutePath().toString();
    String resourceType = "ServiceProviderConfig";
    String version = "<META_VERSION>";
    LocalDateTime now = LocalDateTime.now();

    meta.setCreated(now);
    meta.setLastModified(now);
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
