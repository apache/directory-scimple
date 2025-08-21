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
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * An extension that can be used to validate schema generation for each type of attribute.
 */
@XmlRootElement( name = "AllSchemaTypesExtension", namespace = "https://directory.apache.org/scimple/test/extensions" )
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(id = AllSchemaTypesExtension.SCHEMA_URN, description="All Schema Attribute Types", name="AllSchemaTypesExtension", required=true)
public class AllSchemaTypesExtension implements ScimExtension {
  
  public static final String  SCHEMA_URN = "urn:mem:params:scim:schemas:extension:AllSchemaTypesExtension";

  @ScimAttribute(description = "One String")
  @XmlElement
  private String string1;

  @ScimAttribute(caseExact = true)
  @XmlElement
  private String string2;

  @ScimAttribute
  @XmlElement
  private List<String> stringList1;

  @ScimAttribute(required = true)
  @XmlElement
  private Boolean boolean1;

  @ScimAttribute(uniqueness = Schema.Attribute.Uniqueness.SERVER)
  @XmlElement
  private List<Boolean> booleanList1;

  @ScimAttribute(required = true)
  @XmlElement
  private boolean boolean2;

  @ScimAttribute(mutability = Schema.Attribute.Mutability.IMMUTABLE)
  @XmlElement
  private boolean[] booleanArray1;

  @ScimAttribute
  @XmlElement
  private Double decimal1;

  @ScimAttribute(required = true)
  @XmlElement
  private double decimal2;

  @ScimAttribute
  @XmlElement
  private List<Double> decimalList1;

  @ScimAttribute
  @XmlElement
  private double[] decimalArray1;

  @ScimAttribute
  @XmlElement
  private Integer integer1;

  @ScimAttribute(required = true)
  @XmlElement
  private int integer2;

  @ScimAttribute
  @XmlElement
  private List<Integer> integerList1;

  @ScimAttribute
  @XmlElement
  private int[] integerArray1;

  @ScimAttribute
  @XmlElement
  private Date date1;

  @ScimAttribute
  @XmlElement
  private List<Date> dateList1;

  @ScimAttribute
  @XmlElement
  private Date[] dateArray1;

  @ScimAttribute
  @XmlElement
  private Instant instant1;

  @ScimAttribute
  @XmlElement
  private List<Instant> instantList1;

  @ScimAttribute
  @XmlElement
  private Instant[] instantArray1;

  @ScimAttribute
  @XmlElement
  private byte[] binary1;

  @ScimAttribute
  @XmlElement
  private List<byte[]> binaryList1;

  @ScimAttribute(referenceTypes = {"one", "two"})
  @XmlElement
  private String ref1;

  @ScimAttribute(name = "$ref", referenceTypes = {"three", "four"})
  @XmlElement
  private String ref2;

  @ScimAttribute(referenceTypes = {"one", "two", "three"})
  @XmlElement
  private List<String> refList1;

  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

  public String getString1() {
    return this.string1;
  }

  public String getString2() {
    return this.string2;
  }

  public List<String> getStringList1() {
    return this.stringList1;
  }

  public Boolean getBoolean1() {
    return this.boolean1;
  }

  public List<Boolean> getBooleanList1() {
    return this.booleanList1;
  }

  public boolean isBoolean2() {
    return this.boolean2;
  }

  public boolean[] getBooleanArray1() {
    return this.booleanArray1;
  }

  public Double getDecimal1() {
    return this.decimal1;
  }

  public double getDecimal2() {
    return this.decimal2;
  }

  public List<Double> getDecimalList1() {
    return this.decimalList1;
  }

  public double[] getDecimalArray1() {
    return this.decimalArray1;
  }

  public Integer getInteger1() {
    return this.integer1;
  }

  public int getInteger2() {
    return this.integer2;
  }

  public List<Integer> getIntegerList1() {
    return this.integerList1;
  }

  public int[] getIntegerArray1() {
    return this.integerArray1;
  }

  public Date getDate1() {
    return this.date1;
  }

  public List<Date> getDateList1() {
    return this.dateList1;
  }

  public Date[] getDateArray1() {
    return this.dateArray1;
  }

  public Instant getInstant1() {
    return this.instant1;
  }

  public List<Instant> getInstantList1() {
    return this.instantList1;
  }

  public Instant[] getInstantArray1() {
    return this.instantArray1;
  }

  public byte[] getBinary1() {
    return this.binary1;
  }

  public List<byte[]> getBinaryList1() {
    return this.binaryList1;
  }

  public String getRef1() {
    return this.ref1;
  }

  public String getRef2() {
    return this.ref2;
  }

  public List<String> getRefList1() {
    return this.refList1;
  }

  public AllSchemaTypesExtension setString1(String string1) {
    this.string1 = string1;
    return this;
  }

  public AllSchemaTypesExtension setString2(String string2) {
    this.string2 = string2;
    return this;
  }

  public AllSchemaTypesExtension setStringList1(List<String> stringList1) {
    this.stringList1 = stringList1;
    return this;
  }

  public AllSchemaTypesExtension setBoolean1(Boolean boolean1) {
    this.boolean1 = boolean1;
    return this;
  }

  public AllSchemaTypesExtension setBooleanList1(List<Boolean> booleanList1) {
    this.booleanList1 = booleanList1;
    return this;
  }

  public AllSchemaTypesExtension setBoolean2(boolean boolean2) {
    this.boolean2 = boolean2;
    return this;
  }

  public AllSchemaTypesExtension setBooleanArray1(boolean[] booleanArray1) {
    this.booleanArray1 = booleanArray1;
    return this;
  }

  public AllSchemaTypesExtension setDecimal1(Double decimal1) {
    this.decimal1 = decimal1;
    return this;
  }

  public AllSchemaTypesExtension setDecimal2(double decimal2) {
    this.decimal2 = decimal2;
    return this;
  }

  public AllSchemaTypesExtension setDecimalList1(List<Double> decimalList1) {
    this.decimalList1 = decimalList1;
    return this;
  }

  public AllSchemaTypesExtension setDecimalArray1(double[] decimalArray1) {
    this.decimalArray1 = decimalArray1;
    return this;
  }

  public AllSchemaTypesExtension setInteger1(Integer integer1) {
    this.integer1 = integer1;
    return this;
  }

  public AllSchemaTypesExtension setInteger2(int integer2) {
    this.integer2 = integer2;
    return this;
  }

  public AllSchemaTypesExtension setIntegerList1(List<Integer> integerList1) {
    this.integerList1 = integerList1;
    return this;
  }

  public AllSchemaTypesExtension setIntegerArray1(int[] integerArray1) {
    this.integerArray1 = integerArray1;
    return this;
  }

  public AllSchemaTypesExtension setDate1(Date date1) {
    this.date1 = date1;
    return this;
  }

  public AllSchemaTypesExtension setDateList1(List<Date> dateList1) {
    this.dateList1 = dateList1;
    return this;
  }

  public AllSchemaTypesExtension setDateArray1(Date[] dateArray1) {
    this.dateArray1 = dateArray1;
    return this;
  }

  public AllSchemaTypesExtension setInstant1(Instant instant1) {
    this.instant1 = instant1;
    return this;
  }

  public AllSchemaTypesExtension setInstantList1(List<Instant> instantList1) {
    this.instantList1 = instantList1;
    return this;
  }

  public AllSchemaTypesExtension setInstantArray1(Instant[] instantArray1) {
    this.instantArray1 = instantArray1;
    return this;
  }

  public AllSchemaTypesExtension setBinary1(byte[] binary1) {
    this.binary1 = binary1;
    return this;
  }

  public AllSchemaTypesExtension setBinaryList1(List<byte[]> binaryList1) {
    this.binaryList1 = binaryList1;
    return this;
  }

  public AllSchemaTypesExtension setRef1(String ref1) {
    this.ref1 = ref1;
    return this;
  }

  public AllSchemaTypesExtension setRef2(String ref2) {
    this.ref2 = ref2;
    return this;
  }

  public AllSchemaTypesExtension setRefList1(List<String> refList1) {
    this.refList1 = refList1;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AllSchemaTypesExtension)) return false;
    final AllSchemaTypesExtension other = (AllSchemaTypesExtension) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$string1 = this.getString1();
    final Object other$string1 = other.getString1();
    if (this$string1 == null ? other$string1 != null : !this$string1.equals(other$string1)) return false;
    final Object this$string2 = this.getString2();
    final Object other$string2 = other.getString2();
    if (this$string2 == null ? other$string2 != null : !this$string2.equals(other$string2)) return false;
    final Object this$stringList1 = this.getStringList1();
    final Object other$stringList1 = other.getStringList1();
    if (this$stringList1 == null ? other$stringList1 != null : !this$stringList1.equals(other$stringList1))
      return false;
    final Object this$boolean1 = this.getBoolean1();
    final Object other$boolean1 = other.getBoolean1();
    if (this$boolean1 == null ? other$boolean1 != null : !this$boolean1.equals(other$boolean1)) return false;
    final Object this$booleanList1 = this.getBooleanList1();
    final Object other$booleanList1 = other.getBooleanList1();
    if (this$booleanList1 == null ? other$booleanList1 != null : !this$booleanList1.equals(other$booleanList1))
      return false;
    if (this.isBoolean2() != other.isBoolean2()) return false;
    if (!java.util.Arrays.equals(this.getBooleanArray1(), other.getBooleanArray1())) return false;
    final Object this$decimal1 = this.getDecimal1();
    final Object other$decimal1 = other.getDecimal1();
    if (this$decimal1 == null ? other$decimal1 != null : !this$decimal1.equals(other$decimal1)) return false;
    if (Double.compare(this.getDecimal2(), other.getDecimal2()) != 0) return false;
    final Object this$decimalList1 = this.getDecimalList1();
    final Object other$decimalList1 = other.getDecimalList1();
    if (this$decimalList1 == null ? other$decimalList1 != null : !this$decimalList1.equals(other$decimalList1))
      return false;
    if (!java.util.Arrays.equals(this.getDecimalArray1(), other.getDecimalArray1())) return false;
    final Object this$integer1 = this.getInteger1();
    final Object other$integer1 = other.getInteger1();
    if (this$integer1 == null ? other$integer1 != null : !this$integer1.equals(other$integer1)) return false;
    if (this.getInteger2() != other.getInteger2()) return false;
    final Object this$integerList1 = this.getIntegerList1();
    final Object other$integerList1 = other.getIntegerList1();
    if (this$integerList1 == null ? other$integerList1 != null : !this$integerList1.equals(other$integerList1))
      return false;
    if (!java.util.Arrays.equals(this.getIntegerArray1(), other.getIntegerArray1())) return false;
    final Object this$date1 = this.getDate1();
    final Object other$date1 = other.getDate1();
    if (this$date1 == null ? other$date1 != null : !this$date1.equals(other$date1)) return false;
    final Object this$dateList1 = this.getDateList1();
    final Object other$dateList1 = other.getDateList1();
    if (this$dateList1 == null ? other$dateList1 != null : !this$dateList1.equals(other$dateList1)) return false;
    if (!java.util.Arrays.deepEquals(this.getDateArray1(), other.getDateArray1())) return false;
    final Object this$instant1 = this.getInstant1();
    final Object other$instant1 = other.getInstant1();
    if (this$instant1 == null ? other$instant1 != null : !this$instant1.equals(other$instant1)) return false;
    final Object this$instantList1 = this.getInstantList1();
    final Object other$instantList1 = other.getInstantList1();
    if (this$instantList1 == null ? other$instantList1 != null : !this$instantList1.equals(other$instantList1))
      return false;
    if (!java.util.Arrays.deepEquals(this.getInstantArray1(), other.getInstantArray1())) return false;
    if (!java.util.Arrays.equals(this.getBinary1(), other.getBinary1())) return false;
    final Object this$binaryList1 = this.getBinaryList1();
    final Object other$binaryList1 = other.getBinaryList1();
    if (this$binaryList1 == null ? other$binaryList1 != null : !this$binaryList1.equals(other$binaryList1))
      return false;
    final Object this$ref1 = this.getRef1();
    final Object other$ref1 = other.getRef1();
    if (this$ref1 == null ? other$ref1 != null : !this$ref1.equals(other$ref1)) return false;
    final Object this$ref2 = this.getRef2();
    final Object other$ref2 = other.getRef2();
    if (this$ref2 == null ? other$ref2 != null : !this$ref2.equals(other$ref2)) return false;
    final Object this$refList1 = this.getRefList1();
    final Object other$refList1 = other.getRefList1();
    if (this$refList1 == null ? other$refList1 != null : !this$refList1.equals(other$refList1)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AllSchemaTypesExtension;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $string1 = this.getString1();
    result = result * PRIME + ($string1 == null ? 43 : $string1.hashCode());
    final Object $string2 = this.getString2();
    result = result * PRIME + ($string2 == null ? 43 : $string2.hashCode());
    final Object $stringList1 = this.getStringList1();
    result = result * PRIME + ($stringList1 == null ? 43 : $stringList1.hashCode());
    final Object $boolean1 = this.getBoolean1();
    result = result * PRIME + ($boolean1 == null ? 43 : $boolean1.hashCode());
    final Object $booleanList1 = this.getBooleanList1();
    result = result * PRIME + ($booleanList1 == null ? 43 : $booleanList1.hashCode());
    result = result * PRIME + (this.isBoolean2() ? 79 : 97);
    result = result * PRIME + java.util.Arrays.hashCode(this.getBooleanArray1());
    final Object $decimal1 = this.getDecimal1();
    result = result * PRIME + ($decimal1 == null ? 43 : $decimal1.hashCode());
    final long $decimal2 = Double.doubleToLongBits(this.getDecimal2());
    result = result * PRIME + (int) ($decimal2 >>> 32 ^ $decimal2);
    final Object $decimalList1 = this.getDecimalList1();
    result = result * PRIME + ($decimalList1 == null ? 43 : $decimalList1.hashCode());
    result = result * PRIME + java.util.Arrays.hashCode(this.getDecimalArray1());
    final Object $integer1 = this.getInteger1();
    result = result * PRIME + ($integer1 == null ? 43 : $integer1.hashCode());
    result = result * PRIME + this.getInteger2();
    final Object $integerList1 = this.getIntegerList1();
    result = result * PRIME + ($integerList1 == null ? 43 : $integerList1.hashCode());
    result = result * PRIME + java.util.Arrays.hashCode(this.getIntegerArray1());
    final Object $date1 = this.getDate1();
    result = result * PRIME + ($date1 == null ? 43 : $date1.hashCode());
    final Object $dateList1 = this.getDateList1();
    result = result * PRIME + ($dateList1 == null ? 43 : $dateList1.hashCode());
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getDateArray1());
    final Object $instant1 = this.getInstant1();
    result = result * PRIME + ($instant1 == null ? 43 : $instant1.hashCode());
    final Object $instantList1 = this.getInstantList1();
    result = result * PRIME + ($instantList1 == null ? 43 : $instantList1.hashCode());
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getInstantArray1());
    result = result * PRIME + java.util.Arrays.hashCode(this.getBinary1());
    final Object $binaryList1 = this.getBinaryList1();
    result = result * PRIME + ($binaryList1 == null ? 43 : $binaryList1.hashCode());
    final Object $ref1 = this.getRef1();
    result = result * PRIME + ($ref1 == null ? 43 : $ref1.hashCode());
    final Object $ref2 = this.getRef2();
    result = result * PRIME + ($ref2 == null ? 43 : $ref2.hashCode());
    final Object $refList1 = this.getRefList1();
    result = result * PRIME + ($refList1 == null ? 43 : $refList1.hashCode());
    return result;
  }

  public String toString() {
    return "AllSchemaTypesExtension(string1=" + this.getString1() + ", string2=" + this.getString2() + ", stringList1=" + this.getStringList1() + ", boolean1=" + this.getBoolean1() + ", booleanList1=" + this.getBooleanList1() + ", boolean2=" + this.isBoolean2() + ", booleanArray1=" + java.util.Arrays.toString(this.getBooleanArray1()) + ", decimal1=" + this.getDecimal1() + ", decimal2=" + this.getDecimal2() + ", decimalList1=" + this.getDecimalList1() + ", decimalArray1=" + java.util.Arrays.toString(this.getDecimalArray1()) + ", integer1=" + this.getInteger1() + ", integer2=" + this.getInteger2() + ", integerList1=" + this.getIntegerList1() + ", integerArray1=" + java.util.Arrays.toString(this.getIntegerArray1()) + ", date1=" + this.getDate1() + ", dateList1=" + this.getDateList1() + ", dateArray1=" + java.util.Arrays.deepToString(this.getDateArray1()) + ", instant1=" + this.getInstant1() + ", instantList1=" + this.getInstantList1() + ", instantArray1=" + java.util.Arrays.deepToString(this.getInstantArray1()) + ", binary1=" + java.util.Arrays.toString(this.getBinary1()) + ", binaryList1=" + this.getBinaryList1() + ", ref1=" + this.getRef1() + ", ref2=" + this.getRef2() + ", refList1=" + this.getRefList1() + ")";
  }
}
