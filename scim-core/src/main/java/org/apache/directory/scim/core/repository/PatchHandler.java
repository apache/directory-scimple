package org.apache.directory.scim.core.repository;

import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimResource;

import java.util.List;

public interface PatchHandler {

  <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations);

}
