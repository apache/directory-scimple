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

package org.apache.directory.scim.spec.resources;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneNumberJsonTest {

  @Test
  public void testPhoneNumberJson() throws Exception {
    PhoneNumber phoneNumber = new PhoneNumber();
    phoneNumber.setValue("tel:+12083869507");
    
    ObjectMapper objectMapper = getObjectMapper();
    
    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(phoneNumber);
    
    log.info(json);
    
    PhoneNumber readValue = objectMapper.readValue(json, PhoneNumber.class);
    
    Assert.assertEquals(phoneNumber.getNumber(), readValue.getNumber());
    Assert.assertEquals(phoneNumber.getExtension(), readValue.getExtension());
    Assert.assertEquals(phoneNumber.isDomainPhoneContext(), readValue.isDomainPhoneContext());
    Assert.assertEquals(phoneNumber.isGlobalNumber(), readValue.isGlobalNumber());
    Assert.assertEquals(phoneNumber.getPhoneContext(), readValue.getPhoneContext());
    Assert.assertEquals(phoneNumber.getSubAddress(), readValue.getSubAddress());
    Assert.assertEquals(phoneNumber.getPrimary(), readValue.getPrimary());
    Assert.assertEquals(phoneNumber.getDisplay(), readValue.getDisplay());
    Assert.assertEquals(phoneNumber.getType(), readValue.getType());
    Assert.assertEquals(phoneNumber.getValue(), readValue.getValue());

    
    log.info("done");
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
    objectMapper.registerModule(jaxbAnnotationModule);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
    AnnotationIntrospector pair = new AnnotationIntrospectorPair(jaxbIntrospector, jacksonIntrospector);
    objectMapper.setAnnotationIntrospector(pair);

    objectMapper.setSerializationInclusion(Include.NON_NULL);
    return objectMapper;
  }
  
}
