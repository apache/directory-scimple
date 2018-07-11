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

package org.apache.directory.scim.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Uniqueness;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ScimAttribute {
  
  //This is an ugly but necessary work around
  //We need something to determine which canonical value
  //parameter is desired and we can't use null so we had
  //to create this little empty enum as a place holder
  //for assignment checks.
  enum NoOp {
  }
  
  String name() default "";
  boolean required() default false;
  
  //These two canonical attributes should be mutually exclusive, if both are 
  //present we will reject the registered provider
  Class<? extends Enum<?>>  canonicalValueEnum() default NoOp.class;
  String [] canonicalValueList() default "";
  
  boolean caseExact() default false;
  Mutability mutability() default Mutability.READ_WRITE;
  Returned returned() default Returned.DEFAULT;
  Uniqueness uniqueness() default Uniqueness.NONE;
  String [] referenceTypes() default "";
  String description() default "";
}