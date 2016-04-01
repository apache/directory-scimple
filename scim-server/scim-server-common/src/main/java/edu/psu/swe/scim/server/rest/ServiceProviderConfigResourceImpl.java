package edu.psu.swe.scim.server.rest;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.spec.protocol.ServiceProviderConfigResource;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.Type;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

public class ServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {
  @Override
  public Response getServiceProviderConfiguration() {
    ServiceProviderConfiguration serviceProviderConfiguration = new ServiceProviderConfiguration();
    AuthenticationSchema authenticationSchema = new AuthenticationSchema();
    String description = "<AUTHENTICATION_SCHEMA_DESCRIPTION>";
    String authenticationSchemaDocumentationUri = "<AUTHENTICATION_SCHEMA_DOCUMENTATION_URI>";
    String authenticationSchemaName = "<AUTHENTICATION_SCHEMA_NAME>";
    String specUri = "<AUTHENTICATION_SCHEMA_URI>";
    Type authenticationSchemaType = Type.OAUTH2;

    authenticationSchema.setDescription(description);
    authenticationSchema.setDocumentationUri(authenticationSchemaDocumentationUri);
    authenticationSchema.setName(authenticationSchemaName);
    authenticationSchema.setSpecUri(specUri);
    authenticationSchema.setType(authenticationSchemaType);

    List<AuthenticationSchema> authenticationSchemas = Arrays.asList(authenticationSchema);
    BulkConfiguration bulk = new BulkConfiguration();
    SupportedConfiguration changePassword = new SupportedConfiguration();
    String documentationUrl = "<SERVICE_CONFIG_PROVIDER_CONFIGURATION_URL>";
    SupportedConfiguration etag = new SupportedConfiguration();
    Map<String, ScimExtension> extensions = new HashMap<>(); // TODO needed?
    String externalId = "<EXTERNAL_ID>"; // TODO needed?
    FilterConfiguration filter = new FilterConfiguration();
    String id = "<ID>"; // TODO needed?
    Meta meta = new Meta(); // TODO needed?
    String location = "<META_LOCATION>";
    String resourceType = "<META_RESOURCE_TYPE>";
    String version = "<META_VERSION>";
    SupportedConfiguration patch = new SupportedConfiguration();
    List<String> schemaUrnList = Arrays.asList("<SCHEMA_URNS>"); // TODO needed?
    SupportedConfiguration sort = new SupportedConfiguration();

    bulk.setMaxOperations(0);
    bulk.setMaxPayloadSize(0);
    bulk.setSupported(false);
    changePassword.setSupported(false);
    etag.setSupported(false);
    filter.setMaxResults(0);
    filter.setSupported(false);
    meta.setCreated(new Date());
    meta.setLastModified(new Date());
    meta.setLocation(location);
    meta.setResourceType(resourceType);
    meta.setVersion(version);
    patch.setSupported(false);
    sort.setSupported(false);
    serviceProviderConfiguration.setAuthenticationSchemes(authenticationSchemas);
    serviceProviderConfiguration.setBulk(bulk);
    serviceProviderConfiguration.setChangePassword(changePassword);
    serviceProviderConfiguration.setDocumentationUrl(documentationUrl);
    serviceProviderConfiguration.setEtag(etag);
    serviceProviderConfiguration.setExtensions(extensions);
    serviceProviderConfiguration.setExternalId(externalId);
    serviceProviderConfiguration.setFilter(filter);
    serviceProviderConfiguration.setId(id);
    serviceProviderConfiguration.setMeta(meta);
    serviceProviderConfiguration.setPatch(patch);
    serviceProviderConfiguration.setSchemaUrnList(schemaUrnList);
    serviceProviderConfiguration.setSort(sort);

    return Response.status(Status.OK).entity(serviceProviderConfiguration).build();
  }
}
