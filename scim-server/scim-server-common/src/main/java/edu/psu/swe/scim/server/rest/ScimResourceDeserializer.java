package edu.psu.swe.scim.server.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.resources.ScimResource;

public class ScimResourceDeserializer extends JsonDeserializer<ScimResource> {
  private final Registry registry;
  private final ObjectMapper objectMapper;

  public ScimResourceDeserializer(Registry registry, ObjectMapper objectMapper) {
    this.registry = registry;
    this.objectMapper = objectMapper;
  }

  @Override
  public ScimResource deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    ScimResource scimResource;
    JsonLocation location = jsonParser.getCurrentLocation();
    TreeNode node = jsonParser.getCodec().readTree(jsonParser);
    ArrayNode schemas = (ArrayNode) node.get("schemas");
    Class<? extends ScimResource> scimResourceClass = null;

    for (JsonNode schemaUrnNode : schemas) {
      String schemaUrn = schemaUrnNode.textValue();
      scimResourceClass = registry.findScimResourceClass(schemaUrn);

      if (scimResourceClass != null) {
        break;
      }
    }
    if (scimResourceClass == null) {
      throw new JsonParseException("Could not find a valid schema in: " + schemas + ", valid schemas are: " + registry.getAllSchemaUrns(), location);
    }
    scimResource = objectMapper.readValue(node.toString(), scimResourceClass);

    return scimResource;
  }
}
