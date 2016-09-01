package edu.psu.swe.scim.tools.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class LintTest {
  
  Lint lint;
  
  @Before
  public void setUp() {
    lint = new Lint();
  }

  @Test
  @Ignore // TODO - Change the file names to match those provided by the
          //        scim-spec-schema module and figure out why some schemas
          //        don't have meta attributes.
  @Parameters({
    "schemas/user-schema.json",
    "schemas/group-schema.json",
    "schemas/resource-type-schema.json",
    "schemas/schema-schema.json",
    "schemas/service-provider-configuration-schema.json",
    "schemas/enterprise-user-schema.json"
  })
  public void testConvertWithSchemas(String schemaFileName) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(schemaFileName);

    try {
      JsonNode jsonNode = lint.convert(inputStream);
      assertTrue(jsonNode.isObject());
      assertFalse(lint.hasSchemas(jsonNode));
      assertTrue(lint.isSchema(jsonNode));
    } catch (JsonProcessingException e) {
      fail();
    }
  }

  @Test
  @Parameters({
    "examples/enterprise-user-example.json",
    "examples/full-user-example.json",
    "examples/group-example.json",
    "examples/minimal-user-example.json",
    "examples/resource-type-group-example.json",
    "examples/resource-type-user-example.json"
  })
  public void testConvertWithExamples(String exampleFileName) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(exampleFileName);

    try {
      JsonNode jsonNode = lint.convert(inputStream);
      assertTrue(jsonNode.isObject());
      assertTrue(lint.hasSchemas(jsonNode));
    } catch (JsonProcessingException e) {
      fail();
    }
  }

}
