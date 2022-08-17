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

package org.apache.directory.scim.example.spring.service;

import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.protocol.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.protocol.filter.CompareOperator;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

final class InMemoryScimFilterMatcher {

  private static final Logger log = LoggerFactory.getLogger(InMemoryScimFilterMatcher.class);

  private InMemoryScimFilterMatcher(){}

  /**
   * Basic FilterExpression support for in-memory objects, actual implementations should translate the Filter into
   * the appropriate query language.
   */
  public static boolean matches(ScimResource resource, Filter filter) {

    // no filter defined, return everything
    if (filter == null) {
      return true;
    }

    // FIXME: This example ONLY supports simple Filter arguments, Filters can be more complex, you would likely translate
    // them into a query instead of manually filtering anyway

    if (filter.getExpression() instanceof AttributeComparisonExpression expression) {
      String attribute = expression.getAttributePath().getAttributeName();
      CompareOperator op = expression.getOperation();
      Object compareValue = expression.getCompareValue();


      Field field = ReflectionUtils.findField(resource.getClass(), attribute);
      if (field == null) {
        return false;
      }
      field.setAccessible(true);

      Object actualValue = ReflectionUtils.getField(field, resource);
      switch (op) {
        case EQ -> {
          return compareValue.equals(actualValue);
        }
        case NE -> {
          return !compareValue.equals(actualValue);
        }
        case SW -> {
          return isStringExpression(field, compareValue, expression)
            && actualValue != null
            && actualValue.toString().startsWith(compareValue.toString());
        }
        case EW -> {
          return isStringExpression(field, compareValue, expression)
            && actualValue != null
            && actualValue.toString().endsWith(compareValue.toString());
        }
        case CO -> {
          return isStringExpression(field, compareValue, expression)
            && actualValue != null
            && actualValue.toString().contains(compareValue.toString());
        }
        default -> throw new ScimResourceInvalidException("This example only supports basic string filters");
      }
    } else{
      log.warn("Unsupported Filter expression of type: " + filter.getExpression().getClass());
      return false;
    }
  }

  private static boolean isStringExpression(Field field, Object compareValue, FilterExpression expression) {
    if (!field.getType().isAssignableFrom(String.class)) {
      // TODO: maybe this should be a 400?
      log.warn("Non String value for expression : " + expression.toFilter());
      return false;
    }
    if (compareValue == null) {
      // TODO: maybe this should be a 400?
      log.warn("Empty value for expression : " + expression.toFilter());
      return false;
    }
    return true;
  }
}
