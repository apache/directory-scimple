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

package org.apache.directory.scim.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class ObjectMapperFactory {

  private final static ObjectMapper objectMapper = createObjectMapper();

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    objectMapper.registerModule(new JaxbAnnotationModule());

    AnnotationIntrospector pair = new AnnotationIntrospectorPair(
      new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()),
      new JacksonAnnotationIntrospector());
    objectMapper.setAnnotationIntrospector(pair);
    objectMapper.registerModule(new Jdk8Module());

    return objectMapper;
  }
}
