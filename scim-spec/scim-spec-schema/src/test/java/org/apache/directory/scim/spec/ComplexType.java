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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.directory.scim.spec.annotation.ScimAttribute;

@XmlType(name = "complexType")
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexType {

  @XmlElement
  @ScimAttribute(description = "First attribute")
  String firstAttribute;

  public String getFirstAttribute() {
    return this.firstAttribute;
  }

  public ComplexType setFirstAttribute(String firstAttribute) {
    this.firstAttribute = firstAttribute;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ComplexType)) return false;
    final ComplexType other = (ComplexType) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$firstAttribute = this.getFirstAttribute();
    final Object other$firstAttribute = other.getFirstAttribute();
    if (this$firstAttribute == null ? other$firstAttribute != null : !this$firstAttribute.equals(other$firstAttribute))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ComplexType;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $firstAttribute = this.getFirstAttribute();
    result = result * PRIME + ($firstAttribute == null ? 43 : $firstAttribute.hashCode());
    return result;
  }

  public String toString() {
    return "ComplexType(firstAttribute=" + this.getFirstAttribute() + ")";
  }
}
