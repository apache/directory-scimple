package org.apache.directory.scim.test.helpers;

import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.helpers.builder.EnterpriseExtensionManagerBuilder;
import org.apache.directory.scim.test.helpers.builder.ScimUserPatchOperationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScimUserPatchOperationTest {
  Registry registry;

  @BeforeEach
  void setUp() throws Exception {
    this.registry = ScimTestHelper.createRegistry();
  }

  @Test
  void removeNameAttributePatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);

    modified.getName().setFamilyName(null);

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void removeEnterpriseExtensionPatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);

    modified.getExtensions().remove(EnterpriseExtension.URN);

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void replaceEnterpriseExtensionEmployeeNumberPatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);

    ((EnterpriseExtension) modified.getExtension(EnterpriseExtension.URN))
      .setEmployeeNumber(UUID.randomUUID().toString());

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void removeEnterpriseExtensionEmployeeNumberPatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);


    ((EnterpriseExtension) modified.getExtension(EnterpriseExtension.URN))
      .setEmployeeNumber(null);

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void addEnterpriseExtensionManagerDisplayNamePatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    ((EnterpriseExtension) original.getExtension(EnterpriseExtension.URN)).setManager(null);

    final ScimUser modified = SerializationUtils.clone(original);

    ((EnterpriseExtension) modified.getExtension(EnterpriseExtension.URN))
      .setManager(EnterpriseExtensionManagerBuilder.builder().displayName("** Display Name **").build());

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void replaceEnterpriseExtensionManagerDisplayNamePatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);

    ((EnterpriseExtension) modified.getExtension(EnterpriseExtension.URN))
      .getManager().setDisplayName("Manager Of People");

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }

  @Test
  void removeEnterpriseExtensionManagerDisplayNamePatchOperation() throws Exception {
    final ScimUser original = ScimTestHelper.generateScimUser();
    final ScimUser modified = SerializationUtils.clone(original);

    ((EnterpriseExtension) modified.getExtension(EnterpriseExtension.URN))
      .getManager().setDisplayName(null);

    ScimTestHelper.logPatchOperationBuilder(new ScimUserPatchOperationBuilder(registry, original, modified).createPatchOperations());
  }
}
