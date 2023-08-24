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

import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import java.util.Map;
import java.util.function.Predicate;


public final class FilterExpressions {

  private FilterExpressions() {}

  /**
   * Converts a filter into a Predicate used for in-memory evaluation. Production implementations should translate the Filter into
   * the appropriate query language.
   * <p>
   *
   * <b>This implementation should only be used for small collections or demo proposes.</b>
   */
  public static Predicate<ScimResource> inMemory(Filter filter, Schema schema) {
    if (filter == null) {
      return x -> true;
    }
    FilterExpression expression = filter.getExpression();
    if (expression == null) {
      return x -> true;
    }
    return InMemoryScimFilterMatcher.toPredicate(expression, schema);
  }

  public static <R> Predicate<R> inMemory(FilterExpression expression, Schema schema) {
    return InMemoryScimFilterMatcher.toPredicate(expression, schema);
  }

  public static <R> Predicate<R> inMemoryMap(FilterExpression expression, Schema schema) {
    return new InMemoryMapScimFilterMatcher<R>().apply(expression, schema);
  }

  static class InMemoryMapScimFilterMatcher<R> extends InMemoryScimFilterMatcher<R> {
    @Override
    public <A> A get(Schema.Attribute attribute, A actual) {
      Map<String, Object> map = (Map<String, Object>) actual;
      return (A) map.get(attribute.getName());
    }
  }
}
