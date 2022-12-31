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
import org.apache.directory.scim.spec.resources.KeyedResource;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    final ValuePathExpression valuePathExpression = valuePathExpression(patchOperation);
    final AttributeReference attributeReference = attributeReference(valuePathExpression);

    Schema baseSchema = this.schemaRegistry.getSchema(source.getBaseUrn());
    Attribute attribute = baseSchema.getAttribute(attributeReference.getAttributeName());

    Object attributeObject = attribute.getAccessor().get(source);
    if (attributeObject == null && patchOperation.getOperation().equals(PatchOperation.Type.REPLACE)) {
      throw new IllegalArgumentException("Cannot apply patch replace on missing property: " + attribute.getName());
    }

    if (valuePathExpression.getAttributeExpression() != null && attributeObject instanceof Collection<?>) {
      // apply expression filter
      Collection<Object> items = (Collection<Object>) attributeObject;
      Collection<Object> updatedCollection = items.stream().map(item -> {
        KeyedResource keyedResource = (KeyedResource) item;
        Map<String, Object> keyedResourceAsMap = objectAsMap(item);
        Predicate<KeyedResource> pred = FilterExpressions.inMemory(valuePathExpression.getAttributeExpression(), baseSchema);
        if (pred.test(keyedResource)) {
          String subAttributeName = valuePathExpression.getAttributePath().getSubAttributeName();
          if (keyedResourceAsMap.get(subAttributeName) == null && patchOperation.getOperation().equals(PatchOperation.Type.REPLACE)) {
            throw new IllegalArgumentException("Cannot apply patch replace on missing property: " + valuePathExpression.toFilter());
          }
          keyedResourceAsMap.put(subAttributeName, patchOperation.getValue());
          return keyedResourceAsMap;
        } else {
          // filter does not apply
          return item;
        }
      }).collect(Collectors.toList());
      sourceAsMap.put(attribute.getName(), updatedCollection);
    } else {
      // no filter expression
      sourceAsMap.put(attribute.getName(), patchOperation.getValue());
    }
    return (T) mapAsScimResource(sourceAsMap, source.getClass());
  }

  private PatchOperationPath tryGetOperationPath(String key) {
    try {
      return new PatchOperationPath(key);
    } catch (FilterParseException e) {
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

  public static ValuePathExpression valuePathExpression(final PatchOperation operation) {
    return Optional.ofNullable(operation.getPath())
      .map(PatchOperationPath::getValuePathExpression)
      .orElseThrow(() -> new IllegalArgumentException("Patch operation must have a value path expression"));
  }

  public static AttributeReference attributeReference(final ValuePathExpression expression) {
    return Optional.ofNullable(expression.getAttributePath())
      .orElseThrow(() -> new IllegalArgumentException("Patch operation must have an expression with a valid attribute path"));
  }

}
