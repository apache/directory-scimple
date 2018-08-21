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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.search.Filter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FilterBuilderStringTests {

  @Test
  public void testEndsWith() throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().endsWith("address.streetAddress", "Way").toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testStartsWith()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().startsWith("address.streetAddress", "133").toString();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testContains()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = FilterClient.builder().contains("address.streetAddress", "MacDuff").toString();
    Filter filter = new Filter(decode(encoded));
  }

  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }
}
