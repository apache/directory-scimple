package org.apache.directory.scim.server.utility.patch;

import static org.apache.directory.scim.server.utility.patch.FilterMatchUtil.*;
import static org.apache.directory.scim.test.helpers.ScimTestHelper.createRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.time.LocalDateTime;

import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.filter.CompareOperator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

class FilterMatchUtilTest {

  Registry registry;
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    this.registry = createRegistry();
    this.objectMapper = new ObjectMapperFactory(this.registry).createObjectMapper();
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testPresent_is() {
    final Map<String, Object> element = ImmutableMap.of("key", "value");
    assertThat(presentExpression("key", element)).isTrue();
  }

  @Test
  void testPresent_not() {
    final Map<String, Object> element = ImmutableMap.of("key", "value");
    assertThat(presentExpression("NonExistentKey", element)).isFalse();
  }

  @Test
  void testBinaryCompare_equals() throws ScimException {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThat(binaryCompare(encodedString, encodedString,
      CompareOperator.EQ)).isTrue();
  }

  @Test
  void testBinaryCompare_notEquals() throws ScimException {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThat(binaryCompare(encodedString, originalInput,
      CompareOperator.NE)).isTrue();
  }

  @Test
  void testBinaryCompare_lessThan() {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThrows(ScimException.class, () -> binaryCompare(encodedString, originalInput,
      CompareOperator.LT));
  }

  @Test
  void testBinaryCompare_lessThanOrEqualTo() {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThrows(ScimException.class, () -> binaryCompare(encodedString, originalInput,
      CompareOperator.LE));
  }

  @Test
  void testBinaryCompare_greaterThan() {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThrows(ScimException.class, () -> binaryCompare(encodedString, originalInput,
      CompareOperator.GT));
  }

  @Test
  void testBinaryCompare_greaterThanOrEqualTo() {
    String originalInput = "test input";
    String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

    assertThrows(ScimException.class, () -> binaryCompare(encodedString, originalInput,
      CompareOperator.GE));
  }

  @Test
  void testBooleanCompare_equals() throws ScimException {
    assertThat(booleanCompare(true, true, CompareOperator.EQ)).isTrue();
  }

  @Test
  void testBooleanCompare_notEquals() throws ScimException {
    assertThat(booleanCompare(true, false, CompareOperator.NE)).isTrue();
  }

  @Test
  void testBooleanCompare_lessThan() {
    assertThrows(ScimException.class, () -> booleanCompare(true, true,
      CompareOperator.LT));
  }

  @Test
  void testBooleanCompare_lessThanOrEqualTo() {
    assertThrows(ScimException.class, () -> booleanCompare(true, true,
      CompareOperator.LE));
  }

  @Test
  void testBooleanCompare_greaterThan() {
    assertThrows(ScimException.class, () -> booleanCompare(true, true,
      CompareOperator.GT));
  }

  @Test
  void testBooleanCompare_greaterThanOrEqualTo() {
    assertThrows(ScimException.class, () -> booleanCompare(false, false,
      CompareOperator.GE));
  }

  @Test
  void testIntegerCompare_equals() {
    assertThat(integerCompare(1, 1, CompareOperator.EQ)).isTrue();
  }

  @Test
  void testIntegerCompare_notEquals() {
    assertThat(integerCompare(1, 2, CompareOperator.NE)).isTrue();
  }

  @Test
  void testIntegerCompare_lessThan() {
    assertThat(integerCompare(1, 2, CompareOperator.LT)).isTrue();
  }

  @Test
  void testIntegerCompare_lessThanOrEqualTo() {
    assertThat(integerCompare(1, 1, CompareOperator.LE)).isTrue();
  }

  @Test
  void testIntegerCompare_greaterThan() {
    assertThat(integerCompare(2, 1, CompareOperator.GT)).isTrue();
  }

  @Test
  void testIntegerCompare_greaterThanOrEqualTo() {
    assertThat(integerCompare(1, 1, CompareOperator.GE)).isTrue();
  }

  @Test
  void testNumberCompare_equals() {
    assertThat(numberCompare(1.0, 1.0, CompareOperator.EQ)).isTrue();
  }

  @Test
  void testNumberCompare_notEquals() {
    assertThat(numberCompare(1.0, 2.0, CompareOperator.NE)).isTrue();
  }

  @Test
  void testNumberCompare_lessThan() {
    assertThat(numberCompare(1.0, 2.0, CompareOperator.LT)).isTrue();
  }

  @Test
  void testNumberCompare_lessThanOrEqualTo() {
    assertThat(numberCompare(1.0, 1.0, CompareOperator.LE)).isTrue();
  }

  @Test
  void testNumberCompare_greaterThan() {
    assertThat(numberCompare(2.0, 1.0, CompareOperator.GT)).isTrue();
  }

  @Test
  void testNumberCompare_greaterThanOrEqualTo() {
    assertThat(numberCompare(1.0, 1.0, CompareOperator.GE)).isTrue();
  }

  @Test
  void testDateTimeCompare_equals() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime, localDateTime,
      CompareOperator.EQ)).isTrue();
  }

  @Test
  void testDateTimeCompare_notEquals() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime, localDateTime.plusDays(1),
      CompareOperator.NE)).isTrue();
  }

  @Test
  void testDateTimeCompare_lessThan() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime, localDateTime.plusDays(1),
      CompareOperator.LT)).isTrue();
  }

  @Test
  void testDateTimeCompare_lessThanOrEqualTo() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime, localDateTime,
      CompareOperator.LE)).isTrue();
  }

  @Test
  void testDateTimeCompare_greaterThan() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime.plusHours(1),
      localDateTime, CompareOperator.GT)).isTrue();
  }

  @Test
  void testDateTimeCompare_greaterThanOrEqualTo() {
    LocalDateTime localDateTime = LocalDateTime.now();
    assertThat(dateTimeCompare(localDateTime, localDateTime,
      CompareOperator.GE)).isTrue();
  }

  @Test
  void testReferenceCompare_equals() {
    assertThat(referenceCompare(URI.create("http://example.com/Users/ProfileUrl"),
      URI.create("http://example.com/Users/ProfileUrl"),
      CompareOperator.EQ)).isTrue();
  }

  @Test
  void testReferenceCompare_notEquals() {
    assertThat(referenceCompare(URI.create("http://example.com/Users/JohnDoe"),
      URI.create("http://example.com/Users/ProfileUrl"),
      CompareOperator.NE)).isTrue();
  }

  @Test
  void testReferenceCompare_lessThan() {
    assertThat(referenceCompare(URI.create("http://example.com/Users/JohnDoe"),
      URI.create("https://example.com/Users/JohnDoe"),
      CompareOperator.LT)).isTrue();
  }

  @Test
  void testReferenceCompare_lessThanOrEqualTo() {
    assertThat(referenceCompare(URI.create("https://example.com/Users/JohnDoe"),
      URI.create("https://example.com/Users/JohnDoe"),
      CompareOperator.LE)).isTrue();
  }

  @Test
  void testReferenceCompare_greaterThan() {
    assertThat(referenceCompare(URI.create("https://example.com/Users/JohnDoe"),
      URI.create("http://example.com/Users/JohnDoe"),
      CompareOperator.GT)).isTrue();
  }

  @Test
  void testReferenceCompare_greaterThanOrEqualTo() {
    assertThat(referenceCompare(URI.create("https://example.com/Users/JohnDoe"),
      URI.create("https://example.com/Users/JohnDoe"),
      CompareOperator.GE)).isTrue();
  }

  @Test
  void testComplexCompare_equals() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.EQ));
  }

  @Test
  void testComplexCompare_notEquals() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.NE));
  }

  @Test
  void testComplexCompare_lessThan() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.LT));
  }

  @Test
  void testComplexCompare_lessThanOrEqualTo() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.LE));
  }

  @Test
  void testComplexCompare_greaterThan() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.GT));
  }

  @Test
  void testComplexCompare_greaterThanOrEqualTo() {
    assertThrows(UnsupportedOperationException.class,
      () -> complexCompare(new HashMap<>(),
        new HashMap<>(), CompareOperator.GE));
  }

  @Test
  void testStringCompare_equalsIgnoreCase() {
    assertThat(stringCompare("string", "String",
      CompareOperator.EQ, false)).isTrue();
  }

  @Test
  void testStringCompare_notEqualsIgnoreCase() {
    assertThat(stringCompare("string", "Dummy",
      CompareOperator.NE, false)).isTrue();
  }

  @Test
  void testStringCompare_startsWithIgnoreCase() {
    assertThat(stringCompare("string", "St",
      CompareOperator.SW, false)).isTrue();
  }

  @Test
  void testStringCompare_endsWithIgnoreCase() {
    assertThat(stringCompare("string", "Ng",
      CompareOperator.EW, false)).isTrue();
  }

  @Test
  void testStringCompare_containsIgnoreCase() {
    assertThat(stringCompare("string", "In",
      CompareOperator.CO, false)).isTrue();
  }

  @Test
  void testStringCompare_lessThanIgnoreCase() {
    assertThat(stringCompare("a", "B",
      CompareOperator.LT, false)).isTrue();
  }

  @Test
  void testStringCompare_lessThanOrEqualToIgnoreCase() {
    assertThat(stringCompare("a", "A",
      CompareOperator.LE, false)).isTrue();
  }

  @Test
  void testStringCompare_greaterThanIgnoreCase() {
    assertThat(stringCompare("b", "a",
      CompareOperator.GT, false)).isTrue();
  }

  @Test
  void testStringCompare_greaterThanOrEqualToIgnoreCase() {
    assertThat(stringCompare("B", "b",
      CompareOperator.GE, false)).isTrue();
  }

  @Test
  void testStringCompare_equals() {
    assertThat(stringCompare("string", "string",
      CompareOperator.EQ, true)).isTrue();
    assertThat(stringCompare("string", "String",
      CompareOperator.EQ, true)).isFalse();
  }

  @Test
  void testStringCompare_notEquals() {
    assertThat(stringCompare("string", "String",
      CompareOperator.NE, true)).isTrue();
  }

  @Test
  void testStringCompare_startsWith() {
    assertThat(stringCompare("string", "st",
      CompareOperator.SW, true)).isTrue();
    assertThat(stringCompare("string", "St",
      CompareOperator.SW, true)).isFalse();
  }

  @Test
  void testStringCompare_endsWith() {
    assertThat(stringCompare("string", "ng",
      CompareOperator.EW, true)).isTrue();
  }

  @Test
  void testStringCompare_contains() {
    assertThat(stringCompare("string", "in",
      CompareOperator.CO, true)).isTrue();
  }

  @Test
  void testStringCompare_lessThan() {
    assertThat(stringCompare("a", "b",
      CompareOperator.LT, true)).isTrue();
  }

  @Test
  void testStringCompare_lessThanOrEqualTo() {
    assertThat(stringCompare("a", "b",
      CompareOperator.LE, true)).isTrue();
  }

  @Test
  void testStringCompare_greaterThan() {
    assertThat(stringCompare("b", "a",
      CompareOperator.GT, true)).isTrue();
  }

  @Test
  void testStringCompare_greaterThanOrEqualTo() {
    assertThat(stringCompare("b", "a",
      CompareOperator.GE, true)).isTrue();
  }

  @Test
  void testCompareResult_lessThan() {
    assertThat(compareResult(CompareOperator.LT, -1)).isTrue();
  }

  @Test
  void testCompareResult_lessThanOrEqualTo() {
    assertThat(compareResult(CompareOperator.LE, 0)).isTrue();
  }

  @Test
  void testCompareResult_greaterThan() {
    assertThat(compareResult(CompareOperator.GT, 1)).isTrue();
  }

  @Test
  void testCompareResult_greaterThanOrEqualTo() {
    assertThat(compareResult(CompareOperator.GE, 0)).isTrue();
  }
}
