package edu.psu.swe.scim.server.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
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
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.resources.TypedAttribute;
import edu.psu.swe.scim.spec.schema.AttributeContainer;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;

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

  private Map<Attribute, Integer> addRemoveOffsetMap = new HashMap<>();
  
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
    
    if (patchOperations == null) {
      try {
        patchOperations = createPatchOperations(); 
      } catch (IllegalArgumentException | IllegalAccessException | JsonProcessingException e) {
        throw new IllegalStateException("Error creating the patch list", e);
      }
    }
    
    return patchOperations;
  }

  private void sortMultiValuedCollections(Object t, AttributeContainer ac) throws IllegalArgumentException, IllegalAccessException {
    if (t != null) {
      for (Attribute attribute : ac.getAttributes()) {
        Field field = attribute.getField();
        if (attribute.isMultiValued()) {
          @SuppressWarnings("unchecked")
          List<Object> collection = (List<Object>) field.get(t);
          if (collection != null) {
            Collections.sort(collection, (o1, o2) -> {
              if (o1 == null) {
                return -1;
              }
              if (o2 == null) {
                return 1;
              }
              if (o1 instanceof TypedAttribute && o2 instanceof TypedAttribute) {
                TypedAttribute t1 = (TypedAttribute) o1;
                TypedAttribute t2 = (TypedAttribute) o2;
                String type1 = t1.getType();
                String type2 = t2.getType();
                return type1.compareTo(type2);
              }
              if (o1 instanceof Comparable<?>) {
                Comparable c1 = (Comparable)o1;
                Comparable c2 = (Comparable)o2;
                return c1.compareTo(c2);
              }
              return 0;
            });
          }
        } else if (attribute.getType() == Attribute.Type.COMPLEX) {
          sortMultiValuedCollections(field.get(t), attribute);
        }
      }
    }
  }

  private T applyPatchOperations() {
    // TODO Auto-generated method stub
    return resource;
  }
  
  /**
   * There is a know issue with the diffing tool that the tool will attempt to move empty arrays. By
   * nulling out the empty arrays during comparison, this will prevent that error from occurring. Because
   * deleting requires the parent node
   * @param node Parent node.
   */
  private static void nullEmptyLists(JsonNode node) {
    List<String> objectsToDelete = new ArrayList<>();
    
    if (node != null) {
      Iterator<Map.Entry<String, JsonNode>> children = node.fields();
      while(children.hasNext()) {
        Map.Entry<String, JsonNode> child = children.next();
        String name = child.getKey();
        JsonNode childNode = child.getValue();
        
        //Attempt to delete children before analyzing 
        if (childNode.isContainerNode()) {
          nullEmptyLists(childNode);
        }
        
        if (childNode != null && childNode instanceof ArrayNode) {
          ArrayNode ar = (ArrayNode)childNode;
          if (ar.size() == 0) {
            objectsToDelete.add(name);
          }
        }
      }
      
      if (!objectsToDelete.isEmpty()) {
        if (node instanceof ObjectNode) {
          ObjectNode on = (ObjectNode)node;
          for(String name : objectsToDelete) {
            on.putNull(name);
          }
        }
      }
    }
  }

  private List<PatchOperation> createPatchOperations() throws IllegalArgumentException, IllegalAccessException, JsonProcessingException {

    sortMultiValuedCollections(this.original, schema);
    Map<String, ScimExtension> extensions = this.original.getExtensions();
    for(Map.Entry<String, ScimExtension> entry : extensions.entrySet()) {
      Schema extSchema = registry.getSchema(entry.getKey());
      sortMultiValuedCollections(entry.getValue(), extSchema);
    }
    sortMultiValuedCollections(this.resource, schema);
    extensions = this.resource.getExtensions();
    for(Map.Entry<String, ScimExtension> entry : extensions.entrySet()) {
      Schema extSchema = registry.getSchema(entry.getKey());
      sortMultiValuedCollections(entry.getValue(), extSchema);
    }

    //Create a Jackson ObjectMapper that reads JaxB annotations
    ObjectMapper objMapper = new ObjectMapper();
    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objMapper.registerModule(jaxbAnnotationModule);
    objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(objMapper.getTypeFactory());
    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
    AnnotationIntrospector pair = new AnnotationIntrospectorPair(jacksonIntrospector, jaxbIntrospector);
    objMapper.setAnnotationIntrospector(pair);
    
    JsonNode node1 = objMapper.valueToTree(original);
    nullEmptyLists(node1);
    JsonNode node2 = objMapper.valueToTree(resource);
    nullEmptyLists(node2);
    JsonNode differences = JsonDiff.asJson(node1, node2);
    
    
    /*
    Commenting out debug statement to prevent PII from appearing in log
    ObjectWriter writer = objMapper.writerWithDefaultPrettyPrinter();
    try {
      log.debug("Original: "+writer.writeValueAsString(node1));
      log.debug("Resource: "+writer.writeValueAsString(node2));
    } catch (IOException e) {
      
    }*/

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

  List<PatchOperation> convertToPatchOperations(JsonNode node) throws IllegalArgumentException, IllegalAccessException, JsonProcessingException {
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

  private PatchOperation convertToPatchOperation(String operationNode, String diffPath, JsonNode valueNode) throws IllegalArgumentException, IllegalAccessException, JsonProcessingException {

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

  private PatchOperation handleExtensions(JsonNode valueNode, Type patchOpType, ParseData parseData) throws JsonProcessingException {
    PatchOperation operation = new PatchOperation();
    operation.setOperation(patchOpType);
    AttributeReference attributeReference = new AttributeReference(parseData.pathUri, null);
    PatchOperationPath patchOperationPath = new PatchOperationPath();
    patchOperationPath.setAttributeReference(attributeReference);
    operation.setPath(patchOperationPath);
    operation.setValue(determineValue(patchOpType, valueNode, parseData));
    return operation;
  }

  private PatchOperation handleAttributes(JsonNode valueNode, PatchOperation.Type patchOpType, ParseData parseData) throws IllegalAccessException, JsonProcessingException {
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
        parseData.traverseObjectsInArray(pathPart, patchOpType);

        if (parseData.isLastIndex(i) && patchOpType == PatchOperation.Type.ADD) {
          break;
        }

        if (parseData.originalObject instanceof TypedAttribute) {
          TypedAttribute typedAttribute = (TypedAttribute) parseData.originalObject;
          String type = typedAttribute.getType();
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("type"), CompareOperator.EQ, type);
        } else if (parseData.originalObject instanceof String || parseData.originalObject instanceof Number) {
          String toString = parseData.originalObject.toString();
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, toString);
        } else if(parseData.originalObject instanceof Enum) {
          Enum<?> tempEnum = (Enum<?>)parseData.originalObject;
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, tempEnum.name());
        } else {
          log.info("Attribute: {} doesn't implement TypedAttribute, can't create ValueFilterExpression", parseData.originalObject.getClass());
          valueFilterExpression = new AttributeComparisonExpression(new AttributeReference("value"), CompareOperator.EQ, "?");
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
    
    if (patchOpType == Type.REPLACE && (parseData.resourceObject == null || 
        (parseData.resourceObject instanceof Collection && ((Collection<?>)parseData.resourceObject).isEmpty()))) {
      patchOpType = Type.REMOVE;
      valueNode = null;
    }
    
    if (patchOpType == Type.REPLACE && parseData.originalObject == null) {
      patchOpType = Type.ADD;
      //valueNode = null;
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
  
  private Class<?> getClassOfResource(ParseData parseData) {
    if (parseData.originalObject != null) {
      return parseData.originalObject.getClass();
    }
    if (parseData.resourceObject != null) {
      return parseData.resourceObject.getClass();
    }
    
    return null;
  }

  private Object determineValue(PatchOperation.Type patchOpType, JsonNode valueNode, ParseData parseData) throws JsonProcessingException {
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
        ObjectNode objNode = (ObjectNode)valueNode;
        ObjectMapperContextResolver ctxResolver = new ObjectMapperContextResolver();
        ObjectMapper objMapper = ctxResolver.getContext(null);
        return objMapper.treeToValue(objNode, getClassOfResource(parseData));
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

    public void traverseObjectsInArray(String pathPart, Type patchOpType) {
      int index = Integer.parseInt(pathPart);

      Attribute attr = (Attribute) ac;
      
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
      
      int newindex = index + addRemoveOffset;
      if (newindex < 0) {
        log.error("Attempting to retrieve a negative index:{} on pathPath: {}", newindex, pathPart);
      }
      
      originalObject = lookupIndexInArray(originalObject, newindex);
      resourceObject = lookupIndexInArray(resourceObject, newindex);
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
