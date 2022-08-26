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

package org.apache.directory.scim.server.repository;

import java.util.Comparator;
import java.util.Set;

import org.apache.directory.scim.spec.resources.TypedAttribute;

public class PrioritySortingComparitor implements Comparator<Object> {

  private Set<Object> priorities;

  public PrioritySortingComparitor(Set<Object> priorities) {
    this.priorities = priorities;
  }

  @Override
  public int compare(Object o1, Object o2) {
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }

    Comparable c1 = getComparableValue(o1);
    Comparable c2 = getComparableValue(o2);
    
    boolean o1Priority = priorities.contains(c1);
    boolean o2Priority = priorities.contains(c2);

    if (o1Priority == o2Priority) {
      return c1.compareTo(c2);
    } else {
      return o1Priority ? -1 : 1;
    }

  }

  public static Comparable getComparableValue(Object obj) {
    if (obj instanceof TypedAttribute) {
      TypedAttribute typed = (TypedAttribute) obj;
      return typed.getType();
    } else if (obj instanceof Comparable) {
      return (Comparable) obj;
    } else {
      return obj.toString();
    }
  }
}
