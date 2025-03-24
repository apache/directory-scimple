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

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

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

  DateTimeFormatter iso8601DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

  public Mapper() {
  }

  /**
   * Converts an ISO 8601 DateTime string into the equivalent Java Date object.
   *
   * @param isodate the ISO 8601 DateTime to be converted.
   * @return the equivalent Java Instant object.
   */
  public Instant convertDateTime(String isodate) {
    TemporalAccessor temporal = iso8601DateTimeFormatter.parse(isodate);
    return Instant.from(temporal);
  }
}
