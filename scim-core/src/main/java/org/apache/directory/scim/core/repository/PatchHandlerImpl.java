package org.apache.directory.scim.core.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.filter.*;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.patch.PatchOperationPath;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.*;

import static org.apache.directory.scim.core.repository.ObjectMapperProvider.createObjectMapper;

@SuppressWarnings("unchecked")
@Slf4j
public class PatchHandlerImpl implements PatchHandler {

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  private final ObjectMapper objectMapper = createObjectMapper();

  private final SchemaRegistry schemaRegistry;

  public PatchHandlerImpl(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public <T extends ScimResource> T apply(final T original, final List<PatchOperation> patchOperations) {
    if (original == null) {
      throw new IllegalArgumentException("Original resource is null. Cannot apply patch.");
    }
    if (patchOperations == null) {
      throw new IllegalArgumentException("patchOperations is null. Cannot apply patch.");
    }

    T updatedScimResource;
    updatedScimResource = SerializationUtils.clone(original);
    for (PatchOperation patchOperation : patchOperations) {
      if (patchOperation.getPath() == null) {
        if (!(patchOperation.getValue() instanceof Map)) {
          throw new IllegalArgumentException("Cannot apply patch. value is required");
        }
        Map<String, Object> properties = (Map<String, Object>) patchOperation.getValue();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
          // convert SCIM patch to RFC-6902 patch
          PatchOperation newPatchOperation = new PatchOperation();
          newPatchOperation.setOperation(patchOperation.getOperation());
          newPatchOperation.setPath(tryGetOperationPath(entry.getKey()));
          newPatchOperation.setValue(entry.getValue());

          updatedScimResource = apply(updatedScimResource, newPatchOperation);
        }
      } else {
        updatedScimResource = apply(updatedScimResource, patchOperation);
      }

    }
    return updatedScimResource;
  }

  private <T extends ScimResource> T apply(final T source, final PatchOperation patchOperation) {
    Map<String, Object> sourceAsMap = objectAsMap(source);

    final AttributeReference attributeReference = attributeReference(patchOperation);
    Schema baseSchema = this.schemaRegistry.getSchema(source.getBaseUrn());
    Attribute attribute = baseSchema.getAttribute(attributeReference.getAttributeName());

    sourceAsMap.put(attribute.getName(), patchOperation.getValue());

    return (T) mapAsScimResource(sourceAsMap, source.getClass());
  }

  private PatchOperationPath tryGetOperationPath(String key) {
    try {
      return new PatchOperationPath(key);
    }
    catch (FilterParseException e) {
      log.warn("Parsing path failed with exception.", e);
      throw new IllegalArgumentException("Cannot parse path expression: " + e.getMessage());
    }
  }

  private <T extends ScimResource> T mapAsScimResource(final Map<String, Object> scimResourceAsMap, final Class<T> clazz) {
    return objectMapper.convertValue(scimResourceAsMap, clazz);
  }

  private Map<String, Object> objectAsMap(final Object object) {
    return objectMapper.convertValue(object, MAP_TYPE);
  }

  public static AttributeReference attributeReference(final PatchOperation operation) {
    return Optional.ofNullable(operation.getPath())
      .map(PatchOperationPath::getValuePathExpression)
      .map(ValuePathExpression::getAttributePath)
      .orElseThrow(() -> new IllegalArgumentException("Patch operation must have a value path expression"));
  }

}
