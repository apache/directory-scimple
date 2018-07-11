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

/**
 * 
 */
package org.apache.directory.scim.spec.schema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Mapper provides methods to bi-directionally transform SCIM attribute
 * values into Java types. The eight types supported by SCIM are defined in
 * section 2.2 of the SCIM schema specification. The mapping to Java objects are
 * as follows:
 * 
 * <pre>
 *   String    -> string    -> String
 *   Boolean   -> boolean   -> Boolean
 *   Decimal   -> decimal   -> Double
 *   Integer -  > integer   -> Long
 *   DateTime  -> dateTime  -> Date
 *   Binary    -> binary    -> Byte[]
 *   Reference -> reference -> URN?
 *   Complex   -> complex   -> (Java Object as defined)
 * </pre>
 * 
 * @author Steve Moyer
 */
public class Mapper {

  // Pattern for ISO 8601 DateTime - see: https://en.wikipedia.org/wiki/ISO_8601
  static final String ISO8601_PATTERN = "^([0-9]{4})-([0-9]{2})-([0-9]{2})[Tt]([0-9]{2}):([0-9]{2}):([0-9]{2})([Zz]|([+-]{1}[0-9]{2}:[0-9]{2}))";

  // Date time component indexes
  static final int DATE_COMPONENT_INDEX_YEAR = 1;
  static final int DATE_COMPONENT_INDEX_MONTH = 2;
  static final int DATE_COMPONENT_INDEX_DAY = 3;
  static final int TIME_COMPONENT_INDEX_HOUR = 4;
  static final int TIME_COMPONENT_INDEX_MINUTE = 5;
  static final int TIME_COMPONENT_INDEX_SECOND = 6;
  static final int TIMEZONE_COMPONENT_INDEX = 7;

  // Format string to create an ISO 8601 date string from a Java date object
  static final String ISO8601_DATETIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssXXX";

  SimpleDateFormat iso8601DateFormat;
  Pattern iso8601Pattern;

  public Mapper() {
    iso8601DateFormat = new SimpleDateFormat(ISO8601_DATETIME_FORMATTER);
    iso8601Pattern = Pattern.compile(ISO8601_PATTERN);
  }

  public String convertDateTime(Date date) {
    return iso8601DateFormat.format(date);
  }

  /**
   * Converts an ISO 8601 DateTime string into the equivalent Java Date object.
   * 
   * @param date the ISO 8601 DateTime to be converted.
   * @return the equivalent Java Date object.
   * @throws ParseException 
   */
  public Date convertDateTime(String isodate) throws ParseException {
//    Calendar calendar = null;
//    Matcher matcher = iso8601Pattern.matcher(date);
//    if (matcher.matches()) {
//      TimeZone timeZone = convertTimeZone(matcher.group(TIMEZONE_COMPONENT_INDEX));
//      calendar = new GregorianCalendar(timeZone);
//      calendar.set(
//          Integer.parseInt(matcher.group(DATE_COMPONENT_INDEX_YEAR)),
//          Integer.parseInt(matcher.group(DATE_COMPONENT_INDEX_MONTH)),
//          Integer.parseInt(matcher.group(DATE_COMPONENT_INDEX_DAY)),
//          Integer.parseInt(matcher.group(TIME_COMPONENT_INDEX_HOUR)),
//          Integer.parseInt(matcher.group(TIME_COMPONENT_INDEX_MINUTE)),
//          Integer.parseInt(matcher.group(TIME_COMPONENT_INDEX_SECOND))
//          );
//    } else {
//      // TODO - This is an error
//    }
//    return calendar.getTime();
    Date date = iso8601DateFormat.parse(isodate);
    return date;
  }

  /**
   * Converts the timeZone portion of an ISO 8601 date into a Java TimeZone
   * object.
   * 
   * @param timeZone the ISO 8601 representation of the time zone.
   * @return the equivalent Java TimeZone object.
   */
  TimeZone convertTimeZone(String timeZone) {
    String[] timeZoneTokens = timeZone.split(":");
    int hours = 0;
    int minutes = 0;

    if (!timeZone.startsWith("Z") && !timeZone.startsWith("z")) {
      if (timeZoneTokens.length > 0) {
        hours = Integer.parseInt(timeZoneTokens[0]);
      }
      if (timeZoneTokens.length > 1) {
        minutes = Integer.parseInt(timeZoneTokens[1]);
        if (timeZone.startsWith("-")) {
          minutes = minutes * -1;
        }
      }
    }
    
    int timeZoneOffsetMilliSeconds = ((hours * 60) + minutes) * 1000;
    return new SimpleTimeZone(timeZoneOffsetMilliSeconds, "");
  }

}
