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

package org.apache.directory.scim.spec.patch;

import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.filter.FilterParsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the nesting-depth limit applied by {@link FilterParsers} when parsing PATCH
 * operation paths (exercised through {@link PatchOperationPath}).
 *
 * <p>Depth tests use the nested-group form {@code attrName[(((a pr)))]}, which drives the
 * recursive {@code attributeExpression} grammar rule, rather than a flat {@code and}-chain
 * (which ANTLR handles iteratively and would not exercise the stack guard).
 */
public class PatchPathParsingTest {

  // A PATCH-path predicate "attrName[a pr]" has a base rule-entry depth of 1 (one
  // attributeExpression), and each additional '(' group adds one more. So the largest
  // nesting that still parses is MAX_NESTING_DEPTH - 1 groups. (The filter and PATCH-path
  // grammars have different base offsets, which is why this differs from the filter tests.)
  private static final int LARGEST_PARSING_GROUPS = FilterParsers.MAX_NESTING_DEPTH - 1; // 39

  private static String nestedPatchPath(int groups) {
    return "attrName[" + "(".repeat(groups) + "a pr" + ")".repeat(groups) + "]";
  }

  @Test
  public void depthAtLimit_largestParsingNesting_parsesSuccessfully() {
    assertThatCode(() -> PatchOperationPath.fromString(nestedPatchPath(LARGEST_PARSING_GROUPS)))
        .doesNotThrowAnyException();
  }

  @Test
  public void depthOnePastLimit_smallestThrowingNesting_throwsFilterParseException() {
    assertThatThrownBy(() -> PatchOperationPath.fromString(nestedPatchPath(LARGEST_PARSING_GROUPS + 1)))
        .isInstanceOf(FilterParseException.class)
        .hasMessageContaining("nesting depth exceeds maximum");
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  public void depthOf200_throwsFilterParseException_noStackOverflow() {
    assertThatThrownBy(() -> PatchOperationPath.fromString(nestedPatchPath(200)))
        .isInstanceOf(FilterParseException.class)
        .hasMessageContaining("nesting depth exceeds maximum")
        .satisfies(ex -> {
          for (Throwable t = ex; t != null; t = t.getCause()) {
            assertThat(t).isNotInstanceOf(StackOverflowError.class);
          }
        });
  }

  @Test
  public void malformedPatchPath_unbalancedBracket_throwsFilterParseException() {
    // Confirms the syntax-error path still yields a FilterParseException with a static,
    // leak-free message (no raw input / ANTLR token text).
    assertThatThrownBy(() -> PatchOperationPath.fromString("attrName[a pr"))
        .isInstanceOf(FilterParseException.class)
        .hasMessage("Failed to parse patch path expression");
  }
}
