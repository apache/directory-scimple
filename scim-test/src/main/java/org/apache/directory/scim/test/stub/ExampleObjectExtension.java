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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;

import java.io.Serializable;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "ExampleObject", id = ExampleObjectExtension.URN, description = "Example Object Extensions.")
public class ExampleObjectExtension implements ScimExtension {

  private static final long serialVersionUID = -5398090056271556423L;

  public static final String URN = "urn:ietf:params:scim:schemas:extension:example:2.0:Object";

  @ScimAttribute(returned = Returned.ALWAYS)
  @XmlElement
  private String valueAlways;

  @ScimAttribute(returned = Returned.DEFAULT)
  @XmlElement
  private String valueDefault;

  @ScimAttribute(returned = Returned.NEVER)
  @XmlElement
  private String valueNever;

  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private String valueRequest;

  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private ComplexObject valueComplex;

  @ScimAttribute()
  @XmlElement
  private List<String> list;

  @ScimAttribute
  @XmlElement
  private List<Order> enumList;

  @ScimAttribute
  @XmlElement
  private Subobject subobject;

  @Override
  public String getUrn() {
    return URN;
  }

  public String getValueAlways() {
    return this.valueAlways;
  }

  public String getValueDefault() {
    return this.valueDefault;
  }

  public String getValueNever() {
    return this.valueNever;
  }

  public String getValueRequest() {
    return this.valueRequest;
  }

  public ComplexObject getValueComplex() {
    return this.valueComplex;
  }

  public List<String> getList() {
    return this.list;
  }

  public List<Order> getEnumList() {
    return this.enumList;
  }

  public Subobject getSubobject() {
    return this.subobject;
  }

  public ExampleObjectExtension setValueAlways(String valueAlways) {
    this.valueAlways = valueAlways;
    return this;
  }

  public ExampleObjectExtension setValueDefault(String valueDefault) {
    this.valueDefault = valueDefault;
    return this;
  }

  public ExampleObjectExtension setValueNever(String valueNever) {
    this.valueNever = valueNever;
    return this;
  }

  public ExampleObjectExtension setValueRequest(String valueRequest) {
    this.valueRequest = valueRequest;
    return this;
  }

  public ExampleObjectExtension setValueComplex(ComplexObject valueComplex) {
    this.valueComplex = valueComplex;
    return this;
  }

  public ExampleObjectExtension setList(List<String> list) {
    this.list = list;
    return this;
  }

  public ExampleObjectExtension setEnumList(List<Order> enumList) {
    this.enumList = enumList;
    return this;
  }

  public ExampleObjectExtension setSubobject(Subobject subobject) {
    this.subobject = subobject;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ExampleObjectExtension)) return false;
    final ExampleObjectExtension other = (ExampleObjectExtension) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$valueAlways = this.getValueAlways();
    final Object other$valueAlways = other.getValueAlways();
    if (this$valueAlways == null ? other$valueAlways != null : !this$valueAlways.equals(other$valueAlways))
      return false;
    final Object this$valueDefault = this.getValueDefault();
    final Object other$valueDefault = other.getValueDefault();
    if (this$valueDefault == null ? other$valueDefault != null : !this$valueDefault.equals(other$valueDefault))
      return false;
    final Object this$valueNever = this.getValueNever();
    final Object other$valueNever = other.getValueNever();
    if (this$valueNever == null ? other$valueNever != null : !this$valueNever.equals(other$valueNever)) return false;
    final Object this$valueRequest = this.getValueRequest();
    final Object other$valueRequest = other.getValueRequest();
    if (this$valueRequest == null ? other$valueRequest != null : !this$valueRequest.equals(other$valueRequest))
      return false;
    final Object this$valueComplex = this.getValueComplex();
    final Object other$valueComplex = other.getValueComplex();
    if (this$valueComplex == null ? other$valueComplex != null : !this$valueComplex.equals(other$valueComplex))
      return false;
    final Object this$list = this.getList();
    final Object other$list = other.getList();
    if (this$list == null ? other$list != null : !this$list.equals(other$list)) return false;
    final Object this$enumList = this.getEnumList();
    final Object other$enumList = other.getEnumList();
    if (this$enumList == null ? other$enumList != null : !this$enumList.equals(other$enumList)) return false;
    final Object this$subobject = this.getSubobject();
    final Object other$subobject = other.getSubobject();
    if (this$subobject == null ? other$subobject != null : !this$subobject.equals(other$subobject)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ExampleObjectExtension;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $valueAlways = this.getValueAlways();
    result = result * PRIME + ($valueAlways == null ? 43 : $valueAlways.hashCode());
    final Object $valueDefault = this.getValueDefault();
    result = result * PRIME + ($valueDefault == null ? 43 : $valueDefault.hashCode());
    final Object $valueNever = this.getValueNever();
    result = result * PRIME + ($valueNever == null ? 43 : $valueNever.hashCode());
    final Object $valueRequest = this.getValueRequest();
    result = result * PRIME + ($valueRequest == null ? 43 : $valueRequest.hashCode());
    final Object $valueComplex = this.getValueComplex();
    result = result * PRIME + ($valueComplex == null ? 43 : $valueComplex.hashCode());
    final Object $list = this.getList();
    result = result * PRIME + ($list == null ? 43 : $list.hashCode());
    final Object $enumList = this.getEnumList();
    result = result * PRIME + ($enumList == null ? 43 : $enumList.hashCode());
    final Object $subobject = this.getSubobject();
    result = result * PRIME + ($subobject == null ? 43 : $subobject.hashCode());
    return result;
  }

  public String toString() {
    return "ExampleObjectExtension(valueAlways=" + this.getValueAlways() + ", valueDefault=" + this.getValueDefault() + ", valueNever=" + this.getValueNever() + ", valueRequest=" + this.getValueRequest() + ", valueComplex=" + this.getValueComplex() + ", list=" + this.getList() + ", enumList=" + this.getEnumList() + ", subobject=" + this.getSubobject() + ")";
  }

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  public static class ComplexObject implements Serializable {

    private static final long serialVersionUID = 2822581434679824690L;

    @ScimAttribute(description = "The \"id\" of the complex object.")
    @XmlElement
    private String value;

    @ScimAttribute(mutability = Mutability.READ_ONLY, description = "displayName of the object.")
    @XmlElement
    private String displayName;

    public ComplexObject() {
    }

    public String getValue() {
      return this.value;
    }

    public String getDisplayName() {
      return this.displayName;
    }

    public ComplexObject setValue(String value) {
      this.value = value;
      return this;
    }

    public ComplexObject setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof ComplexObject)) return false;
      final ComplexObject other = (ComplexObject) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$value = this.getValue();
      final Object other$value = other.getValue();
      if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
      final Object this$displayName = this.getDisplayName();
      final Object other$displayName = other.getDisplayName();
      if (this$displayName == null ? other$displayName != null : !this$displayName.equals(other$displayName))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof ComplexObject;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $value = this.getValue();
      result = result * PRIME + ($value == null ? 43 : $value.hashCode());
      final Object $displayName = this.getDisplayName();
      result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
      return result;
    }

    public String toString() {
      return "ExampleObjectExtension.ComplexObject(value=" + this.getValue() + ", displayName=" + this.getDisplayName() + ")";
    }
  }
}
