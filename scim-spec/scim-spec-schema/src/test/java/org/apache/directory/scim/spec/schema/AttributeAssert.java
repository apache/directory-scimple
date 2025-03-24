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

import org.assertj.core.api.AbstractAssert;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeAssert extends AbstractAssert<AttributeAssert, Schema.Attribute> {
    AttributeAssert(Schema.Attribute attribute) {
      super(attribute, AttributeAssert.class);
    }

    AttributeAssert hasName(String name) {
      isNotNull();
      assertThat(actual.getName())
        .as("Incorrect Attribute.name")
        .isEqualTo(name);
      return this;
    }

    AttributeAssert hasMutability(Schema.Attribute.Mutability mutability) {
      isNotNull();
      assertThat(actual.getMutability())
        .as("Incorrect Attribute.mutability")
        .isEqualTo(mutability);
      return this;
    }

    AttributeAssert hasReturned(Schema.Attribute.Returned returned) {
      isNotNull();
      assertThat(actual.getReturned())
        .as("Incorrect Attribute.returned")
        .isEqualTo(returned);
      return this;
    }

    AttributeAssert hasType(Schema.Attribute.Type type) {
      isNotNull();
      assertThat(actual.getType())
        .as("Incorrect Attribute.type")
        .isEqualTo(type);
      return this;
    }

    AttributeAssert hasUniqueness(Schema.Attribute.Uniqueness uniqueness) {
      isNotNull();
      assertThat(actual.getUniqueness())
        .as("Incorrect Attribute.uniqueness")
        .isEqualTo(uniqueness);
      return this;
    }

    AttributeAssert hasSubAttributes(List<Schema.Attribute> subAttributes) {
      isNotNull();
      assertThat(actual.getSubAttributes())
        .as("Incorrect Attribute.subAttributes")
        .isEqualTo(subAttributes);
      return this;
    }

    AttributeAssert isMultiValued(boolean multiValued) {
      isNotNull();
      assertThat(actual.isMultiValued())
        .as("Incorrect Attribute.multiValued")
        .isEqualTo(multiValued);
      return this;
    }

    AttributeAssert hasDescription(String description) {
      isNotNull();
      assertThat(actual.getDescription())
        .as("Incorrect Attribute.description")
        .isEqualTo(description);
      return this;
    }

    AttributeAssert isRequired(boolean required) {
      isNotNull();
      assertThat(actual.isRequired())
        .as("Incorrect Attribute.required")
        .isEqualTo(required);
      return this;
    }

    AttributeAssert hasCanonicalValues(Set<String> canonicalValues) {
      isNotNull();
      assertThat(actual.getCanonicalValues())
        .as("Incorrect Attribute.canonicalValues")
        .isEqualTo(canonicalValues);
      return this;
    }

    AttributeAssert isCaseExact(boolean caseExact) {
      isNotNull();
      assertThat(actual.isCaseExact())
        .as("Incorrect Attribute.caseExact")
        .isEqualTo(caseExact);
      return this;
    }

    AttributeAssert hasReferenceTypes(List<String> referenceTypes) {
      isNotNull();
      assertThat(actual.getReferenceTypes())
        .as("Incorrect Attribute.referenceTypes")
        .isEqualTo(referenceTypes);
      return this;
    }

  AttributeAssert hasPath(String path) {
    isNotNull();
    assertThat(actual.getPath())
      .as("Incorrect Attribute.path")
      .isEqualTo(path);
    return this;
  }

  AttributeAssert hasUrn(String urn) {
    isNotNull();
    assertThat(actual.getUrn())
      .as("Incorrect Attribute.urn")
      .isEqualTo(urn);
    return this;
  }
  }
