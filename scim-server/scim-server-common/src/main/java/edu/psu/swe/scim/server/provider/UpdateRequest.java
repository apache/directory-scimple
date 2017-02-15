package edu.psu.swe.scim.server.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.JsonDiff;

import edu.psu.swe.scim.server.rest.ObjectMapperContextResolver;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import edu.psu.swe.scim.spec.protocol.data.PatchOperationPath;
import edu.psu.swe.scim.spec.protocol.data.PatchValue;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.ValueFilterExpression;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.resources.TypedAttribute;
import edu.psu.swe.scim.spec.schema.AttributeContainer;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Named
@Slf4j
@EqualsAndHashCode
@ToString
public class UpdateRequest<T extends ScimResource> {

  private static final String OPERATION = "op";
  private static final String PATH = "path";
  private static final String VALUE = "value";

  @Getter
  private String id;
  private T resource;
  private T original;
  private List<PatchOperation> patchOperations;
  private boolean initialized = false;

  private Schema schema;

  private Registry registry;

  @Inject
  public UpdateRequest(Registry registry) {
    this.registry = registry;
  }

  public void initWithResource(String id, T original, T resource) {
    this.id = id;
    schema = registry.getSchema(original.getBaseUrn());

    try {
      this.original = original;
      this.resource = resource;
      sortMultiValuedCollections(this.original, schema);
      sortMultiValuedCollections(this.resource, schema);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.warn("Unable to sort the collections within the ScimResource, Skipping sort", e);
    }

    initialized = true;
  }

  public void initWithPatch(String id, T original, List<PatchOperation> patchOperations) {
    this.id = id;
    this.original = original;
    this.patchOperations = patchOperations;
    schema = registry.getSchema(original.getBaseUrn());

    initialized = true;
  }

  public T getResource() {
    if (!initialized) {
      throw new IllegalStateException("UpdateRequest was not initialized");
    }

    if (resource != null) {
      return resource;
    }

    return applyPatchOperations();
  }

  public List<PatchOperation> getPatchOperations() {
    if (!initialized) {
      throw new IllegalStateException("UpdateRequest was not initialized");
    }

    if (patchOperations != null) {
      return patchOperations;
    }

    return createPatchOperations();
  }

  private void sortMultiValuedCollections(Object t, AttributeContainer ac) throws IllegalArgumentException, IllegalAccessException {
    for (Attribute attribute : ac.getAttributes()) {
      Field field = attribute.getField();
      if (attribute.isMultiValued()) {
        @SuppressWarnings("unchecked")
        List<Object> collection = (List<Object>) field.get(t);
        if (collection != null) {
          Collections.sort(collection, (o1, o2) -> {
            if (o1 instanceof TypedAttribute && o2 instanceof TypedAttribute) {
              TypedAttribute t1 = (TypedAttribute) o1;
              TypedAttribute t2 = (TypedAttribute) o2;
              String type1 = t1.getType();
              String type2 = t2.getType();
              if (type1 == null) {
                return -1;
              }
              if (type2 == null) {
                return 1;
              }
              return type1.compareTo(type2);
            }
            return 0;
          });
        }
      } else if (attribute.getType() == Type.COMPLEX) {
        sortMultiValuedCollections(field.get(t), attribute);
      }
    }
  }

  private T applyPatchOperations() {
    // TODO Auto-generated method stub
    return original;
  }

  private List<PatchOperation> createPatchOperations() {
    ObjectMapperContextResolver ctxResolver = new ObjectMapperContextResolver();
    ObjectMapper objMapper = ctxResolver.getContext(null); // TODO is there a
                                                           // better way?

    JsonNode node1 = objMapper.valueToTree(original);
    JsonNode node2 = objMapper.valueToTree(resource);
    JsonNode differences = JsonDiff.asJson(node1, node2);

    try {
      log.info("Differences: " + objMapper.writerWithDefaultPrettyPrinter()
                                          .writeValueAsString(differences));
    } catch (JsonProcessingException e) {
      log.info("Unable to debug differences: ", e);
    }

    List<PatchOperation> patchOps = convertToPatch(differences);

    try {
      log.info("Patch Ops: " + objMapper.writerWithDefaultPrettyPrinter()
                                        .writeValueAsString(patchOps));
    } catch (JsonProcessingException e) {
      log.info("Unable to debug patch ops: ", e);
    }

    return patchOps;
  }

  JsonNode compareUsers(ScimUser user1, ScimUser user2) {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node1 = mapper.valueToTree(user1);
    JsonNode node2 = mapper.valueToTree(user2);
    return JsonDiff.asJson(node1, node2);
  }

  List<PatchOperation> convertToPatch(JsonNode node) {
    List<PatchOperation> operations = new ArrayList<>();
    if (node == null) {
      return Collections.emptyList();
    }

    if (!(node instanceof ArrayNode)) {
      throw new RuntimeException("Expecting an instance of a ArrayNode, but got: " + node.getClass());
    }

    ArrayNode root = (ArrayNode) node;
    for (int i = 0; i < root.size(); i++) {
      ObjectNode patchNode = (ObjectNode) root.get(i);
      JsonNode operationNode = patchNode.get(OPERATION);
      JsonNode pathNode = patchNode.get(PATH);
      JsonNode valueNode = patchNode.get(VALUE);

      PatchOperation operation = new PatchOperation();
      operation.setOpreration(PatchOperation.Type.valueOf(operationNode.asText()
                                                                       .toUpperCase()));
      operation.setPath(convertPath(pathNode.asText()));
      if (valueNode != null) {
        PatchValue patchValue = new PatchValue();
        if (valueNode instanceof TextNode) {
          patchValue.setValue(valueNode.asText());
        } else {
          patchValue.setValue(valueNode.toString());
        }
        operation.setValue(patchValue);
      }
      operations.add(operation);
    }
    return operations;
  }

  private PatchOperationPath convertPath(final String diffPath) {
    PatchOperationPath patchOperationPath = new PatchOperationPath();
    if (diffPath == null || diffPath.length() < 1) {
      return null;
    }

    String path = diffPath.substring(1);
    List<String> pathParts = new ArrayList<>(Arrays.asList(path.split("/")));

    // Extract namespace
    String pathUri = null;

    String firstPathPart = pathParts.get(0);
    if (firstPathPart.contains(":")) {
      pathUri = firstPathPart;
      pathParts.remove(0);
    }

    AttributeContainer ac;
    if (pathUri != null) {
      ac = registry.getSchema(pathUri);
    } else {
      ac = schema;
    }

    List<String> attributeReferenceList = new ArrayList<>();
    ValueFilterExpression valueFilterExpression = null;
    List<String> subAttributes = new ArrayList<>();

    boolean processingMultiValued = false;
    boolean processedMultiValued = false;
    boolean done = false;

    for (String pathPart : pathParts) {
      if (done) {
        throw new RuntimeException("Path should be done... Attribute not supported by the schema: " + pathPart);
      } else if (processingMultiValued) {
        // TODO generate value path expression
        AttributeComparisonExpression ace = new AttributeComparisonExpression(new AttributeReference("field"), CompareOperator.EQ, "value");
        valueFilterExpression = ace;
        processingMultiValued = false;
        processedMultiValued = true;
      } else {
        Attribute attribute = ac.getAttribute(pathPart);
        if (attribute == null) {
          throw new RuntimeException("Attribute not supported by the schema: " + pathPart);
        }

        if (processedMultiValued) {
          subAttributes.add(pathPart);
        } else {
          attributeReferenceList.add(pathPart);
        }

        if (attribute.isMultiValued()) {
          ac = attribute;
          processingMultiValued = true;
        } else {
          done = true;
        }

      }
    }

    patchOperationPath.setAttributeReference(new AttributeReference(pathUri, attributeReferenceList.stream()
                                                                                                   .collect(Collectors.joining("."))));
    patchOperationPath.setValueFilterExpression(valueFilterExpression);
    patchOperationPath.setSubAttributes(subAttributes.isEmpty() ? null : subAttributes.toArray(new String[subAttributes.size()]));

    return patchOperationPath;
  }

}
