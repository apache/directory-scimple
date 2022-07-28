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

package org.apache.directory.scim.client.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.client.rest.FilterBuilder;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FilterBuilderNotEqualsTest {

  @Test
  public void testNotnotEqualStringString() throws FilterParseException {
    Filter filter = FilterBuilder.create().notEqual("address.streetAddress", "7714 Sassafrass Way").build();
    Filter expected = new Filter("address.streetAddress NE \"7714 Sassafrass Way\"");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testNotnotEqualStringBoolean() throws FilterParseException {
    Filter filter = FilterBuilder.create().notEqual("address.active", true).build();
    Filter expected = new Filter("address.active NE true");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testNotnotEqualStringDate() throws FilterParseException {
    Date now = new Date();
    Filter filter = FilterBuilder.create().notEqual("date.date", now).build();
    Filter expected = new Filter("date.date NE \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").format(now) + "\""); // FIXME: format is missing TZ
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testNotnotEqualStringLocalDate() throws FilterParseException {
    LocalDate now = LocalDate.now();
    Filter filter = FilterBuilder.create().notEqual("date.date", now).build();
    Filter expected = new Filter("date.date NE \"" + DateTimeFormatter.ISO_LOCAL_DATE.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testNotnotEqualStringLocalDateTime() throws FilterParseException {
    LocalDateTime now = LocalDateTime.now();
    Filter filter = FilterBuilder.create().notEqual("date.date", now).build();
    Filter expected = new Filter("date.date NE \"" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now) + "\"");
    // TODO: dates are parsed to strings, for now use string comparison
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testNotnotEqualStringInteger() throws FilterParseException {
    int i = 10;
    Filter filter = FilterBuilder.create().notEqual("int.int", i).build();
    Filter expected = new Filter("int.int NE 10");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testNotnotEqualStringLong() throws FilterParseException {
    long i = 10l;
    Filter filter = FilterBuilder.create().notEqual("long.long", i).build();
    Filter expected = new Filter("long.long NE 10");
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testNotnotEqualStringFloat() throws FilterParseException {
    float i = 10.2f;
    Filter filter = FilterBuilder.create().notEqual("float.float", i).build();
    Filter expected = new Filter("float.float NE 10.2");
    assertThat(filter.getFilter()).isEqualTo(expected.getFilter());
  }

  @Test
  public void testNotnotEqualStringDouble() throws FilterParseException {
    double i = 10.2;
    Filter filter = FilterBuilder.create().notEqual("double.double", i).build();
    Filter expected = new Filter("double.double NE 10.2");
    assertThat(filter).isEqualTo(expected);
  }

  @Test
  public void testNotEqualNull() throws FilterParseException {
    Filter filter = FilterBuilder.create().notEqualNull("null.null").build();
    Filter expected = new Filter("null.null NE null");
    assertThat(filter).isEqualTo(expected);
  }
}
