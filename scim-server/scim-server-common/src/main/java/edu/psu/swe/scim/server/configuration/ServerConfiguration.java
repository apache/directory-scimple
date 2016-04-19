package edu.psu.swe.scim.server.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

@Data
@Named
@ApplicationScoped
public class ServerConfiguration {

  boolean supportsChangePassword = false;
  
  @Setter(AccessLevel.NONE)
  boolean supportsBulk = true;
  int bulkMaxOperations = 100;
  int bulkMaxPayloadSize = 1024;  //TODO what should this be?
  
  @Setter(AccessLevel.NONE)
  boolean supportsETag = true;
  
  boolean supportsFilter = false;
  int filterMaxResults = 100;
  
  boolean supportsPatch = false;
  
  boolean supportsSort = false;
  
  @Setter(AccessLevel.NONE)
  List<AuthenticationSchema> authenticationSchemas = new ArrayList<>();
  
  public List<AuthenticationSchema> getAuthenticationSchemas() {
    return Collections.unmodifiableList(authenticationSchemas);
  }
  
  public void addAuthenticationSchema(AuthenticationSchema authenticationSchema) {
    authenticationSchemas.add(authenticationSchema);
  }

  public SupportedConfiguration getChangePasswordConfiguration() {
    return createSupportedConfiguration(supportsChangePassword);
  }

  public BulkConfiguration getBulkConfiguration() {
    BulkConfiguration bulkConfiguration = new BulkConfiguration();

    bulkConfiguration.setSupported(supportsBulk);
    bulkConfiguration.setMaxOperations(bulkMaxOperations);
    bulkConfiguration.setMaxPayloadSize(bulkMaxPayloadSize);

    return bulkConfiguration;
  }

  public SupportedConfiguration getEtagConfiguration() {
    return createSupportedConfiguration(supportsETag);

  }

  public FilterConfiguration getFilterConfiguration() {
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.setSupported(supportsFilter);
    filterConfiguration.setMaxResults(filterMaxResults);
    return filterConfiguration;
  }

  public SupportedConfiguration getPatchConfiguration() {
    return createSupportedConfiguration(supportsPatch);
  }

  public SupportedConfiguration getSortConfiguration() {
    return createSupportedConfiguration(supportsSort);
  }
  
  private SupportedConfiguration createSupportedConfiguration(boolean supported) {
    SupportedConfiguration supportedConfiguration = new SupportedConfiguration();
    supportedConfiguration.setSupported(supported);
    return supportedConfiguration;
  }

}
