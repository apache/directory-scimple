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
import java.util.List;

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

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Subobject)) return false;
    final Subobject other = (Subobject) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$string1 = this.getString1();
    final Object other$string1 = other.getString1();
    if (this$string1 == null ? other$string1 != null : !this$string1.equals(other$string1)) return false;
    final Object this$string2 = this.getString2();
    final Object other$string2 = other.getString2();
    if (this$string2 == null ? other$string2 != null : !this$string2.equals(other$string2)) return false;
    final Object this$boolean1 = this.getBoolean1();
    final Object other$boolean1 = other.getBoolean1();
    if (this$boolean1 == null ? other$boolean1 != null : !this$boolean1.equals(other$boolean1)) return false;
    final Object this$boolean2 = this.getBoolean2();
    final Object other$boolean2 = other.getBoolean2();
    if (this$boolean2 == null ? other$boolean2 != null : !this$boolean2.equals(other$boolean2)) return false;
    final Object this$list1 = this.getList1();
    final Object other$list1 = other.getList1();
    if (this$list1 == null ? other$list1 != null : !this$list1.equals(other$list1)) return false;
    final Object this$list2 = this.getList2();
    final Object other$list2 = other.getList2();
    if (this$list2 == null ? other$list2 != null : !this$list2.equals(other$list2)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Subobject;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $string1 = this.getString1();
    result = result * PRIME + ($string1 == null ? 43 : $string1.hashCode());
    final Object $string2 = this.getString2();
    result = result * PRIME + ($string2 == null ? 43 : $string2.hashCode());
    final Object $boolean1 = this.getBoolean1();
    result = result * PRIME + ($boolean1 == null ? 43 : $boolean1.hashCode());
    final Object $boolean2 = this.getBoolean2();
    result = result * PRIME + ($boolean2 == null ? 43 : $boolean2.hashCode());
    final Object $list1 = this.getList1();
    result = result * PRIME + ($list1 == null ? 43 : $list1.hashCode());
    final Object $list2 = this.getList2();
    result = result * PRIME + ($list2 == null ? 43 : $list2.hashCode());
    return result;
  }

  public String toString() {
    return "Subobject(string1=" + this.getString1() + ", string2=" + this.getString2() + ", boolean1=" + this.getBoolean1() + ", boolean2=" + this.getBoolean2() + ", list1=" + this.getList1() + ", list2=" + this.getList2() + ")";
  }
}
