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

package org.apache.directory.scim.spec.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.directory.scim.spec.schema.Schema.Attribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class SchemaTest {

  static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();;
  Validator validator;

  @BeforeEach
  public void setUp() {
    validator = validatorFactory.getValidator();
  }

  /**
   * Tests that the schemas published in the SCIM schema specification are valid
   * when unmarshalled into the equivalent classes developed from those
   * specifications.
   * 
   * @param schemaFileName
   *          the name of the resource on the classpath that contains the JSON
   *          representation of the schema.
   */
  @ParameterizedTest
  @ValueSource( strings = {
      "schemas/urn:ietf:params:scim:schemas:core:2.0:User.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:Group.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:ResourceType.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:Schema.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig.json",
      "schemas/urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.json"
  })
  public void testUnmarshallingProvidedSchemas(String schemaFileName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(schemaFileName);

    ObjectMapper objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);

    AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);

    // Unmarshall the JSON document to a Schema and its associated object graph.
    Schema schema = null;
    try {
      schema = objectMapper.readValue(inputStream, Schema.class);
      assertNotNull(schema);
    } catch (Throwable t) {
      fail("Unexpected Throwable was caught while unmarshalling JSON: " + t.getLocalizedMessage());
    }

    // Validate the unmarshalled object graph to confirm the JSON
    // representations meet the schema specification.
    if (schema != null) {
      Set<ConstraintViolation<Schema>> schemaViolations = validator.validate(schema);
      assertTrue(schemaViolations.isEmpty());

      for (Attribute attribute : schema.getAttributes()) {
        Set<ConstraintViolation<Attribute>> attributeViolations = validator.validate(attribute);
        assertTrue(attributeViolations.isEmpty());
      }

      Meta meta = schema.getMeta();
      if (meta != null) {
        Set<ConstraintViolation<Meta>> metaViolations = validator.validate(meta);
        assertTrue(metaViolations.isEmpty());
      }
    }
  }

  @Test
  @Disabled
  public void testGetId() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testSetId() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testGetName() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testSetName() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testGetDescription() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testSetDescription() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testGetAttributes() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  public void testSetAttributes() {
    fail("Not yet implemented");
  }

}
