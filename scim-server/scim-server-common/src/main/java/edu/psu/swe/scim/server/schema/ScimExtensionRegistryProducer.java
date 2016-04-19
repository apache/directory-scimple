package edu.psu.swe.scim.server.schema;

import javax.enterprise.inject.Produces;

import edu.psu.swe.scim.spec.extension.ScimExtensionRegistry;

public class ScimExtensionRegistryProducer {

  @Produces
  public ScimExtensionRegistry produceScimExtensionRegistry() {
    return ScimExtensionRegistry.getInstance();
  }
  
}
