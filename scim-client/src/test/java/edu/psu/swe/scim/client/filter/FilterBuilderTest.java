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

package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import junitparams.JUnitParamsRunner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class FilterBuilderTest {

//  address.type EQ "work"
//  address.primary EQ true
//  address.primary EQ false
//  address.primary EQ null
//  address.number EQ 123
//  address.number EQ 123 OR address.primary EQ null
//  address.number EQ 123 AND address.primary EQ null
//  address.number EQ 123 OR NOT(address.primary EQ null)
//  NOT(address.number EQ 123) OR NOT(address.primary EQ null)
//  (address.number EQ 123) OR (address.primary EQ null)
//  ((address.number EQ 123) OR (address.primary EQ null))
//  address.primary PR
//  NOT(address.primary PR)
//  urn:scimscim:Custom+2.0:address EQ "work"
//  urn:justlongenoughnidxxxxxxxxxxxxxxx:Custom:address EQ "work"
//  urn:scim:Custom()+,-.:=@;$_!*\nss:address EQ "work"
//  address.type2_-3 EQ "work"
//
//  Invalid:
//  address.type EQ "work" MAYBE address.type EQ "home"
//  address.type YZ "work"
//  address.type EQ work
//  address..type EQ "work"
//  .address.type EQ "work"
//  address[street[apt EQ "100"]]
//  address[type EQ "work" AND street[apt EQ "100"]]
//  urn:scim+scim:Custom:address EQ "work"
//  urn:scim+scim:Custom.address EQ "work"
//  urn:reallyreallyreallyreallylongnidxx:Custom:address EQ "work"
//  address.type EQ "work" OR "home"
//  NOT(address.primary)
//  address^.type EQ "work"
//  address,type EQ "work"
//  address.2type EQ "work"

  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  @Test
  public void testSimpleAnd() throws UnsupportedEncodingException, FilterParseException {
  
    String encoded = FilterClient.builder().equalTo("name.givenName", "Bilbo").and().equalTo("name.familyName", "Baggins").toString();
  
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testSimpleOr() throws UnsupportedEncodingException, FilterParseException {
  
    String encoded = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.familyName", "Baggins").toString();
  
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testAndOrChain() throws UnsupportedEncodingException, FilterParseException {
  
    String encoded = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins").toString();
  
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testAndOrChainComplex() throws UnsupportedEncodingException, FilterParseException {
  
    String encoded = FilterClient.builder().equalTo("name.givenName", "Bilbo").and(FilterClient.builder().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins").filter()).toString();
  
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testOrAndChainComplex() throws UnsupportedEncodingException, FilterParseException {
  
    String encoded = FilterClient.builder().equalTo("name.givenName", "Bilbo").or(FilterClient.builder().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins").filter()).toString();
  
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testComplexAnd() throws UnsupportedEncodingException, FilterParseException {
  
    FilterClient.Builder b1 = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");
    FilterClient.Builder b2 = FilterClient.builder().equalTo("address.streetAddress", "Underhill").or().equalTo("address.streetAddress", "Overhill").and().equalTo("address.postalCode", "16803");
    
    String encoded = FilterClient.builder().and(b1.filter(), b2.filter()).toString();
    
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testNot() throws UnsupportedEncodingException, FilterParseException {
    FilterClient.Builder b1 = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");

    String encoded = FilterClient.builder().not(b1.filter()).toString();
    
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testAttributeContains() throws UnsupportedEncodingException, FilterParseException {
    
    FilterClient.Builder b1 = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");
    FilterClient.Builder b2 = FilterClient.builder().attributeHas("address", b1.filter());
    
    String encoded = b2.toString();
    
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testAttributeContainsEmbedded() throws UnsupportedEncodingException, FilterParseException {
    
    thrown.expect(FilterParseException.class);
    FilterClient.Builder b1 = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");
    FilterClient.Builder b2 = FilterClient.builder().attributeHas("address", b1.filter());
    
    FilterClient.Builder b3 = FilterClient.builder().attributeHas("address", b2.filter());
   
    String encoded = b3.toString();
    
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  
  @Test
  public void testAttributeContainsDeeplyEmbedded() throws UnsupportedEncodingException, FilterParseException {
    
    thrown.expect(FilterParseException.class);
    FilterClient.Builder b1 = FilterClient.builder().equalTo("name.givenName", "Bilbo").or().equalTo("name.givenName", "Frodo").and().equalTo("name.familyName", "Baggins");
    FilterClient.Builder b2 = FilterClient.builder().attributeHas("address", b1.filter());
    FilterClient.Builder b3 = FilterClient.builder().equalTo("name.giveName", "Gandalf").and(b2.filter());
    FilterClient.Builder b4 = FilterClient.builder().attributeHas("address", b3.filter());
   
    String encoded = b4.toString();
    
    String decoded = decode(encoded);
    Filter filter = new Filter(decoded); 
  }
  //@Test
//  public void testNotSingleArg() throws UnsupportedEncodingException, FilterParseException {
// 
//     String encoded = filterBuilder.not(attributeComparisonExpression, LogicalOperator.AND, attributeComparisonExpression2).toString();
// 
//     String decoded = decode(encoded);
//     Filter filter = new Filter(decoded);
//  }
//
//  //@Test
//  public void testAnd() throws UnsupportedEncodingException, FilterParseException {
//
//    String encoded = filterBuilder.and(attributeComparisonExpression, attributeComparisonExpression2).toString();
//    
//    String decoded = decode(encoded);
//    Filter filter = new Filter(decoded);
//  }
//
//  //@Test
//  public void testOr() throws UnsupportedEncodingException, FilterParseException {
//
//    String encoded = filterBuilder.equalTo("addresses.postalCode", "16801")
//                                  .and()
//                                  .or(attributeComparisonExpression, attributeComparisonExpression2)
//                                  .toString();
//
//    log.info(encoded);
//    
//    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
//    
//    log.info(decoded);
//    
//    Filter filter = new Filter(decoded);
//  }
  
  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }

}
