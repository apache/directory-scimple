package edu.psu.swe.scim.server.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import edu.psu.swe.scim.spec.id.ScimResourceId;

public class ScimResourceIdDeserializer extends JsonDeserializer<ScimResourceId> {
  @Override
  public ScimResourceId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    ScimResourceId resourceId;
    JsonLocation location = jsonParser.getCurrentLocation();
    String id = jsonParser.readValueAs(String.class);

    if (id != null) {
      resourceId = new ScimResourceId(id);
    } else {
      throw new JsonParseException("Expecting JSON String", location);
    }
    return resourceId;
  }
}
