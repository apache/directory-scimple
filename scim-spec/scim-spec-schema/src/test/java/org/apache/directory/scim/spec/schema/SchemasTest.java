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

import org.apache.directory.scim.spec.AllSchemaTypesExtension;
import org.apache.directory.scim.spec.ComplexTypeExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SchemasTest {

  private Schema schema = Schemas.schemaForExtension(AllSchemaTypesExtension.class);

  @Test
  public void string1Attribute() {
    assertThat(schema.getAttribute("string1"))
      .hasName("string1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.STRING)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("One String")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void string2Attribute() {
    assertThat(schema.getAttribute("string2"))
      .hasName("string2")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.STRING)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(true)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void stringListAttribute() {
    assertThat(schema.getAttribute("stringList1"))
      .hasName("stringList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.STRING)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void booleanAttribute_required() {
    assertThat(schema.getAttribute("boolean1"))
      .hasName("boolean1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.BOOLEAN)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(true)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void booleanListAttribute_serverUnique() {
    assertThat(schema.getAttribute("booleanList1"))
      .hasName("booleanList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.BOOLEAN)
      .hasUniqueness(Schema.Attribute.Uniqueness.SERVER)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void boolean2Attribute() {
    assertThat(schema.getAttribute("boolean2"))
      .hasName("boolean2")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.BOOLEAN)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(true)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void booleanArray1Attribute() {
    assertThat(schema.getAttribute("booleanArray1"))
      .hasName("booleanArray1")
      .hasMutability(Schema.Attribute.Mutability.IMMUTABLE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.BOOLEAN)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void decimal1Attribute() {
    assertThat(schema.getAttribute("decimal1"))
      .hasName("decimal1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DECIMAL)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void decimal2Attribute() {
    assertThat(schema.getAttribute("decimal2"))
      .hasName("decimal2")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DECIMAL)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(true)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void decimalList1Attribute() {
    assertThat(schema.getAttribute("decimalList1"))
      .hasName("decimalList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DECIMAL)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void decimalArray1Attribute() {
    assertThat(schema.getAttribute("decimalArray1"))
      .hasName("decimalArray1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DECIMAL)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void integer1Attribute() {
    assertThat(schema.getAttribute("integer1"))
      .hasName("integer1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.INTEGER)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void integer2Attribute() {
    assertThat(schema.getAttribute("integer2"))
      .hasName("integer2")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.INTEGER)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(true)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void integerList1Attribute() {
    assertThat(schema.getAttribute("integerList1"))
      .hasName("integerList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.INTEGER)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void integerArray1Attribute() {
    assertThat(schema.getAttribute("integerArray1"))
      .hasName("integerArray1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.INTEGER)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void date1Attribute() {
    assertThat(schema.getAttribute("date1"))
      .hasName("date1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void dateList1Attribute() {
    assertThat(schema.getAttribute("dateList1"))
      .hasName("dateList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void dateArray1Attribute() {
    assertThat(schema.getAttribute("dateArray1"))
      .hasName("dateArray1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void instant1Attribute() {
    assertThat(schema.getAttribute("instant1"))
      .hasName("instant1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void instantList1Attribute() {
    assertThat(schema.getAttribute("instantList1"))
      .hasName("instantList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void instantArray1Attribute() {
    assertThat(schema.getAttribute("instantArray1"))
      .hasName("instantArray1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.DATE_TIME)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void binary1Attribute() {
    assertThat(schema.getAttribute("binary1"))
      .hasName("binary1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.BINARY)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null);
  }

  @Test
  public void ref1Attribute() {
    assertThat(schema.getAttribute("ref1"))
      .hasName("ref1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.REFERENCE)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(List.of("one", "two"));
  }

  @Test
  public void ref2Attribute() {
    assertThat(schema.getAttribute("$ref"))
      .hasName("$ref")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.REFERENCE)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(List.of("three", "four"));
  }

  @Test
  public void refList1Attribute() {
    assertThat(schema.getAttribute("refList1"))
      .hasName("refList1")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.REFERENCE)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(true)
      .hasDescription("")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(List.of("one", "two", "three"));
  }

  @Test
  public void complexTypeSubAttributesNotDuplicated() {
    Schema schema = Schemas.schemaForExtension(ComplexTypeExtension.class);
    Schema.Attribute complexType = schema.getAttribute("complexType");

    Assertions.assertThat((complexType.subAttributes.size())).isEqualTo(1);

    Schema.Attribute firstAttribute = new Schema.Attribute();
    firstAttribute.setName("firstAttribute");
    firstAttribute.setUrn(ComplexTypeExtension.SCHEMA_URN);
    firstAttribute.setType(Schema.Attribute.Type.STRING);
    firstAttribute.setDescription("First attribute");
    firstAttribute.setMutability(Schema.Attribute.Mutability.READ_WRITE);
    firstAttribute.setReturned(Schema.Attribute.Returned.DEFAULT);
    firstAttribute.setUniqueness(Schema.Attribute.Uniqueness.NONE);

    assertThat(schema.getAttribute("complexType"))
      .hasName("complexType")
      .hasSubAttributes(List.of(firstAttribute));
  }

  private AttributeAssert assertThat(Schema.Attribute attribute) {
    return new AttributeAssert(attribute);
  }
}
