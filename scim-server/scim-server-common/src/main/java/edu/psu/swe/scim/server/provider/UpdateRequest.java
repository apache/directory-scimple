package edu.psu.swe.scim.server.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import edu.psu.swe.scim.spec.protocol.data.PatchValue;
import edu.psu.swe.scim.spec.resources.ScimUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Named
@Slf4j
@EqualsAndHashCode
@ToString
public class UpdateRequest<T> {
  
  @Inject
  private Registry registry;

  private static final String OPERATION = "op";
  private static final String PATH = "path";
  private static final String VALUE = "value";

  @Getter
  private String id;
  private T resource;
  private T original;
  private List<PatchOperation> patchOperations;
  private boolean initialized = false;

  public void initWithResource(String id, T original, T resource) {
    this.id = id;
    this.original = original;
    this.resource = resource;
    initialized = true;
  }

  public void initWithPatch(String id, T original, List<PatchOperation> patchOperations) {
    this.id = id;
    this.original = original;
    this.patchOperations = patchOperations;
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

  private T applyPatchOperations() {
    // TODO Auto-generated method stub
    return original;
  }

  private List<PatchOperation> createPatchOperations() {
    ObjectMapperContextResolver ctxResolver = new ObjectMapperContextResolver();
    ObjectMapper objMapper = ctxResolver.getContext(null);  // TODO is there a better way?
    
    JsonNode node1 = objMapper.valueToTree(original);
    JsonNode node2 = objMapper.valueToTree(resource);
    JsonNode differences = JsonDiff.asJson(node1, node2);
    
    try {
      log.info("Differences: " + objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(differences));
    } catch (JsonProcessingException e) {
      log.info("Unable to debug differences: ", e);
    }
    
    List<PatchOperation> patchOps = convertToPatch(differences);
    
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

  private String convertPath(final String diffPath) {
    if (diffPath == null || diffPath.length() < 1) {
      return null;
    }
    
    String path = diffPath.substring(1);
    path = path.replace("/", ".");
    return path;
  }

}
