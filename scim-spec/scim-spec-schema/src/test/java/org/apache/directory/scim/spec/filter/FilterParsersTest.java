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

package org.apache.directory.scim.spec.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the nesting-depth limit applied by {@link FilterParsers} when parsing filter
 * expressions (exercised through the {@link Filter} public API).
 */
public class FilterParsersTest {

  // A simple filter predicate "a pr" has a base rule-entry depth of 2 (one filterExpression
  // + one attributeExpression), and each additional '(' group adds one filterExpression
  // entry. So the largest nesting that still parses is MAX_NESTING_DEPTH - 2 groups.
  private static final int LARGEST_PARSING_GROUPS = FilterParsers.MAX_NESTING_DEPTH - 2; // 38

  @Test
  public void shortValidFilter_parsesSuccessfully() {
    assertThatCode(() -> new Filter("userName eq \"bjensen\""))
        .doesNotThrowAnyException();
  }

  @Test
  public void depthAtLimit_largestParsingNesting_parsesSuccessfully() {
    String nested = "(".repeat(LARGEST_PARSING_GROUPS) + "a pr" + ")".repeat(LARGEST_PARSING_GROUPS);

    assertThatCode(() -> new Filter(nested))
        .doesNotThrowAnyException();
  }

  @Test
  public void depthOnePastLimit_smallestThrowingNesting_throwsFilterParseException() {
    int groups = LARGEST_PARSING_GROUPS + 1; // 39 → total rule-entry depth 41 > MAX_NESTING_DEPTH
    String nested = "(".repeat(groups) + "a pr" + ")".repeat(groups);

    assertThatThrownBy(() -> new Filter(nested))
        .isInstanceOf(FilterParseException.class)
        .hasMessageContaining("nesting depth exceeds maximum");
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  public void depthOf200_throwsFilterParseException_noStackOverflow() {
    String nested = "(".repeat(200) + "a pr" + ")".repeat(200);

    assertThatThrownBy(() -> new Filter(nested))
        .isInstanceOf(FilterParseException.class)
        .hasMessageContaining("nesting depth exceeds maximum")
        .satisfies(ex -> {
          for (Throwable t = ex; t != null; t = t.getCause()) {
            assertThat(t).isNotInstanceOf(StackOverflowError.class);
          }
        });
  }

  @Test
  @Timeout(value = 15, unit = TimeUnit.SECONDS)
  public void depth1000_doesNotStackOverflow() {
    String nested = "(".repeat(1000) + "a pr" + ")".repeat(1000);

    assertThatThrownBy(() -> new Filter(nested))
        .isInstanceOf(FilterParseException.class)
        .satisfies(ex -> {
          for (Throwable t = ex; t != null; t = t.getCause()) {
            assertThat(t).isNotInstanceOf(StackOverflowError.class);
          }
        });
  }

  @Test
  public void malformedFilter_missingOperand_throwsFilterParseException() {
    assertThatThrownBy(() -> new Filter("userName eq"))
        .isInstanceOf(FilterParseException.class);
  }

  @Test
  public void malformedFilter_unbalancedParens_throwsFilterParseException() {
    assertThatThrownBy(() -> new Filter("((a pr)"))
        .isInstanceOf(FilterParseException.class);
  }

  @Test
  public void nullInput_throwsFilterParseException_notNullPointer() {
    assertThatThrownBy(() -> FilterParsers.parseFilter(null, new ExpressionBuildingListener()))
        .isInstanceOf(FilterParseException.class)
        .isNotInstanceOf(NullPointerException.class)
        .hasMessageContaining("must not be null");
  }
}
