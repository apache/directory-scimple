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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MapperTest {

  static final String[] ISO_DATETIME_EXAMPLES = {
      "2015-04-26T01:37:17+00:00",
      "2015-04-26T01:37:17Z"
  };

  static String[] getIsoDateTimeExamples() {
    return ISO_DATETIME_EXAMPLES;
  }

  @ParameterizedTest
  @MethodSource("getIsoDateTimeExamples")
  public void testConvertDateTimeFromString(String isoDateTime) throws ParseException {
    Mapper mapper = new Mapper();
    Instant instant = mapper.convertDateTime(isoDateTime);
    TimeZone timeZone = new SimpleTimeZone(0, "GMT");
    GregorianCalendar calendar = new GregorianCalendar(timeZone);
    calendar.setTime(Date.from(instant));
    assertEquals(2015, calendar.get(Calendar.YEAR));
    assertEquals(3, calendar.get(Calendar.MONTH));
    assertEquals(26, calendar.get(Calendar.DATE));
    assertEquals(1, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(37, calendar.get(Calendar.MINUTE));
    assertEquals(17, calendar.get(Calendar.SECOND));
  }
}
