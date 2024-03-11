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
import org.apache.directory.scim.spec.resources.ScimUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SchemasTest {

  private Schema schema = Schemas.schemaForExtension(AllSchemaTypesExtension.class);
  private Schema userSchema = Schemas.schemaFor(ScimUser.class);

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
      .hasReferenceTypes(null)
      .hasPath("string1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":string1");
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
      .hasReferenceTypes(null)
      .hasPath("string2")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":string2");
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
      .hasReferenceTypes(null)
      .hasPath("stringList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":stringList1");
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
      .hasReferenceTypes(null)
      .hasPath("boolean1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":boolean1");
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
      .hasReferenceTypes(null)
      .hasPath("booleanList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":booleanList1");
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
      .hasReferenceTypes(null)
      .hasPath("boolean2")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":boolean2");
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
      .hasReferenceTypes(null)
      .hasPath("booleanArray1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":booleanArray1");
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
      .hasReferenceTypes(null)
      .hasPath("decimal1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":decimal1");
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
      .hasReferenceTypes(null)
      .hasPath("decimal2")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":decimal2");
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
      .hasReferenceTypes(null)
      .hasPath("decimalList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":decimalList1");
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
      .hasReferenceTypes(null)
      .hasPath("decimalArray1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":decimalArray1");
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
      .hasReferenceTypes(null)
      .hasPath("integer1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":integer1");
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
      .hasReferenceTypes(null)
      .hasPath("integer2")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":integer2");
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
      .hasReferenceTypes(null)
      .hasPath("integerList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":integerList1");
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
      .hasReferenceTypes(null)
      .hasPath("integerArray1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":integerArray1");
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
      .hasReferenceTypes(null)
      .hasPath("date1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":date1");
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
      .hasReferenceTypes(null)
      .hasPath("dateList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":dateList1");
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
      .hasReferenceTypes(null)
      .hasPath("dateArray1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":dateArray1");
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
      .hasReferenceTypes(null)
      .hasPath("instant1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":instant1");
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
      .hasReferenceTypes(null)
      .hasPath("instantList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":instantList1");
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
      .hasReferenceTypes(null)
      .hasPath("instantArray1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":instantArray1");
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
      .hasReferenceTypes(null)
      .hasPath("binary1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":binary1");
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
      .hasReferenceTypes(List.of("one", "two"))
      .hasPath("ref1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":ref1");
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
      .hasReferenceTypes(List.of("three", "four"))
      .hasPath("$ref")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":$ref");
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
      .hasReferenceTypes(List.of("one", "two", "three"))
      .hasPath("refList1")
      .hasUrn(AllSchemaTypesExtension.SCHEMA_URN + ":refList1");
  }

  @Test
  public void complexTypeSubAttributesNotDuplicated() {
    Schema schema = Schemas.schemaForExtension(ComplexTypeExtension.class);
    Schema.Attribute complexType = schema.getAttribute("complexType");

    Assertions.assertThat((complexType.subAttributes.size())).isEqualTo(1);

    Schema.Attribute firstAttribute = new Schema.Attribute();
    firstAttribute.setName("firstAttribute");
//    firstAttribute.setUrn(ComplexTypeExtension.SCHEMA_URN);
    firstAttribute.setType(Schema.Attribute.Type.STRING);
    firstAttribute.setDescription("First attribute");
    firstAttribute.setMutability(Schema.Attribute.Mutability.READ_WRITE);
    firstAttribute.setReturned(Schema.Attribute.Returned.DEFAULT);
    firstAttribute.setUniqueness(Schema.Attribute.Uniqueness.NONE);

    assertThat(schema.getAttribute("complexType"))
      .hasName("complexType")
      .hasSubAttributes(List.of(firstAttribute))
      .hasPath("complexType")
      .hasUrn(ComplexTypeExtension.SCHEMA_URN + ":complexType");
  }

  @Test
  public void nestedAttributeUrn() {
    Schema.Attribute nameAttribute = userSchema.getAttribute("name");
    Schema.Attribute familyNameAttribute = nameAttribute.getAttribute("familyName");

    assertThat(familyNameAttribute)
      .hasName("familyName")
      .hasMutability(Schema.Attribute.Mutability.READ_WRITE)
      .hasReturned(Schema.Attribute.Returned.DEFAULT)
      .hasType(Schema.Attribute.Type.STRING)
      .hasUniqueness(Schema.Attribute.Uniqueness.NONE)
      .isMultiValued(false)
      .hasDescription("The family name of the User, or Last Name in most Western languages (e.g. Jensen given the full name Ms. Barbara J Jensen, III.).")
      .isRequired(false)
      .isCaseExact(false)
      .hasSubAttributes(null)
      .hasCanonicalValues(null)
      .hasReferenceTypes(null)
      .hasPath("name.familyName")
      .hasUrn(ScimUser.SCHEMA_URI + ":name.familyName");
  }

  private AttributeAssert assertThat(Schema.Attribute attribute) {
    return new AttributeAssert(attribute);
  }
}
