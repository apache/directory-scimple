package edu.psu.swe.scim.server.configuration;

import java.util.Collections;
import java.util.List;

import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import edu.psu.swe.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

public interface ServerConfiguration {
  static SupportedConfiguration defaultValue() {
    SupportedConfiguration supportedConfiguration = new SupportedConfiguration();

    supportedConfiguration.setSupported(false);

    return supportedConfiguration;
  }

  default List<AuthenticationSchema> getAuthenticationSchemas() {
    List<AuthenticationSchema> authenticationSchemas = Collections.emptyList();

    return authenticationSchemas;
  }

  default SupportedConfiguration getChangePasswordConfiguration() {
    SupportedConfiguration supportsChangePassword = defaultValue();

    return supportsChangePassword;
  }

  default BulkConfiguration getBulkConfiguration() {
    BulkConfiguration bulkConfiguration = new BulkConfiguration();

    bulkConfiguration.setSupported(false);
    bulkConfiguration.setMaxOperations(0);
    bulkConfiguration.setMaxPayloadSize(0);

    return bulkConfiguration;
  }

  default SupportedConfiguration getEtagConfiguration() {
    SupportedConfiguration supportsEtag = defaultValue();

    return supportsEtag;
  }

  default FilterConfiguration getFilterConfiguration() {
    FilterConfiguration filterConfiguration = new FilterConfiguration();

    filterConfiguration.setSupported(false);
    filterConfiguration.setMaxResults(0);

    return filterConfiguration;
  }

  default SupportedConfiguration getPatchConfiguration() {
    SupportedConfiguration supportsPatch = defaultValue();

    return supportsPatch;
  }

  default SupportedConfiguration getSortConfiguration() {
    SupportedConfiguration supportsSort = defaultValue();

    return supportsSort;
  }
}
