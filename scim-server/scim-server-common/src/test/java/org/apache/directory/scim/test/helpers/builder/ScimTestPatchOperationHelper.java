package org.apache.directory.scim.test.helpers.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

import org.apache.directory.scim.server.provider.PrioritySortingComparitor;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.protocol.filter.CompareOperator;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.ValuePathExpression;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.TypedAttribute;
import org.apache.directory.scim.spec.schema.AttributeContainer;
import org.apache.directory.scim.spec.schema.Schema;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ScimTestPatchOperationHelper<T extends ScimResource> {

  private static final String OPERATION = "op";
  private static final String PATH = "path";
  private static final String VALUE = "value";
  private final Map<Schema.Attribute, Integer> addRemoveOffsetMap = new HashMap<>();
  private final Registry registry;
  private final T original;
  private final T resource;
  private final Schema schema;

  ScimTestPatchOperationHelper(final Registry registry, T original, T resource) {
    this.registry = registry;
    this.original = original;
    this.resource = resource;

    this.schema = this.registry.getSchema(original.getBaseUrn());
  }

  /**
   * There is a know issue with the diffing tool that the tool will attempt to move empty arrays. By
   * nulling out the empty arrays during comparison, this will prevent that error from occurring. Because
   * deleting requires the parent node
   *
   * @param node Parent node.
   */
  private static void nullEmptyLists(JsonNode node) {
    List<String> objectsToDelete = new ArrayList<>();

    if (node!=null) {
      Iterator<Map.Entry<String, JsonNode>> children = node.fields();
      while (children.hasNext()) {
        Map.Entry<String, JsonNode> child = children.next();
        String name = child.getKey();
        JsonNode childNode = child.getValue();

        //Attempt to delete children before analyzing
        if (childNode.isContainerNode()) {
          nullEmptyLists(childNode);
        }

        if (childNode instanceof ArrayNode) {
          ArrayNode ar = (ArrayNode) childNode;
          if (ar.size()==0) {
            objectsToDelete.add(name);
          }
        }
      }

      if (!objectsToDelete.isEmpty() && node instanceof ObjectNode) {
        ObjectNode on = (ObjectNode) node;
        for (String name : objectsToDelete) {
          on.putNull(name);
        }
      }
    }
  }

  public List<PatchOperation> createPatchOperations()
    throws IllegalArgumentException, IllegalAccessException {
    sortMultiValuedCollections(this.original, this.resource, this.schema);
    Map<String, ScimExtension> originalExtensions = this.original.getExtensions();
    Map<String, ScimExtension> resourceExtensions = this.resource.getExtensions();
    Set<String> keys = new HashSet<>();
    keys.addAll(originalExtensions.keySet());
    keys.addAll(resourceExtensions.keySet());

    for (String key : keys) {
      Schema extSchema = this.registry.getSchema(key);
      ScimExtension originalExtension = originalExtensions.get(key);
      ScimExtension resourceExtension = resourceExtensions.get(key);
      sortMultiValuedCollections(originalExtension, resourceExtension, extSchema);
    }

    //Create a Jackson ObjectMapper that reads JaxB annotations
    ObjectMapper objMapper = new ObjectMapperFactory(registry).createObjectMapper();

    JsonNode node1 = objMapper.valueToTree(this.original);
    nullEmptyLists(node1);
    JsonNode node2 = objMapper.valueToTree(this.resource);
    nullEmptyLists(node2);
    JsonNode differences = JsonDiff.asJson(node1, node2);

    return convertToPatchOperations(differences);
  }

  List<PatchOperation> convertToPatchOperations(JsonNode node) throws IllegalArgumentException, IllegalAccessException {
    List<PatchOperation> operations = new ArrayList<>();
    if (node==null) {
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

      List<PatchOperation> nodeOperations = convertNodeToPatchOperations(operationNode.asText(), pathNode.asText(), valueNode);
      if (!nodeOperations.isEmpty()) {
        operations.addAll(nodeOperations);
      }
    }

    return operations;
  }

  private List<PatchOperation> convertNodeToPatchOperations(String operationNode, String diffPath, JsonNode valueNode) throws IllegalArgumentException, IllegalAccessException {
    log.info(operationNode + ", " + diffPath);
    List<PatchOperation> operations = new ArrayList<>();
    PatchOperation.Type patchOpType = PatchOperation.Type.valueOf(operationNode.toUpperCase());

    if (diffPath!=null && diffPath.length() >= 1) {
      ParseData parseData = new ParseData(diffPath);

      if (parseData.pathParts.isEmpty()) {
        operations.add(handleExtensions(valueNode, patchOpType, parseData));
      } else {
        operations.addAll(handleAttributes(valueNode, patchOpType, parseData));
      }
    }

    return operations;
  }

  private PatchOperation handleExtensions(JsonNode valueNode, PatchOperation.Type patchOpType, ParseData parseData) {
    PatchOperation operation = new PatchOperation();
    operation.setOperation(patchOpType);

    AttributeReference attributeReference = new AttributeReference(parseData.pathUri, null);
    PatchOperationPath patchOperationPath = new PatchOperationPath();
    ValuePathExpression valuePathExpression = new ValuePathExpression(attributeReference);
    patchOperationPath.setValuePathExpression(valuePathExpression);

    operation.setPath(patchOperationPath);
    operation.setValue(determineValue(patchOpType, valueNode, parseData));

    return operation;
  }

  @SuppressWarnings("unchecked")
  private List<PatchOperation> handleAttributes(JsonNode valueNode, PatchOperation.Type patchOpType, ParseData parseData) throws IllegalAccessException {
    log.info("in handleAttributes");
    List<PatchOperation> operations = new ArrayList<>();

    List<String> attributeReferenceList = new ArrayList<>();
    FilterExpression valueFilterExpression = null;
    List<String> subAttributes = new ArrayList<>();

    boolean processingMultiValued = false;
    boolean processedMultiValued = false;
    boolean done = false;

    int i = 0;
    for (String pathPart : parseData.pathParts) {
      log.info(pathPart);
      if (done) {
        throw new RuntimeException("Path should be done... Attribute not supported by the schema: " + pathPart);
      } else if (processingMultiValued) {
        parseData.traverseObjectsInArray(pathPart, patchOpType);

        if (!parseData.isLastIndex(i) || patchOpType!=PatchOperation.Type.ADD) {
          if (parseData.originalObject instanceof TypedAttribute) {
            TypedAttribute typedAttribute = (TypedAttribute) parseData.originalObject;
            String type = typedAttribute.getType();
            valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("type"), CompareOperator.EQ, type);
          } else if (parseData.originalObject instanceof String || parseData.originalObject instanceof Number) {
            String toString = parseData.originalObject.toString();
            valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, toString);
          } else if (parseData.originalObject instanceof Enum) {
            Enum<?> tempEnum = (Enum<?>) parseData.originalObject;
            valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, tempEnum.name());
          } else {
            log.info("Attribute: {} doesn't implement TypedAttribute, can't create ValueFilterExpression", parseData.originalObject.getClass());
            valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, "?");
          }
          processingMultiValued = false;
          processedMultiValued = true;
        }
      } else {
        Schema.Attribute attribute = parseData.ac.getAttribute(pathPart);

        if (attribute!=null) {
          if (processedMultiValued) {
            subAttributes.add(pathPart);
          } else {
            log.info("Adding " + pathPart + " to attributeReferenceList");
            attributeReferenceList.add(pathPart);
          }

          parseData.traverseObjects(pathPart, attribute);

          if (patchOpType==PatchOperation.Type.REPLACE &&
            (parseData.resourceObject instanceof Collection && !((Collection<?>) parseData.resourceObject).isEmpty()) &&
            (parseData.originalObject==null ||
              (parseData.originalObject instanceof Collection && ((Collection<?>) parseData.originalObject).isEmpty()))) {
            patchOpType = PatchOperation.Type.ADD;
          }

          if (attribute.isMultiValued()) {
            processingMultiValued = true;
          } else if (attribute.getType()!=Schema.Attribute.Type.COMPLEX) {
            done = true;
          }
        }
      }
      ++i;
    }

    if (patchOpType==PatchOperation.Type.REPLACE && (parseData.resourceObject==null ||
      (parseData.resourceObject instanceof Collection && ((Collection<?>) parseData.resourceObject).isEmpty()))) {
      patchOpType = PatchOperation.Type.REMOVE;
      valueNode = null;
    }

    if (patchOpType==PatchOperation.Type.REPLACE && parseData.originalObject==null) {
      patchOpType = PatchOperation.Type.ADD;
    }

    if (!attributeReferenceList.isEmpty()) {
      Object value = determineValue(patchOpType, valueNode, parseData);

      if (value instanceof ArrayList) {
        List<Object> objList = (List<Object>) value;

        if (!objList.isEmpty()) {
          Object firstElement = objList.get(0);
          if (firstElement instanceof ArrayList) {
            objList = (List<Object>) firstElement;
          }

          for (Object obj : objList) {
            PatchOperation operation = buildPatchOperation(patchOpType, parseData, attributeReferenceList, valueFilterExpression, subAttributes, obj);
            operations.add(operation);
          }
        }
      } else {
        PatchOperation operation = buildPatchOperation(patchOpType, parseData, attributeReferenceList, valueFilterExpression, subAttributes, value);
        operations.add(operation);
      }
    }

    return operations;
  }

  private PatchOperation buildPatchOperation(PatchOperation.Type patchOpType, ParseData parseData, List<String> attributeReferenceList,
                                             FilterExpression valueFilterExpression, List<String> subAttributes, Object value) {
    PatchOperation operation = new PatchOperation();
    operation.setOperation(patchOpType);
    String attribute = attributeReferenceList.get(0);
    String subAttribute = attributeReferenceList.size() > 1 ? attributeReferenceList.get(1):null;

    if (subAttribute==null && !subAttributes.isEmpty()) {
      subAttribute = subAttributes.get(0);
    }
    AttributeReference attributeReference = new AttributeReference(parseData.pathUri, attribute, subAttribute);
    PatchOperationPath patchOperationPath = new PatchOperationPath();
    ValuePathExpression valuePathExpression = new ValuePathExpression(attributeReference, valueFilterExpression);
    patchOperationPath.setValuePathExpression(valuePathExpression);

    operation.setPath(patchOperationPath);
    operation.setValue(value);

    return operation;
  }

  private Object determineValue(PatchOperation.Type patchOpType, JsonNode valueNode, ParseData parseData) {
    if (patchOpType==PatchOperation.Type.REMOVE) {
      return null;
    }

    if (valueNode!=null) {
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
        List<Object> objectList = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
          Object subObject = determineValue(patchOpType, arrayNode.get(i), parseData);
          if (subObject!=null) {
            objectList.add(subObject);
          }
        }
        return objectList;
      }
    }
    return null;
  }

  @SuppressWarnings("Java8ListSort")
  private void sortMultiValuedCollections(Object obj1, Object obj2, AttributeContainer ac) throws IllegalArgumentException, IllegalAccessException {
    for (Schema.Attribute attribute : ac.getAttributes()) {
      Field field = attribute.getField();
      if (attribute.isMultiValued()) {
        @SuppressWarnings("unchecked")
        List<Object> collection1 = obj1!=null ? (List<Object>) field.get(obj1):null;
        @SuppressWarnings("unchecked")
        List<Object> collection2 = obj2!=null ? (List<Object>) field.get(obj2):null;

        Set<Object> priorities = findCommonElements(collection1, collection2);
        PrioritySortingComparitor prioritySortingComparitor = new PrioritySortingComparitor(priorities);
        if (collection1!=null) {
          Collections.sort(collection1, prioritySortingComparitor);
        }

        if (collection2!=null) {
          Collections.sort(collection2, prioritySortingComparitor);
        }
      } else if (attribute.getType()==Schema.Attribute.Type.COMPLEX) {
        Object nextObj1 = obj1!=null ? field.get(obj1):null;
        Object nextObj2 = obj2!=null ? field.get(obj2):null;
        sortMultiValuedCollections(nextObj1, nextObj2, attribute);
      }
    }
  }

  private Set<Object> findCommonElements(List<Object> list1, List<Object> list2) {
    if (list1==null || list2==null) {
      return Collections.emptySet();
    }

    Set<Object> set1 = new HashSet<>(list1);
    Set<Object> set2 = new HashSet<>(list2);

    set1 = set1.stream().map(PrioritySortingComparitor::getComparableValue).collect(Collectors.toSet());
    set2 = set2.stream().map(PrioritySortingComparitor::getComparableValue).collect(Collectors.toSet());

    set1.retainAll(set2);
    return set1;
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

      if (pathUri!=null) {
        ac = registry.getSchema(pathUri);
        originalObject = original.getExtension(pathUri);
        resourceObject = resource.getExtension(pathUri);
      } else {
        ac = schema;
        originalObject = original;
        resourceObject = resource;
      }
    }

    public void traverseObjects(String pathPart, Schema.Attribute attribute) throws IllegalArgumentException, IllegalAccessException {
      originalObject = lookupAttribute(originalObject, ac, pathPart);
      resourceObject = lookupAttribute(resourceObject, ac, pathPart);
      ac = attribute;
    }

    public void traverseObjectsInArray(String pathPart, PatchOperation.Type patchOpType) {
      int index = Integer.parseInt(pathPart);

      Schema.Attribute attr = (Schema.Attribute) ac;

      Integer addRemoveOffset = addRemoveOffsetMap.getOrDefault(attr, 0);
      switch (patchOpType) {
        case ADD:
          addRemoveOffsetMap.put(attr, addRemoveOffset - 1);
          break;
        case REMOVE:
          addRemoveOffsetMap.put(attr, addRemoveOffset + 1);
          break;
        case REPLACE:
        default:
          // Do Nothing
          break;
      }

      int newIndex = index + addRemoveOffset;
      if (newIndex < 0) {
        log.error("Attempting to retrieve a negative index:{} on pathPath: {}", newIndex, pathPart);
      }

      originalObject = lookupIndexInArray(originalObject, newIndex);
      resourceObject = lookupIndexInArray(resourceObject, index);
    }

    public boolean isLastIndex(int index) {
      int numPathParts = pathParts.size();
      return index==(numPathParts - 1);
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
      if (object==null) {
        return null;
      }

      Schema.Attribute attribute = ac.getAttribute(attributeName);
      Field field = attribute.getField();
      return field.get(object);
    }
  }
}
