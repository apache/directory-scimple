package edu.psu.swe.scim.api.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.psu.swe.scim.api.schema.Mapper;

@RunWith(JUnitParamsRunner.class)
public class MapperTest {

  static final String[] ISO_DATETIME_EXAMPLES = {
      "2015-04-26T01:37:17+00:00",
      "2015-04-26T01:37:17Z"
  };

  Mapper mapper;

  @Before
  public void setUp() {
    mapper = new Mapper();
  }

  String[] getIsoDateTimeExamples() {
    return ISO_DATETIME_EXAMPLES;
  }

  /**
   * Tests that the regular expression provided used to decompose an ISO 8601
   * DateTime string works with the examples from wikipedia.
   * 
   * @param isoDateTime a String[] of examples to test.
   */
  @Test
  @Parameters(method = "getIsoDateTimeExamples")
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
  @Test
  @Parameters(method = "getIsoDateTimeExamples")
  public void testDateTimeGroupIndexesProvideCorrectSubstrings(String isoDateTime) {
    Pattern pattern = Pattern.compile(Mapper.ISO8601_PATTERN);
    assumeNotNull(pattern);
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
  
  @Test
  @Parameters(method = "getIsoDateTimeExamples")
  public void testConvertDateTimeFromString(String isoDateTime) throws ParseException {
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
  public void testConvertDateTimeFromDate() {
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
  @Test
  @Parameters({
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
    TimeZone timeZone = mapper.convertTimeZone(isoTimeZone);
    int actualOffsetMinutes = timeZone.getRawOffset() / 1000;
    int actualHours = actualOffsetMinutes / 60;
    int actualMinutes = actualOffsetMinutes % 60;
    assertEquals(expectedHours, actualHours);
    assertEquals(expectedMinutes, actualMinutes);
  }

}
