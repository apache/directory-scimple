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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;

import static org.apache.directory.scim.spec.ComplexTypeExtension.SCHEMA_URN;

@XmlRootElement(name = "ComplexTypeExtension", namespace = "https://directory.apache.org/scimple/test/extensions")
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(id = SCHEMA_URN, description = "Schema with complex type field", name = "ComplexTypeExtension", required = true)
public class ComplexTypeExtension implements ScimExtension {

  public static final String SCHEMA_URN = "urn:mem:params:scim:schemas:extension:ComplexTypeExtension";

  @ScimAttribute(description = "A complex type")
  @XmlElement
  private ComplexType complexType;

  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

  public ComplexType getComplexType() {
    return this.complexType;
  }

  public ComplexTypeExtension setComplexType(ComplexType complexType) {
    this.complexType = complexType;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ComplexTypeExtension other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$complexType = this.getComplexType();
    final Object other$complexType = other.getComplexType();
    if (this$complexType == null ? other$complexType != null : !this$complexType.equals(other$complexType))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ComplexTypeExtension;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $complexType = this.getComplexType();
    result = result * PRIME + ($complexType == null ? 43 : $complexType.hashCode());
    return result;
  }

  public String toString() {
    return "ComplexTypeExtension(complexType=" + this.getComplexType() + ")";
  }
}
