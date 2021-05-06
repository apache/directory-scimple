package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.resources.ScimUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScimUserPatchOperationBuilder extends ScimTestPatchOperationHelper<ScimUser> {
  public ScimUserPatchOperationBuilder(Registry registry, ScimUser original, ScimUser resource) {
    super(registry, original, resource);
  }
}
