package edu.psu.swe.scim.server.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.psu.swe.scim.spec.id.ScimResourceId;

public class ScimResourceIdSerializer extends JsonSerializer<ScimResourceId> {
  @Override
  public void serialize(ScimResourceId scimResourceId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
    String idValue = scimResourceId.getValue();

    serializerProvider.defaultSerializeValue(idValue, jsonGenerator);
  }
}
