package org.apache.directory.scim.test.helpers.builder;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.resources.ScimGroup;

public class ScimGroupPatchOperationBuilder extends ScimTestPatchOperationHelper<ScimGroup> {
  ScimGroupPatchOperationBuilder(Registry registry, ScimGroup original, ScimGroup resource) {
    super(registry, original, resource);
  }
}
