package edu.psu.swe.scim.memory.configuration;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import edu.psu.swe.scim.server.configuration.ServerConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema.Type;

@Singleton
@ApplicationScoped
public class InMemoryServerConfiguration implements ServerConfiguration {
  @Override
  public List<AuthenticationSchema> getAuthenticationSchemas() {
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

    return Arrays.asList(authenticationSchema);
  }
}
