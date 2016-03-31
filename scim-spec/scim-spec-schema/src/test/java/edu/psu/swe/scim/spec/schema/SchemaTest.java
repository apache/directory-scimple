package edu.psu.swe.scim.spec.schema;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.psu.swe.scim.spec.schema.Attribute;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.Schema;

@RunWith(JUnitParamsRunner.class)
public class SchemaTest {

  static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();;
  Validator validator;

  @Before
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
  @Test
  @Parameters({
      "schemas/urn:ietf:params:scim:schemas:core:2.0:User.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:Group.json",
      "schemas/urn:ietf:params:scim:schemas:core:2.0:ResourceType.json",
      "schemas/schema-schema.json",
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

  @Ignore
  @Test
  public void testGetId() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testSetId() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetName() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testSetName() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetDescription() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testSetDescription() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetAttributes() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testSetAttributes() {
    fail("Not yet implemented");
  }

}
