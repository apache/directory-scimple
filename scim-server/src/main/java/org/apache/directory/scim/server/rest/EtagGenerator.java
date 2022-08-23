/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.server.rest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.directory.scim.spec.json.ObjectMapperFactory;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Meta;

import jakarta.ws.rs.core.EntityTag;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ApplicationScoped
public class EtagGenerator {

  private final ObjectMapper objectMapper;

  public EtagGenerator() {
    objectMapper = ObjectMapperFactory.getObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public EntityTag generateEtag(ScimResource resource) throws JsonProcessingException, NoSuchAlgorithmException, UnsupportedEncodingException {

    Meta meta = resource.getMeta();

    if (meta == null) {
      meta = new Meta();
    }

    resource.setMeta(null);
    String writeValueAsString = objectMapper.writeValueAsString(resource);

    EntityTag etag = hash(writeValueAsString);
    meta.setVersion(etag.getValue());

    resource.setMeta(meta);

    return etag;
  }
  
  private static EntityTag hash(String input) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(input.getBytes(StandardCharsets.UTF_8));
    byte[] hash = digest.digest();
    return new EntityTag(Base64.getEncoder().encodeToString(hash));
  }
}
