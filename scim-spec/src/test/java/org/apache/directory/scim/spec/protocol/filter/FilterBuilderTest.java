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

package org.apache.directory.scim.spec.protocol.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.protocol.search.Filter;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class FilterBuilderTest {

  @Test
  public void testSimpleAnd() throws FilterParseException {
    Filter filter = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .and()
      .equalTo("name.familyName", "Baggins")
      .build();
    assertThat(filter).isEqualTo(new Filter("name.givenName EQ \"Bilbo\" AND name.familyName EQ \"Baggins\""));
  }
  
  @Test
  public void testSimpleOr() throws FilterParseException {
    Filter filter = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or()
      .equalTo("name.familyName", "Baggins").build();

    assertThat(filter).isEqualTo(new Filter("name.givenName EQ \"Bilbo\" OR name.familyName EQ \"Baggins\""));
  }
  
  @Test
  public void testAndOrChain() throws FilterParseException, UnsupportedEncodingException {

    Filter filter = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or()
      .equalTo("name.givenName", "Frodo")
      .and()
      .equalTo("name.familyName", "Baggins").build();

    Filter expected = new Filter("(name.givenName EQ \"Bilbo\" OR name.givenName EQ \"Frodo\") AND name.familyName EQ \"Baggins\"");

    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testAndOrChainComplex() throws FilterParseException, UnsupportedEncodingException {

    Filter filter = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .and(FilterBuilder.create()
        .equalTo("name.givenName", "Frodo")
        .and()
        .equalTo("name.familyName", "Baggins")
        .build())
      .build();

    Filter expected = new Filter("name.givenName EQ \"Bilbo\" AND (name.givenName EQ \"Frodo\" AND name.familyName EQ \"Baggins\")");
    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testOrAndChainComplex() throws FilterParseException {

    Filter filter = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or(FilterBuilder.create()
        .equalTo("name.givenName", "Frodo")
        .and()
        .equalTo("name.familyName", "Baggins")
        .build())
      .build();

    Filter expected = new Filter("name.givenName EQ \"Bilbo\" OR (name.givenName EQ \"Frodo\" AND name.familyName EQ \"Baggins\")");

    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testComplexAnd() throws FilterParseException {
  
    FilterBuilder b1 = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or()
      .equalTo("name.givenName", "Frodo")
      .and()
      .equalTo("name.familyName", "Baggins");

    FilterBuilder b2 = FilterBuilder.create().equalTo("address.streetAddress", "Underhill")
      .or()
      .equalTo("address.streetAddress", "Overhill")
      .and()
      .equalTo("address.postalCode", "16803");
    
    Filter filter = FilterBuilder.create().and(b1.build(), b2.build()).build();

    Filter expected = new Filter("((name.givenName EQ \"Bilbo\" OR name.givenName EQ \"Frodo\") AND name.familyName EQ \"Baggins\") AND ((address.streetAddress EQ \"Underhill\" OR address.streetAddress EQ \"Overhill\") AND address.postalCode EQ \"16803\")");
    
    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testNot() throws FilterParseException {
    FilterBuilder b1 = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or()
      .equalTo("name.givenName", "Frodo")
      .and()
      .equalTo("name.familyName", "Baggins");

    Filter filter = FilterBuilder.create().not(b1.build()).build();
    Filter expected = new Filter("NOT((name.givenName EQ \"Bilbo\" OR name.givenName EQ \"Frodo\") AND name.familyName EQ \"Baggins\")");
    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testAttributeContains() throws FilterParseException {
    
    FilterBuilder b1 = FilterBuilder.create()
      .equalTo("address.type", "work");
    FilterBuilder b2 = FilterBuilder.create()
      .attributeHas("address", b1.build());
    
    Filter filter = b2.build();
    Filter expected = new Filter("address[type EQ \"work\"]");

    assertThat(filter).isEqualTo(expected);
  }
  
  @Test
  public void testAttributeContainsEmbedded() throws FilterParseException {

    FilterBuilder b1 = FilterBuilder.create()
      .equalTo("name.givenName", "Bilbo")
      .or()
      .equalTo("name.givenName", "Frodo")
      .and()
      .equalTo("name.familyName", "Baggins");

    FilterBuilder b2 = FilterBuilder.create().attributeHas("address", b1.build());
    
    FilterBuilder b3 = FilterBuilder.create().attributeHas("address", b2.build());

    // TODO: I'm not sure why this test exists in the Client
    // This should probably be a parsing test, and maybe the FilterBuilder should be smarter and throw an
    // exception when this type Filter is created, instead of just when parsed.
    String filterString = b3.build().toString();
    assertThrows(FilterParseException.class, () -> new Filter(filterString));
  }
  
  @Test
  public void testAttributeContainsDeeplyEmbedded() throws FilterParseException {

      FilterBuilder b1 = FilterBuilder.create().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");
      FilterBuilder b2 = FilterBuilder.create().attributeHas("address", b1.build());
      FilterBuilder b3 = FilterBuilder.create().equalTo("name.giveName", "Gandalf").and(b2.build());
      FilterBuilder b4 = FilterBuilder.create().attributeHas("address", b3.build());

      assertThrows(FilterParseException.class, () -> new Filter( b4.toString()));
  }
}
