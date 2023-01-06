package org.apache.directory.scim.spec.filter;

import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import java.util.function.Predicate;


public final class FilterExpressions {

  private FilterExpressions() {}

  /**
   * Converts a filter into a Predicate used for in-memory evaluation. Production implementations should translate the Filter into
   * the appropriate query language.
   * <p>
   *
   * <b>This implementation should only be used for demo proposes.</b>
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

  public static Predicate<Object> inMemory(FilterExpression expression, Schema schema) {
    return InMemoryScimFilterMatcher.toPredicate(expression, schema);
  }
}
