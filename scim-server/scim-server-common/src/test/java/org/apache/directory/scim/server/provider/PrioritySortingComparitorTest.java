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

package org.apache.directory.scim.server.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PrioritySortingComparitorTest {

  @Test
  public void testSorting() throws Exception {
    Set<Object> priorities = new HashSet<>();
    priorities.add("1P");
    priorities.add("2P");
    PrioritySortingComparitor comparitor = new PrioritySortingComparitor(priorities);
    List<String> list = Arrays.asList("1", "2", "1P", "2P", "3", "4");
    Collections.sort(list, comparitor);
    System.out.println(list);
    
    Assertions.assertThat(list).hasSameElementsAs(Arrays.asList("1P", "2P", "1", "2", "3", "4"));
  }

  @Test
  public void testSorting2() throws Exception {
    Set<Object> priorities = new HashSet<>();
    priorities.add("home");
    priorities.add("work");
    PrioritySortingComparitor comparitor = new PrioritySortingComparitor(priorities);
    List<String> list = Arrays.asList("work", "local", "home");
    Collections.sort(list, comparitor);
    System.out.println(list);
    
    Assertions.assertThat(list).hasSameElementsAs(Arrays.asList("home", "work", "local"));
  }
  
}
