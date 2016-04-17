package edu.psu.swe.scim.server.configuration;

import java.util.Collections;
import java.util.List;

import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

//public interface ServerConfiguration {
public class ServerConfiguration {

  static SupportedConfiguration defaultValue() {
    SupportedConfiguration supportedConfiguration = new SupportedConfiguration();

    supportedConfiguration.setSupported(false);

    return supportedConfiguration;
  }

  public List<AuthenticationSchema> getAuthenticationSchemas() {
    List<AuthenticationSchema> authenticationSchemas = Collections.emptyList();

    return authenticationSchemas;
  }

  public SupportedConfiguration getChangePasswordConfiguration() {
    SupportedConfiguration supportsChangePassword = defaultValue();

    return supportsChangePassword;
  }

  public BulkConfiguration getBulkConfiguration() {
    BulkConfiguration bulkConfiguration = new BulkConfiguration();

    bulkConfiguration.setSupported(false);
    bulkConfiguration.setMaxOperations(0);
    bulkConfiguration.setMaxPayloadSize(0);

    return bulkConfiguration;
  }

  public SupportedConfiguration getEtagConfiguration() {
    SupportedConfiguration supportsEtag = defaultValue();

    return supportsEtag;
  }

  public FilterConfiguration getFilterConfiguration() {
    FilterConfiguration filterConfiguration = new FilterConfiguration();

    filterConfiguration.setSupported(false);
    filterConfiguration.setMaxResults(0);

    return filterConfiguration;
  }

  public SupportedConfiguration getPatchConfiguration() {
    SupportedConfiguration supportsPatch = defaultValue();

    return supportsPatch;
  }

  public SupportedConfiguration getSortConfiguration() {
    SupportedConfiguration supportsSort = defaultValue();

    return supportsSort;
  }
}
