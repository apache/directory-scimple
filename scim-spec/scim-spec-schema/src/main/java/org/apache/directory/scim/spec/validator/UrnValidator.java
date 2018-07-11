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

package org.apache.directory.scim.spec.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrnValidator implements ConstraintValidator<Urn, String> {

  private static final String URN_RFC2141_REGEX = "^urn:[a-zA-Z0-9][a-zA-Z0-9-]{0,31}:(?:[a-zA-Z0-9()+,\\-.:=@;$_!*']|%[0-9a-fA-F]{2})+$";

  @Override
  public void initialize(Urn validator) {
  }

  @Override
  public boolean isValid(String urn, ConstraintValidatorContext context) {
    if (urn == null || urn.isEmpty()) {
      return true;
    }

    return urn.matches(URN_RFC2141_REGEX);
  }
}
