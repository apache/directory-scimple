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

package org.apache.directory.scim.test.stub;

import jakarta.xml.bind.annotation.XmlElement;
import org.apache.directory.scim.spec.annotation.ScimAttribute;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Subobject implements Serializable {

  private static final long serialVersionUID = -8081556701833520316L;

  @ScimAttribute
  @XmlElement
  private String string1;

  @ScimAttribute
  @XmlElement
  private String string2;

  @ScimAttribute
  @XmlElement
  private Boolean boolean1;

  @ScimAttribute
  @XmlElement
  private Boolean boolean2;

  @ScimAttribute
  @XmlElement
  private List<String> list1;

  @ScimAttribute
  @XmlElement
  private List<String> list2;

  @ScimAttribute
  @XmlElement
  private byte[] binary1;

  public String getString1() {
    return this.string1;
  }

  public String getString2() {
    return this.string2;
  }

  public Boolean getBoolean1() {
    return this.boolean1;
  }

  public Boolean getBoolean2() {
    return this.boolean2;
  }

  public List<String> getList1() {
    return this.list1;
  }

  public List<String> getList2() {
    return this.list2;
  }

  public Subobject setString1(String string1) {
    this.string1 = string1;
    return this;
  }

  public Subobject setString2(String string2) {
    this.string2 = string2;
    return this;
  }

  public Subobject setBoolean1(Boolean boolean1) {
    this.boolean1 = boolean1;
    return this;
  }

  public Subobject setBoolean2(Boolean boolean2) {
    this.boolean2 = boolean2;
    return this;
  }

  public Subobject setList1(List<String> list1) {
    this.list1 = list1;
    return this;
  }

  public Subobject setList2(List<String> list2) {
    this.list2 = list2;
    return this;
  }

  public byte[] getBinary1() {
    return binary1;
  }

  public Subobject setBinary1(byte[] binary1) {
    this.binary1 = binary1;
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;

    Subobject subobject = (Subobject) object;

    if (!Objects.equals(string1, subobject.string1)) return false;
    if (!Objects.equals(string2, subobject.string2)) return false;
    if (!Objects.equals(boolean1, subobject.boolean1)) return false;
    if (!Objects.equals(boolean2, subobject.boolean2)) return false;
    if (!Objects.equals(list1, subobject.list1)) return false;
    if (!Objects.equals(list2, subobject.list2)) return false;
      return Arrays.equals(binary1, subobject.binary1);
  }

  @Override
  public int hashCode() {
    int result = string1 != null ? string1.hashCode() : 0;
    result = 31 * result + (string2 != null ? string2.hashCode() : 0);
    result = 31 * result + (boolean1 != null ? boolean1.hashCode() : 0);
    result = 31 * result + (boolean2 != null ? boolean2.hashCode() : 0);
    result = 31 * result + (list1 != null ? list1.hashCode() : 0);
    result = 31 * result + (list2 != null ? list2.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(binary1);
    return result;
  }
}
