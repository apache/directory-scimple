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

package edu.psu.swe.scim.spec.protocol.data;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class PatchOperationPathTest {

  public String[] pathValues() {
    return new String[] { "members",
        "name.familyName",
        "addresses[type eq \"work\"]",
        "members[value eq \"2819c223-7f76-453a-919d-413861904646\"]",
        "members[value eq \"2819c223-7f76-453a-919d-413861904646\"].displayName" };
  }

  @Test
  @Parameters(method = "pathValues")
  public void testPathParsing(String value) throws Exception {
    PatchOperationPath path = new PatchOperationPath(value);
    log.info("ValuePathExpression: " + path.getValuePathExpression());
    
    String result = path.toString();
    log.info(result);
    Assert.assertNotNull(path.getValuePathExpression());
    Assert.assertEquals(value.toLowerCase(), result.toLowerCase());
  }

}
