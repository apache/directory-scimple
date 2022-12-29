package org.apache.directory.scim.core.repository;

import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schemas;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

public class PatchHandlerTest {

  SchemaRegistry mockSchemaRegistry = mock(SchemaRegistry.class);

  @Test
  public void applyReplaceUserName() throws FilterParseException {
    PatchOperation op = new PatchOperation();
    op.setOperation(PatchOperation.Type.REPLACE);
    op.setPath(new PatchOperationPath("userName"));
    op.setValue("testUser_updated");
    ScimUser updatedUser = performPatch(op);
    assertThat(updatedUser.getUserName()).isEqualTo("testUser_updated");
  }

  @Test
  public void applyReplaceUserNameWithMappedValue() {
    PatchOperation op = new PatchOperation();
    op.setOperation(PatchOperation.Type.REPLACE);
    op.setValue(Map.ofEntries(entry("userName", "testUser_updated")));
    ScimUser updatedUser = performPatch(op);
    assertThat(updatedUser.getUserName()).isEqualTo("testUser_updated");
  }

  private ScimUser performPatch(PatchOperation op) {
    when(mockSchemaRegistry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(Schemas.schemaFor(ScimUser.class));
    PatchHandlerImpl patchHandler = new PatchHandlerImpl(mockSchemaRegistry);
    ScimUser user = new ScimUser();
    user.setUserName("testUser");
    return patchHandler.apply(user, List.of(op));
  }
}
