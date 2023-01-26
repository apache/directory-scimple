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
