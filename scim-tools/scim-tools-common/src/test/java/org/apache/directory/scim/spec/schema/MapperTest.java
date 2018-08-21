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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


public class MapperTest {

  static final String[] ISO_DATETIME_EXAMPLES = {
      "2015-04-26T01:37:17+00:00",
      "2015-04-26T01:37:17Z"
  };

  static String[] getIsoDateTimeExamples() {
    return ISO_DATETIME_EXAMPLES;
  }

  /**
   * Tests that the regular expression provided used to decompose an ISO 8601
   * DateTime string works with the examples from wikipedia.
   * 
   * @param isoDateTime a String[] of examples to test.
   */
  @ParameterizedTest
  @MethodSource("getIsoDateTimeExamples")
  public void testDateTimePatternWorksForIso8601Strings(String isoDateTime) {
    Pattern pattern = Pattern.compile(Mapper.ISO8601_PATTERN);
    assertNotNull(pattern);
    Matcher matcher = pattern.matcher(isoDateTime);
    assertTrue(matcher.matches());
  }

  /**
   * Tests that the static final indexes in the Mapper class correspond to the
   * correct matching groups in the ISO 8601 regular expression.
   * 
   * @param isoDateTime a String[] of examples to test.
   */
  @ParameterizedTest
  @MethodSource("getIsoDateTimeExamples")
  public void testDateTimeGroupIndexesProvideCorrectSubstrings(String isoDateTime) {
    Pattern pattern = Pattern.compile(Mapper.ISO8601_PATTERN);
    Matcher matcher = pattern.matcher(isoDateTime);
    assumeTrue(matcher.matches());
    assertEquals("2015", matcher.group(Mapper.DATE_COMPONENT_INDEX_YEAR));
    assertEquals("04", matcher.group(Mapper.DATE_COMPONENT_INDEX_MONTH));
    assertEquals("26", matcher.group(Mapper.DATE_COMPONENT_INDEX_DAY));
    assertEquals("01", matcher.group(Mapper.TIME_COMPONENT_INDEX_HOUR));
    assertEquals("37", matcher.group(Mapper.TIME_COMPONENT_INDEX_MINUTE));
    assertEquals("17", matcher.group(Mapper.TIME_COMPONENT_INDEX_SECOND));
    assertTrue("+00:00".equals(matcher.group(Mapper.TIMEZONE_COMPONENT_INDEX)) ||
        "Z".equals(matcher.group(Mapper.TIMEZONE_COMPONENT_INDEX)));
  }
  
  @ParameterizedTest
  @MethodSource("getIsoDateTimeExamples")
  public void testConvertDateTimeFromString(String isoDateTime) throws ParseException {
    Mapper mapper = new Mapper();
    Date date = mapper.convertDateTime(isoDateTime);
    TimeZone timeZone = new SimpleTimeZone(0, "GMT");
    GregorianCalendar calendar = new GregorianCalendar(timeZone);
    calendar.setTime(date);
    assertEquals(2015, calendar.get(Calendar.YEAR));
    assertEquals(3, calendar.get(Calendar.MONTH));
    assertEquals(26, calendar.get(Calendar.DATE));
    assertEquals(1, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(37, calendar.get(Calendar.MINUTE));
    assertEquals(17, calendar.get(Calendar.SECOND));
  }
  
  @Test
  @Disabled
  public void convertDateTimeFromDate() {
    Mapper mapper = new Mapper();
    TimeZone timeZone = new SimpleTimeZone(0, "GMT");
    GregorianCalendar calendar = new GregorianCalendar(timeZone);
    calendar.set(Calendar.YEAR, 2015);
    calendar.set(Calendar.MONTH, 03);
    calendar.set(Calendar.DATE, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 1);
    calendar.set(Calendar.MINUTE, 37);
    calendar.set(Calendar.SECOND, 17);
    Date date = calendar.getTime();
    String actualDateTime = mapper.convertDateTime(date);
    assertEquals(ISO_DATETIME_EXAMPLES[0], actualDateTime);
  }

  /**
   * Tests that the convertTimeZone() method properly sets the raw offset of
   * the Java TimeZone object (ignoring locality so there are no IDs provided).
   * 
   * @param isoTimeZone an ISO 8601 formatted timezone string.
   * @param expectedHours the expected hours.
   * @param expectedMinutes the expected minutes.
   */
  @ParameterizedTest
  @CsvSource({
      " 00:00,   0,    0",
      "+00:00,   0,    0",
      "-00:00,  -0,    0",
      " 00:30,   0,   30",
      "+00:30,   0,   30",
      "-00:30,   0,  -30",
      " 05:00,   5,    0",
      "+05:00,   5,    0",
      "-05:00,  -5,    0",
      " 05:30,   5,   30",
      "+05:30,   5,   30",
      "-05:30,  -5,  -30",
      " 00,      0,    0",
      "+00,      0,    0",
      "-00,      0,    0",
      "+05,      5,    0",
      "-05,     -5,    0",
  })
  public void testConvertTimeZone(String isoTimeZone, int expectedHours, int expectedMinutes) {
    Mapper mapper = new Mapper();
    TimeZone timeZone = mapper.convertTimeZone(isoTimeZone);
    int actualOffsetMinutes = timeZone.getRawOffset() / 1000;
    int actualHours = actualOffsetMinutes / 60;
    int actualMinutes = actualOffsetMinutes % 60;
    assertEquals(expectedHours, actualHours);
    assertEquals(expectedMinutes, actualMinutes);
  }

}
