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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.JsonDiff;

import edu.psu.swe.scim.server.rest.ObjectMapperContextResolver;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import edu.psu.swe.scim.spec.protocol.data.PatchOperation.Type;
import edu.psu.swe.scim.spec.protocol.data.PatchOperationPath;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.ValueFilterExpression;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.resources.TypedAttribute;
import edu.psu.swe.scim.spec.schema.AttributeContainer;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
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
  @Getter
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

    this.original = original;
    this.resource = resource;

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

    try {
      return createPatchOperations();
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new IllegalStateException("Error creating the patch list", e);
    }
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
      } else if (attribute.getType() == Attribute.Type.COMPLEX) {
        sortMultiValuedCollections(field.get(t), attribute);
      }
    }
  }

  private T applyPatchOperations() {
    // TODO Auto-generated method stub
    return resource;
  }

  private List<PatchOperation> createPatchOperations() throws IllegalArgumentException, IllegalAccessException {

    sortMultiValuedCollections(this.original, schema);
    sortMultiValuedCollections(this.resource, schema);

    ObjectMapperContextResolver ctxResolver = new ObjectMapperContextResolver();
    ObjectMapper objMapper = ctxResolver.getContext(null); // TODO is there a
                                                           // better way?

    JsonNode node1 = objMapper.valueToTree(original);
    JsonNode node2 = objMapper.valueToTree(resource);
    JsonNode differences = JsonDiff.asJson(node1, node2);

    try {
      log.info("Differences: " + objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(differences));
    } catch (JsonProcessingException e) {
      log.info("Unable to debug differences: ", e);
    }

    List<PatchOperation> patchOps = convertToPatchOperations(differences);

    try {
      log.info("Patch Ops: " + objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(patchOps));
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

  List<PatchOperation> convertToPatchOperations(JsonNode node) throws IllegalArgumentException, IllegalAccessException {
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

      PatchOperation operation = convertToPatchOperation(operationNode.asText(), pathNode.asText(), valueNode);
      if (operation != null) {
        operations.add(operation);
      }
    }
    return operations;

  }

  private PatchOperation convertToPatchOperation(String operationNode, String diffPath, JsonNode valueNode) throws IllegalArgumentException, IllegalAccessException {

    PatchOperation.Type patchOpType = PatchOperation.Type.valueOf(operationNode.toUpperCase());

    if (diffPath == null || diffPath.length() < 1) {
      return null;
    }

    ParseData parseData = new ParseData(diffPath);

    if (parseData.pathParts.isEmpty()) {
      return handleExtensions(valueNode, patchOpType, parseData);
    } else {
      return handleAttributes(valueNode, patchOpType, parseData);
    }
  }

  private PatchOperation handleExtensions(JsonNode valueNode, Type patchOpType, ParseData parseData) {
    PatchOperation operation = new PatchOperation();
    operation.setOperation(patchOpType);
    AttributeReference attributeReference = new AttributeReference(parseData.pathUri, null);
    PatchOperationPath patchOperationPath = new PatchOperationPath();
    patchOperationPath.setAttributeReference(attributeReference);
    operation.setPath(patchOperationPath);
    operation.setValue(determineValue(patchOpType, valueNode, parseData));
    return operation;
  }

  private PatchOperation handleAttributes(JsonNode valueNode, PatchOperation.Type patchOpType, ParseData parseData) throws IllegalAccessException {
    List<String> attributeReferenceList = new ArrayList<>();
    ValueFilterExpression valueFilterExpression = null;
    List<String> subAttributes = new ArrayList<>();

    boolean processingMultiValued = false;
    boolean processedMultiValued = false;
    boolean done = false;

    int i = 0;
    for (String pathPart : parseData.pathParts) {
      if (done) {
        throw new RuntimeException("Path should be done... Attribute not supported by the schema: " + pathPart);
      } else if (processingMultiValued) {
        parseData.traverseObjectsInArray(pathPart);

        if (parseData.isLastIndex(i) && patchOpType == PatchOperation.Type.ADD) {
          break;
        }

        if (parseData.originalObject instanceof TypedAttribute) {
          TypedAttribute typedAttribute = (TypedAttribute) parseData.originalObject;
          String type = typedAttribute.getType();
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("type"), CompareOperator.EQ, type);
        } else {
          log.warn("Attribute: {} doesn't implement TypedAttribute, can't create ValueFilterExpression", parseData.originalObject.getClass());
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("type"), CompareOperator.EQ, "?");
        }
        processingMultiValued = false;
        processedMultiValued = true;
      } else {
        Attribute attribute = parseData.ac.getAttribute(pathPart);
        if (attribute == null) {
          // throw new RuntimeException("Attribute not supported by the schema:
          // " + pathPart);
          break;
        }

        if (processedMultiValued) {
          subAttributes.add(pathPart);
        } else {
          attributeReferenceList.add(pathPart);
        }

        parseData.traverseObjects(pathPart, attribute);

        if (attribute.isMultiValued()) {
          processingMultiValued = true;
        } else if (attribute.getType() != Attribute.Type.COMPLEX) {
          done = true;
        }
      }
      ++i;
    }

    PatchOperation operation = new PatchOperation();
    operation.setOperation(patchOpType);
    if (!attributeReferenceList.isEmpty()) {
      AttributeReference attributeReference = new AttributeReference(parseData.pathUri, attributeReferenceList.stream().collect(Collectors.joining(".")));
      PatchOperationPath patchOperationPath = new PatchOperationPath();
      patchOperationPath.setAttributeReference(attributeReference);
      patchOperationPath.setValueFilterExpression(valueFilterExpression);
      patchOperationPath.setSubAttributes(subAttributes.isEmpty() ? null : subAttributes.toArray(new String[subAttributes.size()]));

      operation.setPath(patchOperationPath);
      operation.setValue(determineValue(patchOpType, valueNode, parseData));

      return operation;
    } else {
      return null;
    }
  }

  private Object determineValue(PatchOperation.Type patchOpType, JsonNode valueNode, ParseData parseData) {
    if (patchOpType == PatchOperation.Type.REMOVE) {
      return null;
    }

    if (valueNode != null) {
      if (valueNode instanceof TextNode) {
        return valueNode.asText();
      } else if (valueNode instanceof BooleanNode) {
        return valueNode.asBoolean();
      } else if (valueNode instanceof DoubleNode || valueNode instanceof FloatNode) {
        return valueNode.asDouble();
      } else if (valueNode instanceof IntNode) {
        return valueNode.asInt();
      } else if (valueNode instanceof NullNode) {
        return null;
      } else if (valueNode instanceof ObjectNode) {
        return parseData.resourceObject;
      } else if (valueNode instanceof POJONode) {
        POJONode pojoNode = (POJONode) valueNode;
        return pojoNode.getPojo();
      } else if (valueNode instanceof ArrayNode) {
        ArrayNode arrayNode = (ArrayNode) valueNode;
        List<Object> objectList = new ArrayList<Object>();
        for(int i = 0; i < arrayNode.size(); i++) {
          Object subObject = determineValue(patchOpType, arrayNode.get(i), parseData);
          if (subObject != null) {
            objectList.add(subObject);
          }
        }
        return objectList;
      }
    }
    return null;
  }

  private class ParseData {

    List<String> pathParts;
    Object originalObject;
    Object resourceObject;
    AttributeContainer ac;
    String pathUri;

    public ParseData(String diffPath) {
      String path = diffPath.substring(1);
      pathParts = new ArrayList<>(Arrays.asList(path.split("/")));

      // Extract namespace
      pathUri = null;

      String firstPathPart = pathParts.get(0);
      if (firstPathPart.contains(":")) {
        pathUri = firstPathPart;
        pathParts.remove(0);
      }

      if (pathUri != null) {
        ac = registry.getSchema(pathUri);
        originalObject = original.getExtension(pathUri);
        resourceObject = resource.getExtension(pathUri);
      } else {
        ac = schema;
        originalObject = original;
        resourceObject = resource;
      }
    }

    public void traverseObjects(String pathPart, Attribute attribute) throws IllegalArgumentException, IllegalAccessException {
      originalObject = lookupAttribute(originalObject, ac, pathPart);
      resourceObject = lookupAttribute(resourceObject, ac, pathPart);
      ac = attribute;
    }

    public void traverseObjectsInArray(String pathPart) {
      int index = Integer.parseInt(pathPart);

      originalObject = lookupIndexInArray(originalObject, index);
      resourceObject = lookupIndexInArray(resourceObject, index);
    }

    public boolean isLastIndex(int index) {
      int numPathParts = pathParts.size();
      return index == (numPathParts - 1);
    }

    @SuppressWarnings("rawtypes")
    private Object lookupIndexInArray(Object object, int index) {
      if (!(object instanceof List)) {
        throw new RuntimeException("Unsupported collection type: " + object.getClass());
      }
      List list = (List) object;
      if (index >= list.size()) {
        return null;
      }

      return list.get(index);
    }

    private Object lookupAttribute(Object object, AttributeContainer ac, String attributeName) throws IllegalArgumentException, IllegalAccessException {
      if (object == null) {
        return null;
      }

      Attribute attribute = ac.getAttribute(attributeName);
      Field field = attribute.getField();
      return field.get(object);
    }
  }

}
