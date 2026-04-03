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

import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Converts a {@link SortRequest} into a {@link Comparator} used for in-memory sorting. Production implementations
 * should translate the SortRequest into the appropriate query language (e.g., SQL ORDER BY).
 * <p>
 *
 * <b>This implementation should only be used for small collections or demo purposes.</b>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.2.3">RFC 7644 Section 3.4.2.3 - Sorting</a>
 */
public final class SortExpressions {

  private static final Logger log = LoggerFactory.getLogger(SortExpressions.class);

  private SortExpressions() {}

  /**
   * Converts a {@link SortRequest} into a {@link Comparator} for in-memory evaluation.
   * <p>
   * If the {@code sortRequest} is {@code null} or has no {@code sortBy}, a no-op comparator is returned
   * that preserves the original order. Per RFC 7644 Section 3.4.2.3, when {@code sortOrder} is not specified,
   * it defaults to {@link SortOrder#ASCENDING}.
   *
   * @param sortRequest the sort request containing sortBy and sortOrder
   * @param schema the schema to resolve attributes against
   * @return a comparator for sorting ScimResources
   */
  public static Comparator<ScimResource> comparator(SortRequest sortRequest, Schema schema) {
    if (sortRequest == null || sortRequest.getSortBy() == null) {
      return (a, b) -> 0;
    }

    AttributeReference sortBy = sortRequest.getSortBy();
    SortOrder sortOrder = sortRequest.getSortOrder() != null ? sortRequest.getSortOrder() : SortOrder.ASCENDING;

    Schema.Attribute schemaAttribute = BaseFilterExpressionMapper.attribute(schema, sortBy);
    if (schemaAttribute == null) {
      log.debug("Sort attribute '{}' not found in schema, preserving original order", sortBy);
      return (a, b) -> 0;
    }

    boolean hasSubAttribute = sortBy.getSubAttributeName() != null;
    Schema.Attribute parentAttribute = hasSubAttribute ? schema.getAttribute(sortBy.getAttributeName()) : null;

    return (a, b) -> {
      Object valueA = extractValue(a, schemaAttribute, parentAttribute, hasSubAttribute);
      Object valueB = extractValue(b, schemaAttribute, parentAttribute, hasSubAttribute);

      // nulls always sort to end, regardless of sort order
      if (valueA == null && valueB == null) return 0;
      if (valueA == null) return 1;
      if (valueB == null) return -1;

      int result = compareValues(valueA, valueB, schemaAttribute);
      return sortOrder == SortOrder.DESCENDING ? -result : result;
    };
  }

  private static Object extractValue(Object resource, Schema.Attribute attribute, Schema.Attribute parentAttribute, boolean hasSubAttribute) {
    try {
      if (hasSubAttribute && parentAttribute != null) {
        Object parent = parentAttribute.getAccessor().get(resource);
        if (parent == null) {
          return null;
        }
        return attribute.getAccessor().get(parent);
      }
      return attribute.getAccessor().get(resource);
    } catch (Exception e) {
      log.debug("Failed to extract sort value", e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static int compareValues(Object a, Object b, Schema.Attribute attribute) {
    // case-insensitive string comparison when not caseExact
    if (a instanceof String stringA && b instanceof String stringB) {
      if (!attribute.isCaseExact()) {
        return String.CASE_INSENSITIVE_ORDER.compare(stringA, stringB);
      }
      return stringA.compareTo(stringB);
    }

    // Comparable types (LocalDateTime, Integer, etc.)
    if (a instanceof Comparable comparableA && a.getClass().isInstance(b)) {
      return comparableA.compareTo(b);
    }

    return 0;
  }
}
